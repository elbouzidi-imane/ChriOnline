package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.ProductReview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public ProductReview save(ProductReview review) {
        String sql = """
            INSERT INTO avis_produit (utilisateur_id, produit_id, commande_id, note, commentaire, avis_taille)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id, created_at
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getUtilisateurId());
            ps.setInt(2, review.getProduitId());
            ps.setInt(3, review.getCommandeId());
            ps.setInt(4, review.getNote());
            ps.setString(5, review.getCommentaire());
            ps.setString(6, review.getAvisTaille());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                review.setId(rs.getInt("id"));
                review.setCreatedAt(rs.getTimestamp("created_at"));
                return review;
            }
        } catch (Exception e) {
            System.err.println("ReviewDAO.save : " + e.getMessage());
        }
        return null;
    }

    public List<ProductReview> findByProduct(int productId) {
        String sql = "SELECT * FROM avis_produit WHERE produit_id = ? ORDER BY created_at DESC";
        List<ProductReview> reviews = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("ReviewDAO.findByProduct : " + e.getMessage());
        }
        return reviews;
    }

    public boolean existsByUserOrderProduct(int userId, int orderId, int productId) {
        String sql = "SELECT COUNT(*) FROM avis_produit WHERE utilisateur_id = ? AND commande_id = ? AND produit_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, orderId);
            ps.setInt(3, productId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            System.err.println("ReviewDAO.existsByUserOrderProduct : " + e.getMessage());
            return false;
        }
    }

    private ProductReview mapRow(ResultSet rs) throws Exception {
        ProductReview review = new ProductReview();
        review.setId(rs.getInt("id"));
        review.setUtilisateurId(rs.getInt("utilisateur_id"));
        review.setProduitId(rs.getInt("produit_id"));
        review.setCommandeId(rs.getInt("commande_id"));
        review.setNote(rs.getInt("note"));
        review.setCommentaire(rs.getString("commentaire"));
        review.setAvisTaille(rs.getString("avis_taille"));
        review.setCreatedAt(rs.getTimestamp("created_at"));
        return review;
    }
}
