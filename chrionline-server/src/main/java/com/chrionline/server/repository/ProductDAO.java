package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.Category;
import com.chrionline.server.model.Product;
import com.chrionline.server.model.ProductSize;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Tous les produits actifs ──────────────────────
    public List<Product> findAll() {
        String sql = "SELECT * FROM produit WHERE statut = 'ACTIF' ORDER BY id";
        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = mapRow(rs);
                p.setTailles(findSizes(p.getId()));
                products.add(p);
            }
        } catch (Exception e) {
            System.err.println("ProductDAO.findAll : " + e.getMessage());
        }
        return products;
    }

    // ── Produit par id ────────────────────────────────
    public Product findById(int id) {
        String sql = "SELECT * FROM produit WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product p = mapRow(rs);
                p.setTailles(findSizes(p.getId()));
                return p;
            }
        } catch (Exception e) {
            System.err.println("ProductDAO.findById : " + e.getMessage());
        }
        return null;
    }

    // ── Produits par catégorie ────────────────────────
    public List<Product> findByCategorie(int categorieId) {
        String sql = "SELECT * FROM produit WHERE categorie_id=? AND statut='ACTIF'";
        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categorieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product p = mapRow(rs);
                p.setTailles(findSizes(p.getId()));
                products.add(p);
            }
        } catch (Exception e) {
            System.err.println("ProductDAO.findByCategorie : " + e.getMessage());
        }
        return products;
    }

    // ── Sauvegarder un produit ────────────────────────
    public Product save(Product p) {
        String sql = """
            INSERT INTO produit
                (categorie_id, nom, description, matiere, couleur,
                 prix_original, prix_reduit, statut, date_debut_vente, image_url)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            RETURNING id
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getCategorieId());
            ps.setString(2, p.getNom());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getMatiere());
            ps.setString(5, p.getCouleur());
            ps.setDouble(6, p.getPrixOriginal());
            ps.setDouble(7, p.getPrixReduit());
            ps.setString(8, p.getStatut());
            ps.setDate(9, p.getDateDebutVente() != null
                    ? new java.sql.Date(p.getDateDebutVente().getTime()) : null);
            ps.setString(10, p.getImageUrl());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) p.setId(rs.getInt("id"));
            return p;
        } catch (Exception e) {
            System.err.println("ProductDAO.save : " + e.getMessage());
            return null;
        }
    }

    // ── Mettre à jour un produit ──────────────────────
    public boolean update(Product p) {
        String sql = """
            UPDATE produit SET
                nom=?, description=?, matiere=?, couleur=?,
                prix_original=?, prix_reduit=?, image_url=?
            WHERE id=?
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getMatiere());
            ps.setString(4, p.getCouleur());
            ps.setDouble(5, p.getPrixOriginal());
            ps.setDouble(6, p.getPrixReduit());
            ps.setString(7, p.getImageUrl());
            ps.setInt(8, p.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ProductDAO.update : " + e.getMessage());
            return false;
        }
    }

    // ── Désactiver un produit (jamais supprimer) ──────
    public boolean deactivate(int id) {
        String sql = "UPDATE produit SET statut = 'INACTIF' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ProductDAO.deactivate : " + e.getMessage());
            return false;
        }
    }

    // ── Tailles d'un produit ──────────────────────────
    public List<ProductSize> findSizes(int produitId) {
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
            System.err.println("ProductDAO.findSizes : " + e.getMessage());
        }
        return sizes;
    }

    // ── Mettre à jour le stock d'une taille ───────────
    public boolean updateStock(int tailleId, int newStock) {
        String sizeSql = "UPDATE taille_produit SET stock = ? WHERE id = ?";
        String productSql = """
            UPDATE produit
            SET stock = (
                SELECT COALESCE(SUM(stock), 0)
                FROM taille_produit
                WHERE produit_id = (
                    SELECT produit_id FROM taille_produit WHERE id = ?
                )
            )
            WHERE id = (
                SELECT produit_id FROM taille_produit WHERE id = ?
            )
            """;
        try (Connection conn = getConnection();
             PreparedStatement sizePs = conn.prepareStatement(sizeSql);
             PreparedStatement productPs = conn.prepareStatement(productSql)) {
            conn.setAutoCommit(false);

            sizePs.setInt(1, newStock);
            sizePs.setInt(2, tailleId);
            boolean updated = sizePs.executeUpdate() > 0;
            if (!updated) {
                conn.rollback();
                return false;
            }

            productPs.setInt(1, tailleId);
            productPs.setInt(2, tailleId);
            productPs.executeUpdate();
            conn.commit();
            return true;
        } catch (Exception e) {
            System.err.println("ProductDAO.updateStock : " + e.getMessage());
            return false;
        }
    }

    // ── Mapper ResultSet → Product ────────────────────
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategorieId(rs.getInt("categorie_id"));
        p.setNom(rs.getString("nom"));
        p.setDescription(rs.getString("description"));
        p.setMatiere(rs.getString("matiere"));
        p.setCouleur(rs.getString("couleur"));
        p.setPrixOriginal(rs.getDouble("prix_original"));
        p.setPrixReduit(rs.getDouble("prix_reduit"));
        p.setStock(rs.getInt("stock"));
        p.setStatut(rs.getString("statut"));
        p.setNombreVentes(rs.getInt("nombre_ventes"));
        p.setImageUrl(rs.getString("image_url"));
        return p;
    }
}
