package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.NotificationEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public NotificationEntry save(int userId, String message) {
        String sql = """
            INSERT INTO notification (utilisateur_id, message, lue)
            VALUES (?, ?, false)
            RETURNING id, created_at, lue
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                NotificationEntry entry = new NotificationEntry();
                entry.setId(rs.getInt("id"));
                entry.setUtilisateurId(userId);
                entry.setMessage(message);
                entry.setLue(rs.getBoolean("lue"));
                entry.setCreatedAt(rs.getTimestamp("created_at"));
                return entry;
            }
        } catch (Exception e) {
            System.err.println("NotificationDAO.save : " + e.getMessage());
        }
        return null;
    }

    public List<NotificationEntry> findByUser(int userId) {
        String sql = """
            SELECT * FROM notification
            WHERE utilisateur_id = ?
            ORDER BY created_at DESC
            LIMIT 30
            """;
        List<NotificationEntry> entries = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NotificationEntry entry = new NotificationEntry();
                entry.setId(rs.getInt("id"));
                entry.setUtilisateurId(rs.getInt("utilisateur_id"));
                entry.setMessage(rs.getString("message"));
                entry.setLue(rs.getBoolean("lue"));
                entry.setCreatedAt(rs.getTimestamp("created_at"));
                entries.add(entry);
            }
        } catch (Exception e) {
            System.err.println("NotificationDAO.findByUser : " + e.getMessage());
        }
        return entries;
    }

    public boolean markAsRead(int notificationId, int userId) {
        String sql = "UPDATE notification SET lue = true WHERE id = ? AND utilisateur_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("NotificationDAO.markAsRead : " + e.getMessage());
            return false;
        }
    }
}
