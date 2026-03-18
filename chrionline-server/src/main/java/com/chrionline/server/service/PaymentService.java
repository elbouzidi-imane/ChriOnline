package com.chrionline.server.service;

import com.chrionline.server.model.Payment;
import com.chrionline.server.repository.PaymentDAO;

public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    // ── Simuler un paiement ───────────────────────────
    public Payment simulerPaiement(int commandeId, double montant,
                                   String modePaiement) {
        Payment payment = new Payment(commandeId, montant, modePaiement);
        payment.setStatut("VALIDE");
        payment.setReference("TXN-" + System.currentTimeMillis());
        return paymentDAO.save(payment);
    }

    // ── Récupérer le paiement d'une commande ──────────
    public Payment getByCommande(int commandeId) {
        return paymentDAO.findByCommande(commandeId);
    }

    // ── Rembourser ────────────────────────────────────
    public boolean rembourser(int commandeId) {
        Payment payment = paymentDAO.findByCommande(commandeId);
        if (payment == null) return false;
        // Dans ce projet : juste mettre à jour le statut
        // En réel : appeler l'API bancaire
        String sql = "UPDATE paiement SET statut='REMBOURSE' WHERE commande_id=?";
        try (java.sql.Connection conn =
                     com.chrionline.server.db.DatabaseManager
                             .getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("PaymentService.rembourser : " + e.getMessage());
            return false;
        }
    }

    // ── Vérifier si une commande est payée ────────────
    public boolean isPaye(int commandeId) {
        Payment payment = paymentDAO.findByCommande(commandeId);
        return payment != null && payment.isValide();
    }
}