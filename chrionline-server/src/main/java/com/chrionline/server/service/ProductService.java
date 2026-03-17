package com.chrionline.server.service;

import com.chrionline.server.model.Category;
import com.chrionline.server.model.Product;
import com.chrionline.server.repository.CategoryDAO;
import com.chrionline.server.repository.ProductDAO;

import java.util.List;

public class ProductService {

    private final ProductDAO  productDAO  = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ── Tous les produits actifs ──────────────────────
    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    // ── Produit par id ────────────────────────────────
    public Product getProductById(int id) {
        return productDAO.findById(id);
    }

    // ── Produits par catégorie ────────────────────────
    public List<Product> getProductsByCategorie(int categorieId) {
        return productDAO.findByCategorie(categorieId);
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