package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.ProductSize;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SizeDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Ajouter une taille ────────────────────────────
    public ProductSize save(ProductSize size) {
        String sql = """
            INSERT INTO taille_produit (produit_id, valeur, stock)
            VALUES (?,?,?) RETURNING id
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, size.getProduitId());
            ps.setString(2, size.getValeur());
            ps.setInt(3, size.getStock());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) size.setId(rs.getInt("id"));
            return size;
        } catch (Exception e) {
            System.err.println("SizeDAO.save : " + e.getMessage());
            return null;
        }
    }

    // ── Supprimer une taille ──────────────────────────
    public boolean delete(int id) {
        String sql = "DELETE FROM taille_produit WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("SizeDAO.delete : " + e.getMessage());
            return false;
        }
    }

    // ── Tailles d'un produit ──────────────────────────
    public List<ProductSize> findByProduit(int produitId) {
        String sql = "SELECT * FROM taille_produit WHERE produit_id = ?";
        List<ProductSize> sizes = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produitId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductSize s = new ProductSize();
                s.setId(rs.getInt("id"));
                s.setProduitId(rs.getInt("produit_id"));
                s.setValeur(rs.getString("valeur"));
                s.setStock(rs.getInt("stock"));
                sizes.add(s);
            }
        } catch (Exception e) {
            System.err.println("SizeDAO.findByProduit : " + e.getMessage());
        }
        return sizes;
    }
}