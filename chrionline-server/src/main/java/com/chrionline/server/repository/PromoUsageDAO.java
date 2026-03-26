package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PromoUsageDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public boolean saveUsage(int promoId, int userId, int orderId, double discountAmount) {
        String sql = """
                INSERT INTO promo_usage (promo_id, utilisateur_id, commande_id, discount_amount)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ps.setInt(2, userId);
            ps.setInt(3, orderId);
            ps.setDouble(4, discountAmount);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("PromoUsageDAO.saveUsage : " + e.getMessage());
            return false;
        }
    }

    public int countUsages(int promoId) {
        String sql = "SELECT COUNT(*) FROM promo_usage WHERE promo_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("PromoUsageDAO.countUsages : " + e.getMessage());
        }
        return 0;
    }

    public int countUsagesByUser(int promoId, int userId) {
        String sql = "SELECT COUNT(*) FROM promo_usage WHERE promo_id = ? AND utilisateur_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("PromoUsageDAO.countUsagesByUser : " + e.getMessage());
        }
        return 0;
    }

    public double sumDiscount(int promoId) {
        String sql = "SELECT COALESCE(SUM(discount_amount), 0) FROM promo_usage WHERE promo_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("PromoUsageDAO.sumDiscount : " + e.getMessage());
        }
        return 0;
    }

    public List<String> findClients(int promoId) {
        String sql = """
                SELECT DISTINCT u.email
                FROM promo_usage pu
                JOIN utilisateur u ON u.id = pu.utilisateur_id
                WHERE pu.promo_id = ?
                ORDER BY u.email
                """;
        List<String> clients = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clients.add(rs.getString(1));
            }
        } catch (Exception e) {
            System.err.println("PromoUsageDAO.findClients : " + e.getMessage());
        }
        return clients;
    }
}
