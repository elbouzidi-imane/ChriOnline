package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.Livraison;

import java.sql.*;

public class LivraisonDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public Livraison save(Livraison livraison) {
        String sql = """
            INSERT INTO livraison
                (commande_id, mode_livraison, statut, date_estimee, suivi_actif)
            VALUES (?,?,?,?,?) RETURNING id
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, livraison.getCommandeId());
            ps.setString(2, livraison.getModeLivraison());
            ps.setString(3, livraison.getStatut());
            ps.setDate(4, livraison.getDateEstimee() != null
                    ? new java.sql.Date(livraison.getDateEstimee().getTime()) : null);
            ps.setBoolean(5, livraison.isSuiviActif());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) livraison.setId(rs.getInt("id"));
            return livraison;
        } catch (Exception e) {
            System.err.println("LivraisonDAO.save : " + e.getMessage());
            return null;
        }
    }

    public boolean updateStatut(int commandeId, String statut) {
        String sql = "UPDATE livraison SET statut=? WHERE commande_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, commandeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("LivraisonDAO.updateStatut : " + e.getMessage());
            return false;
        }
    }

    public Livraison findByCommande(int commandeId) {
        String sql = "SELECT * FROM livraison WHERE commande_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Livraison l = new Livraison();
                l.setId(rs.getInt("id"));
                l.setCommandeId(rs.getInt("commande_id"));
                l.setModeLivraison(rs.getString("mode_livraison"));
                l.setStatut(rs.getString("statut"));
                l.setDateEstimee(rs.getDate("date_estimee"));
                l.setDateEffective(rs.getDate("date_effective"));
                l.setSuiviActif(rs.getBoolean("suivi_actif"));
                return l;
            }
        } catch (Exception e) {
            System.err.println("LivraisonDAO.findByCommande : " + e.getMessage());
        }
        return null;
    }
}