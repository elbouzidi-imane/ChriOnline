package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.GuideTaille;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuideDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Guide d'un produit ────────────────────────────
    public List<GuideTaille> findByProduit(int produitId) {
        String sql = "SELECT * FROM guide_taille WHERE produit_id = ?";
        List<GuideTaille> guides = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produitId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) guides.add(mapRow(rs));
        } catch (Exception e) {
            System.err.println("GuideDAO.findByProduit : " + e.getMessage());
        }
        return guides;
    }

    // ── Ajouter une entrée guide ──────────────────────
    public GuideTaille save(GuideTaille guide) {
        String sql = """
            INSERT INTO guide_taille
                (produit_id, taille, poitrine, taille_cm, hanches)
            VALUES (?,?,?,?,?) RETURNING id
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guide.getProduitId());
            ps.setString(2, guide.getTaille());
            ps.setString(3, guide.getPoitrine());
            ps.setString(4, guide.getTailleCm());
            ps.setString(5, guide.getHanches());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) guide.setId(rs.getInt("id"));
            return guide;
        } catch (Exception e) {
            System.err.println("GuideDAO.save : " + e.getMessage());
            return null;
        }
    }

    // ── Supprimer une entrée guide ────────────────────
    public boolean delete(int id) {
        String sql = "DELETE FROM guide_taille WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("GuideDAO.delete : " + e.getMessage());
            return false;
        }
    }

    // ── Supprimer tout le guide d'un produit ─────────
    public boolean deleteByProduit(int produitId) {
        String sql = "DELETE FROM guide_taille WHERE produit_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produitId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("GuideDAO.deleteByProduit : " + e.getMessage());
            return false;
        }
    }

    private GuideTaille mapRow(ResultSet rs) throws SQLException {
        GuideTaille g = new GuideTaille();
        g.setId(rs.getInt("id"));
        g.setProduitId(rs.getInt("produit_id"));
        g.setTaille(rs.getString("taille"));
        g.setPoitrine(rs.getString("poitrine"));
        g.setTailleCm(rs.getString("taille_cm"));
        g.setHanches(rs.getString("hanches"));
        return g;
    }
}