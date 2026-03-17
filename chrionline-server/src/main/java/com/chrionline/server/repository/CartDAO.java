package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.Cart;
import com.chrionline.server.model.CartLine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Récupère ou crée le panier de l'utilisateur ───
    public Cart getOrCreateCart(int userId) {
        String sql = "SELECT * FROM panier WHERE utilisateur_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Cart cart = new Cart();
                cart.setId(rs.getInt("id"));
                cart.setUtilisateurId(userId);
                cart.setDateCreation(rs.getDate("date_creation"));
                cart.setLignes(getLines(cart.getId()));
                return cart;
            }
        } catch (Exception e) {
            System.err.println("CartDAO.getOrCreateCart get : " + e.getMessage());
        }
        return createCart(userId);
    }

    private Cart createCart(int userId) {
        String sql = "INSERT INTO panier (utilisateur_id) VALUES (?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Cart cart = new Cart(userId);
                cart.setId(rs.getInt("id"));
                return cart;
            }
        } catch (Exception e) {
            System.err.println("CartDAO.createCart : " + e.getMessage());
        }
        return null;
    }

    // ── Lignes du panier ──────────────────────────────
    public List<CartLine> getLines(int panierId) {
        String sql = "SELECT * FROM ligne_panier WHERE panier_id = ?";
        List<CartLine> lines = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, panierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CartLine l = new CartLine();
                l.setId(rs.getInt("id"));
                l.setPanierId(rs.getInt("panier_id"));
                l.setProduitId(rs.getInt("produit_id"));
                l.setTailleId(rs.getInt("taille_produit_id"));
                l.setQuantite(rs.getInt("quantite"));
                l.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                lines.add(l);
            }
        } catch (Exception e) {
            System.err.println("CartDAO.getLines : " + e.getMessage());
        }
        return lines;
    }

    // ── Ajouter une ligne ─────────────────────────────
    public CartLine addLine(int panierId, int produitId,
                            int tailleId, int qte, double prix) {
        // Vérifier si la ligne existe déjà
        String checkSql = """
            SELECT id, quantite FROM ligne_panier
            WHERE panier_id=? AND taille_produit_id=?
            """;
        try (Connection conn = getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setInt(1, panierId);
            check.setInt(2, tailleId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                // Ligne existe → incrémenter la quantité
                int newQte = rs.getInt("quantite") + qte;
                int ligneId = rs.getInt("id");
                updateLine(ligneId, newQte);
                CartLine l = new CartLine();
                l.setId(ligneId);
                l.setQuantite(newQte);
                l.setPrixUnitaire(prix);
                return l;
            }
        } catch (Exception e) {
            System.err.println("CartDAO.addLine check : " + e.getMessage());
        }

        // Nouvelle ligne
        String sql = """
            INSERT INTO ligne_panier
                (panier_id, produit_id, taille_produit_id, quantite, prix_unitaire)
            VALUES (?,?,?,?,?) RETURNING id
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, panierId);
            ps.setInt(2, produitId);
            ps.setInt(3, tailleId);
            ps.setInt(4, qte);
            ps.setDouble(5, prix);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CartLine l = new CartLine(panierId, produitId, tailleId, qte, prix);
                l.setId(rs.getInt("id"));
                return l;
            }
        } catch (Exception e) {
            System.err.println("CartDAO.addLine insert : " + e.getMessage());
        }
        return null;
    }

    // ── Modifier la quantité d'une ligne ──────────────
    public boolean updateLine(int ligneId, int newQte) {
        String sql = "UPDATE ligne_panier SET quantite=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQte);
            ps.setInt(2, ligneId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("CartDAO.updateLine : " + e.getMessage());
            return false;
        }
    }

    // ── Supprimer une ligne ───────────────────────────
    public boolean removeLine(int ligneId) {
        String sql = "DELETE FROM ligne_panier WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ligneId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("CartDAO.removeLine : " + e.getMessage());
            return false;
        }
    }

    // ── Vider le panier ───────────────────────────────
    public boolean clearCart(int panierId) {
        String sql = "DELETE FROM ligne_panier WHERE panier_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, panierId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("CartDAO.clearCart : " + e.getMessage());
            return false;
        }
    }
}