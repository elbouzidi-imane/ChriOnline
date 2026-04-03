package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.Payment;

import java.sql.*;

public class PaymentDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public Payment save(Payment payment) {
        String sql = """
            INSERT INTO paiement
                (commande_id, montant, mode_paiement, statut, reference)
            VALUES (?,?,?,?,?) RETURNING id, date_paiement
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment.getCommandeId());
            ps.setDouble(2, payment.getMontant());
            ps.setString(3, payment.getModePaiement());
            ps.setString(4, payment.getStatut());
            ps.setString(5, payment.getReference());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                payment.setId(rs.getInt("id"));
                payment.setDatePaiement(rs.getTimestamp("date_paiement"));
            }
            return payment;
        } catch (Exception e) {
            System.err.println("PaymentDAO.save : " + e.getMessage());
            return null;
        }
    }

    public Payment findByCommande(int commandeId) {
        String sql = "SELECT * FROM paiement WHERE commande_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Payment p = new Payment();
                p.setId(rs.getInt("id"));
                p.setCommandeId(rs.getInt("commande_id"));
                p.setMontant(rs.getDouble("montant"));
                p.setModePaiement(rs.getString("mode_paiement"));
                p.setStatut(rs.getString("statut"));
                p.setDatePaiement(rs.getTimestamp("date_paiement"));
                p.setReference(rs.getString("reference"));
                return p;
            }
        } catch (Exception e) {
            System.err.println("PaymentDAO.findByCommande : " + e.getMessage());
        }
        return null;
    }

    public java.util.List<Payment> findAll() {
        String sql = "SELECT * FROM paiement ORDER BY date_paiement DESC";
        java.util.List<Payment> payments = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Payment p = new Payment();
                p.setId(rs.getInt("id"));
                p.setCommandeId(rs.getInt("commande_id"));
                p.setMontant(rs.getDouble("montant"));
                p.setModePaiement(rs.getString("mode_paiement"));
                p.setStatut(rs.getString("statut"));
                p.setDatePaiement(rs.getTimestamp("date_paiement"));
                p.setReference(rs.getString("reference"));
                payments.add(p);
            }
        } catch (Exception e) {
            System.err.println("PaymentDAO.findAll : " + e.getMessage());
        }
        return payments;
    }

    public boolean updateStatut(int commandeId, String statut) {
        String sql = "UPDATE paiement SET statut = ? WHERE commande_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, commandeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("PaymentDAO.updateStatut : " + e.getMessage());
            return false;
        }
    }
}
