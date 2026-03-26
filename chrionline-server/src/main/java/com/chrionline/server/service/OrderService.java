package com.chrionline.server.service;

import com.chrionline.server.model.*;
import com.chrionline.server.repository.*;

import java.time.LocalDate;
import java.util.List;

public class OrderService {

    private final OrderDAO     orderDAO     = new OrderDAO();
    private final CartDAO      cartDAO      = new CartDAO();
    private final ProductDAO   productDAO   = new ProductDAO();
    private final PaymentDAO   paymentDAO   = new PaymentDAO();
    private final PaymentService paymentService = new PaymentService();
    private final LivraisonDAO livraisonDAO = new LivraisonDAO();
    private final PromoService promoService = new PromoService();

    // ── Valider une commande ──────────────────────────
    public Order validerCommande(int userId, String adresse,
                                 String modePaiement, String modeLivraison, String promoCode) {

        // 1. Récupérer le panier
        Cart cart = cartDAO.getOrCreateCart(userId);
        if (cart == null || cart.isEmpty()) {
            System.out.println("OrderService : panier vide pour userId=" + userId);
            return null;
        }

        List<CartLine> lignes = cart.getLignes();

        // 2. Vérifier le stock pour chaque ligne
        for (CartLine ligne : lignes) {
            Product product = productDAO.findById(ligne.getProduitId());
            if (product == null) return null;

            boolean stockOk = false;
            for (ProductSize taille : product.getTailles()) {
                if (taille.getId() == ligne.getTailleId()) {
                    if (taille.getStock() < ligne.getQuantite()) {
                        System.out.println("OrderService : stock insuffisant pour taille "
                                + taille.getValeur());
                        return null;
                    }
                    stockOk = true;
                    break;
                }
            }
            if (!stockOk) return null;
        }

        // 3. Calculer le total
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

        // 4. Créer la commande avec référence temporaire
        Order order = new Order(userId, total, adresse);
        order.setReference("TEMP-" + System.currentTimeMillis());
        order = orderDAO.save(order);
        if (order == null) return null;

        // 5. Générer la vraie référence avec l'id BDD
        String reference = String.format("CMD-%d-%05d",
                LocalDate.now().getYear(), order.getId());
        order.setReference(reference);
        updateReference(order.getId(), reference);

        // 6. Créer les lignes commande + décrémenter le stock
        for (CartLine ligne : lignes) {
            OrderLine orderLine = new OrderLine(
                    order.getId(),
                    ligne.getProduitId(),
                    ligne.getTailleId(),
                    ligne.getQuantite(),
                    ligne.getPrixUnitaire()
            );
            orderDAO.saveOrderLine(orderLine);

            // Décrémenter le stock
            Product product = productDAO.findById(ligne.getProduitId());
            for (ProductSize taille : product.getTailles()) {
                if (taille.getId() == ligne.getTailleId()) {
                    int newStock = taille.getStock() - ligne.getQuantite();
                    productDAO.updateStock(taille.getId(), newStock);
                    break;
                }
            }
        }

        // 7. Simuler le paiement
        Payment payment = new Payment(order.getId(), total, modePaiement);
        payment.setStatut("VALIDE");
        payment.setReference("TXN-" + System.currentTimeMillis());
        paymentDAO.save(payment);

        // 8. Créer la livraison
        Livraison livraison = new Livraison(order.getId(), modeLivraison);
        livraisonDAO.save(livraison);

        // 9. Vider le panier
        cartDAO.clearCart(cart.getId());

        // 10. Mettre à jour le statut → VALIDEE
        orderDAO.updateStatut(order.getId(), "VALIDEE");
        order.setStatut("VALIDEE");

        if (!normalizedPromoCode.isEmpty() && discountAmount > 0) {
            promoService.registerUsage(normalizedPromoCode, userId, order.getId(), discountAmount);
        }

        return order;
    }

    // ── Commandes d'un utilisateur ────────────────────
    public List<Order> getOrdersByUser(int userId) {
        return orderDAO.findByUser(userId);
    }

    // ── Commande par id ───────────────────────────────
    public Order getOrderById(int id) {
        return orderDAO.findById(id);
    }

    // ── Toutes les commandes (admin) ──────────────────
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    // ── Changer le statut (admin) ─────────────────────
    public boolean updateStatut(int id, String statut) {
        return orderDAO.updateStatut(id, statut);
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

    // ── Mettre à jour la référence en BDD ─────────────
    private void updateReference(int orderId, String reference) {
        String sql = "UPDATE commande SET reference = ? WHERE id = ?";
        try (java.sql.Connection conn =
                     com.chrionline.server.db.DatabaseManager
                             .getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reference);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("OrderService.updateReference : " + e.getMessage());
        }
    }
}
