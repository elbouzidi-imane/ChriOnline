package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.err.println("UserDAO.findByEmail : " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.err.println("UserDAO.findById : " + e.getMessage());
        }
        return null;
    }

    public User save(User user) {
        String sql = """
            INSERT INTO utilisateur
                (nom, prenom, email, mot_de_passe, telephone, adresse, role, statut, notifications_activees)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, date_inscription, notifications_activees
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMotDePasse());
            ps.setString(5, user.getTelephone());
            ps.setString(6, user.getAdresse());
            ps.setString(7, user.getRole());
            ps.setString(8, user.getStatut());
            ps.setBoolean(9, user.isNotificationsActivees());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setDateInscription(rs.getDate("date_inscription"));
                user.setNotificationsActivees(rs.getBoolean("notifications_activees"));
            }
            return user;
        } catch (Exception e) {
            System.err.println("UserDAO.save : " + e.getMessage());
            return null;
        }
    }

    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE utilisateur SET statut = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.updateStatut : " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfil(User user) {
        String sql = """
            UPDATE utilisateur
            SET nom=?, prenom=?, telephone=?, adresse=?
            WHERE id=?
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getTelephone());
            ps.setString(4, user.getAdresse());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.updateProfil : " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(int id, String newHashedPassword) {
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.updatePassword : " + e.getMessage());
            return false;
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM utilisateur ORDER BY date_inscription DESC";
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) users.add(mapRow(rs));
        } catch (Exception e) {
            System.err.println("UserDAO.findAll : " + e.getMessage());
        }
        return users;
    }

    public List<Integer> findAdminIds() {
        String sql = "SELECT id FROM utilisateur WHERE role = 'ADMIN' AND statut = 'ACTIF'";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (Exception e) {
            System.err.println("UserDAO.findAdminIds : " + e.getMessage());
        }
        return ids;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.emailExists : " + e.getMessage());
        }
        return false;
    }

    public boolean updateDateNaissance(int id, java.sql.Date dateNaissance) {
        String sql = "UPDATE utilisateur SET date_naissance=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, dateNaissance);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.updateDateNaissance : " + e.getMessage());
            return false;
        }
    }

    public boolean updateNotificationPreference(int id, boolean notificationsActivees) {
        String sql = "UPDATE utilisateur SET notifications_activees=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, notificationsActivees);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.updateNotificationPreference : " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("UserDAO.delete : " + e.getMessage());
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setTelephone(rs.getString("telephone"));
        u.setAdresse(rs.getString("adresse"));
        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));
        u.setDateInscription(rs.getDate("date_inscription"));
        u.setDateNaissance(rs.getDate("date_naissance"));
        u.setNotificationsActivees(rs.getBoolean("notifications_activees"));
        return u;
    }
}
