package com.chrionline.server.service;

import com.chrionline.server.model.Category;
import com.chrionline.server.model.Product;
import com.chrionline.server.repository.CategoryDAO;
import com.chrionline.server.repository.ProductDAO;

import java.util.Date;
import java.util.List;

public class ProductService {

    private final ProductDAO  productDAO  = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ── Tous les produits actifs ──────────────────────
    public List<Product> getAllProducts() {
        List<Product> products = productDAO.findAll();

        for (Product p : products) {
            Category c = categoryDAO.findById(p.getCategorieId());
            if (c != null) {
                p.setCategorie(c);
            }
        }

        return products;
    }

    // ── Produit par id ────────────────────────────────
    public Product getProductById(int id) {
        Product p = productDAO.findById(id);

        if (p != null) {
            Category c = categoryDAO.findById(p.getCategorieId());
            if (c != null) {
                p.setCategorie(c);
            }
        }

        return p;
    }

    // ── Produits par catégorie ────────────────────────
    public List<Product> getProductsByCategorie(int categorieId) {
        List<Product> products = productDAO.findByCategorie(categorieId);

        for (Product p : products) {
            Category c = categoryDAO.findById(p.getCategorieId());
            if (c != null) {
                p.setCategorie(c);
            }
        }

        return products;
    }

    // ── Toutes les catégories ─────────────────────────
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    // ── Admin : ajouter un produit ────────────────────
    public Product addProduct(Product product) {
        if (product.getNom() == null || product.getNom().isEmpty()) {
            return null;
        }
        if (product.getPrixOriginal() <= 0) {
            return null;
        }
        if (product.getStatut() == null || product.getStatut().isBlank()) {
            product.setStatut("ACTIF");
        }
        if (product.getDateDebutVente() == null) {
            product.setDateDebutVente(new Date());
        }
        return productDAO.save(product);
    }

    // ── Admin : modifier un produit ───────────────────
    public boolean updateProduct(Product product) {
        if (product.getId() <= 0) return false;
        return productDAO.update(product);
    }

    // ── Admin : désactiver un produit ─────────────────
    public boolean deleteProduct(int id) {
        return productDAO.deactivate(id);
    }

    // ── Admin : mettre à jour le stock ────────────────
    public boolean updateStock(int tailleId, int newStock) {
        if (newStock < 0) return false;
        return productDAO.updateStock(tailleId, newStock);
    }
}
