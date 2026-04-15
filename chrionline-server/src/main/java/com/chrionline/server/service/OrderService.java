package com.chrionline.server.service;

import com.chrionline.server.model.CancellationConfig;
import com.chrionline.server.model.CancellationResult;
import com.chrionline.server.model.Cart;
import com.chrionline.server.model.CartLine;
import com.chrionline.server.model.Livraison;
import com.chrionline.server.model.Order;
import com.chrionline.server.model.OrderLine;
import com.chrionline.server.model.Payment;
import com.chrionline.server.model.Product;
import com.chrionline.server.model.ProductSize;
import com.chrionline.server.model.PromoValidationResult;
import com.chrionline.server.model.User;
import com.chrionline.server.repository.CartDAO;
import com.chrionline.server.repository.LivraisonDAO;
import com.chrionline.server.repository.OrderDAO;
import com.chrionline.server.repository.PaymentDAO;
import com.chrionline.server.repository.ProductDAO;
import com.chrionline.server.repository.UserDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {
    private static final long DUPLICATE_WINDOW_MILLIS = 30_000L;
    private static final long RECENT_DUPLICATE_DB_WINDOW_MILLIS = 30L * 60L * 1000L;
    private static final Map<Integer, Object> USER_ORDER_LOCKS = new ConcurrentHashMap<>();
    private static final Map<String, Long> RECENT_ORDER_FINGERPRINTS = new ConcurrentHashMap<>();

    private final OrderDAO orderDAO = new OrderDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final PaymentService paymentService = new PaymentService();
    private final LivraisonDAO livraisonDAO = new LivraisonDAO();
    private final PromoService promoService = new PromoService();
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = EmailService.getInstance();
    private final NotificationService notificationService = new NotificationService();

    public Order validerCommande(int userId, String adresse,
                                 String modePaiement, String modeLivraison, String promoCode) {
        Object lock = USER_ORDER_LOCKS.computeIfAbsent(userId, ignored -> new Object());
        synchronized (lock) {
            cleanupRecentFingerprints();

            Cart cart = cartDAO.getOrCreateCart(userId);
            if (cart == null || cart.isEmpty()) {
                System.out.println("OrderService : panier vide pour userId=" + userId);
                return null;
            }

            List<CartLine> lignes = cart.getLignes();
            for (CartLine ligne : lignes) {
                Product product = productDAO.findById(ligne.getProduitId());
                if (product == null) return null;

                boolean stockOk = false;
                for (ProductSize taille : product.getTailles()) {
                    if (taille.getId() == ligne.getTailleId()) {
                        if (taille.getStock() < ligne.getQuantite()) {
                            System.out.println("OrderService : stock insuffisant pour taille " + taille.getValeur());
                            return null;
                        }
                        stockOk = true;
                        break;
                    }
                }
                if (!stockOk) return null;
            }

            double originalTotal = cart.getTotal();
            double total = originalTotal;
            double discountAmount = 0;
            String normalizedPromoCode = promoCode == null ? "" : promoCode.trim();
            if (!normalizedPromoCode.isEmpty()) {
                PromoValidationResult promoResult = promoService.validatePromo(normalizedPromoCode, originalTotal, userId);
                if (!promoResult.isValid()) {
                    return null;
                }
                total = promoResult.getFinalTotal();
                discountAmount = promoResult.getDiscountAmount();
            }

            String successfulFingerprint = buildReplayFingerprint(userId, adresse, modePaiement, modeLivraison, promoCode);
            if (successfulFingerprint != null && RECENT_ORDER_FINGERPRINTS.containsKey(successfulFingerprint)) {
                System.out.println("OrderService : commande dupliquee detectee pour userId=" + userId);
                return null;
            }
            if (hasRecentPersistedDuplicate(userId, total, adresse, modePaiement, modeLivraison, lignes)) {
                System.out.println("OrderService : commande dupliquee detectee en base pour userId=" + userId);
                return null;
            }

            Order order = new Order(userId, total, adresse);
            order.setReference("TEMP-" + System.currentTimeMillis());
            order = orderDAO.save(order);
            if (order == null) return null;

            String reference = String.format("CMD-%d-%05d", LocalDate.now().getYear(), order.getId());
            order.setReference(reference);
            updateReference(order.getId(), reference);

            for (CartLine ligne : lignes) {
                OrderLine orderLine = new OrderLine(
                        order.getId(),
                        ligne.getProduitId(),
                        ligne.getTailleId(),
                        ligne.getQuantite(),
                        ligne.getPrixUnitaire()
                );
                orderDAO.saveOrderLine(orderLine);

                Product product = productDAO.findById(ligne.getProduitId());
                for (ProductSize taille : product.getTailles()) {
                    if (taille.getId() == ligne.getTailleId()) {
                        int newStock = taille.getStock() - ligne.getQuantite();
                        productDAO.updateStock(taille.getId(), newStock);
                        notifyLowStock(product, taille.getValeur(), newStock);
                        break;
                    }
                }
            }

            Payment payment = new Payment(order.getId(), total, modePaiement);
            payment.setStatut("VALIDE");
            payment.setReference("TXN-" + System.currentTimeMillis());
            paymentDAO.save(payment);

            Livraison livraison = new Livraison(order.getId(), modeLivraison);
            livraisonDAO.save(livraison);

            cartDAO.clearCart(cart.getId());

            orderDAO.updateStatut(order.getId(), "VALIDEE");
            order.setStatut("VALIDEE");

            if (!normalizedPromoCode.isEmpty() && discountAmount > 0) {
                promoService.registerUsage(normalizedPromoCode, userId, order.getId(), discountAmount);
            }

            if (successfulFingerprint != null) {
                RECENT_ORDER_FINGERPRINTS.put(successfulFingerprint, System.currentTimeMillis());
            }

            notificationService.notifyAdmins(
                    "Nouvelle commande " + order.getReference() + " a traiter. Montant: "
                            + String.format("%.2f", order.getMontantTotal()) + " MAD"
            );
            sendConfirmationEmail(order);
            return order;
        }
    }

    public String buildReplayFingerprint(int userId, String adresse,
                                         String modePaiement, String modeLivraison, String promoCode) {
        Cart cart = cartDAO.getOrCreateCart(userId);
        if (cart == null || cart.isEmpty()) {
            return null;
        }

        String linesSignature = cart.getLignes().stream()
                .map(line -> line.getProduitId() + "-" + line.getTailleId() + "-" + line.getQuantite() + "-" + line.getPrixUnitaire())
                .sorted()
                .reduce((left, right) -> left + ";" + right)
                .orElse("");

        return userId + "|"
                + normalize(adresse) + "|"
                + normalize(modePaiement) + "|"
                + normalize(modeLivraison) + "|"
                + normalize(promoCode) + "|"
                + String.format("%.2f", cart.getTotal()) + "|"
                + linesSignature;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private boolean hasRecentPersistedDuplicate(int userId, double total, String adresse,
                                                String modePaiement, String modeLivraison,
                                                List<CartLine> lignes) {
        java.sql.Timestamp since = new java.sql.Timestamp(System.currentTimeMillis() - RECENT_DUPLICATE_DB_WINDOW_MILLIS);
        String currentLinesSignature = buildCartLineSignature(lignes);
        for (Order order : orderDAO.findRecentValidatedByUser(userId, since)) {
            if (Math.abs(order.getMontantTotal() - total) > 0.0001d) {
                continue;
            }
            if (!normalize(order.getAdresseLivraison()).equals(normalize(adresse))) {
                continue;
            }
            Payment payment = paymentDAO.findByCommande(order.getId());
            if (payment == null || !normalize(payment.getModePaiement()).equals(normalize(modePaiement))) {
                continue;
            }
            Livraison livraison = livraisonDAO.findByCommande(order.getId());
            if (livraison == null || !normalize(livraison.getModeLivraison()).equals(normalize(modeLivraison))) {
                continue;
            }
            String previousLinesSignature = buildOrderLineSignature(order.getLignes());
            if (previousLinesSignature.equals(currentLinesSignature)) {
                return true;
            }
        }
        return false;
    }

    private String buildCartLineSignature(List<CartLine> lignes) {
        return lignes.stream()
                .map(line -> line.getProduitId() + "-" + line.getTailleId() + "-" + line.getQuantite() + "-" + String.format("%.2f", line.getPrixUnitaire()))
                .sorted()
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
    }

    private String buildOrderLineSignature(List<OrderLine> lignes) {
        return lignes.stream()
                .map(line -> line.getProduitId() + "-" + line.getTailleId() + "-" + line.getQuantite() + "-" + String.format("%.2f", line.getPrixUnitaire()))
                .sorted()
                .reduce((left, right) -> left + ";" + right)
                .orElse("");
    }

    private void cleanupRecentFingerprints() {
        long now = System.currentTimeMillis();
        RECENT_ORDER_FINGERPRINTS.entrySet().removeIf(entry -> now - entry.getValue() > DUPLICATE_WINDOW_MILLIS);
    }

    public List<Order> getOrdersByUser(int userId) {
        return orderDAO.findByUser(userId);
    }

    public Order getOrderById(int id) {
        return orderDAO.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    public boolean updateStatut(int id, String statut) {
        Order order = orderDAO.findById(id);
        if (order == null) {
            System.err.println("OrderService.updateStatut : commande introuvable id=" + id);
            return false;
        }

        String currentStatus = order.getStatut();
        if (statut == null || statut.isBlank()) {
            System.err.println("OrderService.updateStatut : statut vide pour commande=" + id);
            return false;
        }
        if (statut.equals(currentStatus)) {
            System.out.println("OrderService.updateStatut : aucun changement pour commande=" + id + " statut=" + statut);
            return true;
        }
        boolean updated = orderDAO.updateStatut(id, statut);
        if (!updated) {
            System.err.println("OrderService.updateStatut : echec maj statut commande=" + id + " vers " + statut);
            return false;
        }

        sendStatusEmail(order.getUtilisateurId(), order.getReference(), statut);
        notificationService.notifyClientAboutAdminAction(
                order.getUtilisateurId(),
                "Votre commande " + order.getReference() + " est maintenant " + statut + "."
        );
        return true;
    }

    public CancellationConfig getCancellationConfig() {
        return CancellationConfigService.getConfig();
    }

    public CancellationResult cancelOrder(int userId, int orderId, String reason) {
        Order order = orderDAO.findById(orderId);
        if (order == null || order.getUtilisateurId() != userId) {
            return null;
        }

        CancellationConfig config = CancellationConfigService.getConfig();
        if (!config.getCancellableStatus().equals(order.getStatut())) {
            return null;
        }
        if (config.isReasonRequired() && (reason == null || reason.isBlank())) {
            return null;
        }

        for (OrderLine line : order.getLignes()) {
            Product product = productDAO.findById(line.getProduitId());
            if (product == null) {
                continue;
            }
            for (ProductSize size : product.getTailles()) {
                if (size.getId() == line.getTailleId()) {
                    productDAO.updateStock(size.getId(), size.getStock() + line.getQuantite());
                    break;
                }
            }
        }

        orderDAO.updateCancellationInfo(orderId, "ANNULEE", reason);
        order.setStatut("ANNULEE");
        order.setMotifAnnulation(reason);
        notificationService.notifyAdminsAboutClient(
                userId,
                "a annule la commande " + order.getReference() + "."
        );

        String refundMessage;
        if (config.isAutomaticRefund()) {
            boolean refunded = paymentDAO.findByCommande(orderId) != null && paymentService.rembourser(orderId);
            refundMessage = refunded
                    ? "Remboursement automatique lance."
                    : "Remboursement automatique en attente de traitement.";
        } else {
            if (paymentDAO.findByCommande(orderId) != null) {
                paymentDAO.updateStatut(orderId, "EN_ATTENTE");
            }
            refundMessage = "Remboursement manuel prevu par l'administration.";
        }

        return new CancellationResult(
                order.getReference(),
                "ANNULEE",
                "Commande annulee avec succes.",
                refundMessage,
                config.getEstimatedRefundDelay()
        );
    }

    private void sendConfirmationEmail(Order order) {
        User user = userDAO.findById(order.getUtilisateurId());
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            System.err.println("OrderService.sendConfirmationEmail : utilisateur ou email introuvable pour commande=" + order.getId());
            return;
        }
        String firstName = user.getPrenom() == null || user.getPrenom().isBlank() ? "client" : user.getPrenom();
        boolean sent = emailService.sendOrderConfirmationEmail(
                user.getEmail(),
                firstName,
                order.getReference(),
                order.getMontantTotal(),
                user.isNotificationsActivees()
        );
        System.out.println("OrderService.sendConfirmationEmail : commande=" + order.getReference() + " envoye=" + sent);
    }

    private void sendStatusEmail(int userId, String reference, String statut) {
        User user = userDAO.findById(userId);
        if (user == null || !user.isNotificationsActivees()) {
            System.out.println("OrderService.sendStatusEmail : notifications desactivees ou utilisateur introuvable userId=" + userId);
            return;
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            System.err.println("OrderService.sendStatusEmail : email vide userId=" + userId);
            return;
        }
        String firstName = user.getPrenom() == null || user.getPrenom().isBlank() ? "client" : user.getPrenom();
        boolean sent = emailService.sendOrderStatusEmail(user.getEmail(), firstName, reference, statut);
        System.out.println("OrderService.sendStatusEmail : reference=" + reference + " statut=" + statut + " envoye=" + sent);
    }

    private void updateReference(int orderId, String reference) {
        String sql = "UPDATE commande SET reference = ? WHERE id = ?";
        try (java.sql.Connection conn =
                     com.chrionline.server.db.DatabaseManager.getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reference);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("OrderService.updateReference : " + e.getMessage());
        }
    }

    private void notifyLowStock(Product product, String sizeValue, int newStock) {
        if (product == null || newStock > 5) {
            return;
        }
        String level = newStock == 0 ? "Rupture" : "Stock faible";
        String suffix = sizeValue == null || sizeValue.isBlank() ? "" : " (taille " + sizeValue + ")";
        notificationService.notifyAdmins(
                level + " sur " + product.getNom() + suffix + " : reste " + newStock + " unite(s)."
        );
    }
}
