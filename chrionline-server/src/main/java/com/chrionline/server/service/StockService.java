package com.chrionline.server.service;

import com.chrionline.server.model.Product;
import com.chrionline.server.model.ProductSize;
import com.chrionline.server.repository.ProductDAO;

import java.util.List;

public class StockService {

    private final ProductDAO productDAO = new ProductDAO();

    // ── Mettre à jour le stock d'une taille ───────────
    public boolean updateStock(int tailleId, int newStock) {
        if (newStock < 0) {
            System.err.println("StockService : stock négatif refusé");
            return false;
        }
        return productDAO.updateStock(tailleId, newStock);
    }

    // ── Décrémenter le stock après achat ─────────────
    public boolean decrementerStock(int tailleId, int quantite) {
        // Récupérer tous les produits pour trouver la taille
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            for (ProductSize taille : p.getTailles()) {
                if (taille.getId() == tailleId) {
                    int newStock = taille.getStock() - quantite;
                    if (newStock < 0) {
                        System.err.println("StockService : stock insuffisant");
                        return false;
                    }
                    return productDAO.updateStock(tailleId, newStock);
                }
            }
        }
        System.err.println("StockService : taille introuvable id=" + tailleId);
        return false;
    }

    // ── Incrémenter le stock (annulation commande) ────
    public boolean incrementerStock(int tailleId, int quantite) {
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            for (ProductSize taille : p.getTailles()) {
                if (taille.getId() == tailleId) {
                    int newStock = taille.getStock() + quantite;
                    return productDAO.updateStock(tailleId, newStock);
                }
            }
        }
        System.err.println("StockService : taille introuvable id=" + tailleId);
        return false;
    }

    // ── Vérifier si le stock est suffisant ────────────
    public boolean isStockSuffisant(int tailleId, int quantite) {
        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            for (ProductSize taille : p.getTailles()) {
                if (taille.getId() == tailleId) {
                    return taille.getStock() >= quantite;
                }
            }
        }
        return false;
    }

    // ── Produits avec stock faible (admin) ────────────
    public List<Product> getProduitsStockFaible(int seuil) {
        List<Product> tous = productDAO.findAll();
        List<Product> faible = new java.util.ArrayList<>();
        for (Product p : tous) {
            if (p.getStock() <= seuil) {
                faible.add(p);
            }
        }
        return faible;
    }
}