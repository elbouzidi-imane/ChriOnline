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

public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final PaymentService paymentService = new PaymentService();
    private final LivraisonDAO livraisonDAO = new LivraisonDAO();
    private final PromoService promoService = new PromoService();
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = EmailService.getInstance();

    public Order validerCommande(int userId, String adresse,
                                 String modePaiement, String modeLivraison, String promoCode) {
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

        sendConfirmationEmail(order);
        return order;
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
}
