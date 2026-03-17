package com.chrionline.server.service;

import com.chrionline.server.model.Cart;
import com.chrionline.server.model.CartLine;
import com.chrionline.server.model.Product;
import com.chrionline.server.model.ProductSize;
import com.chrionline.server.repository.CartDAO;
import com.chrionline.server.repository.ProductDAO;

import java.util.List;

public class CartService {

    private final CartDAO    cartDAO    = new CartDAO();
    private final ProductDAO productDAO = new ProductDAO();

    // ── Récupérer le panier ───────────────────────────
    public Cart getCart(int userId) {
        return cartDAO.getOrCreateCart(userId);
    }

    // ── Ajouter un article ────────────────────────────
    public String addToCart(int userId, int produitId, int tailleId, int quantite) {

        // Vérifier que le produit existe et est actif
        Product product = productDAO.findById(produitId);
        if (product == null || !product.isDisponible()) {
            return "Produit indisponible";
        }

        // Vérifier que la taille existe et a assez de stock
        ProductSize taille = null;
        for (ProductSize t : product.getTailles()) {
            if (t.getId() == tailleId) {
                taille = t;
                break;
            }
        }

        if (taille == null) {
            return "Taille introuvable";
        }

        if (taille.getStock() < quantite) {
            return "Stock insuffisant (disponible : " + taille.getStock() + ")";
        }

        // Récupérer ou créer le panier
        Cart cart = cartDAO.getOrCreateCart(userId);
        if (cart == null) return "Erreur création panier";

        // Ajouter la ligne
        double prix = product.getPrixAffiche();
        CartLine line = cartDAO.addLine(cart.getId(), produitId, tailleId, quantite, prix);

        if (line == null) return "Erreur ajout au panier";
        return null; // null = succès
    }

    // ── Retirer un article ────────────────────────────
    public boolean removeFromCart(int ligneId) {
        return cartDAO.removeLine(ligneId);
    }

    // ── Modifier la quantité ──────────────────────────
    public String updateQuantite(int userId, int ligneId, int newQuantite) {
        if (newQuantite <= 0) {
            cartDAO.removeLine(ligneId);
            return null;
        }

        // Vérifier le stock disponible
        Cart cart = cartDAO.getOrCreateCart(userId);
        for (CartLine line : cart.getLignes()) {
            if (line.getId() == ligneId) {
                Product product = productDAO.findById(line.getProduitId());
                if (product != null) {
                    for (ProductSize t : product.getTailles()) {
                        if (t.getId() == line.getTailleId()) {
                            if (t.getStock() < newQuantite) {
                                return "Stock insuffisant (disponible : "
                                        + t.getStock() + ")";
                            }
                        }
                    }
                }
                break;
            }
        }

        cartDAO.updateLine(ligneId, newQuantite);
        return null; // null = succès
    }

    // ── Vider le panier ───────────────────────────────
    public boolean clearCart(int userId) {
        Cart cart = cartDAO.getOrCreateCart(userId);
        if (cart == null) return false;
        return cartDAO.clearCart(cart.getId());
    }

    // ── Lignes du panier ──────────────────────────────
    public List<CartLine> getCartLines(int userId) {
        Cart cart = cartDAO.getOrCreateCart(userId);
        if (cart == null) return new java.util.ArrayList<>();
        return cart.getLignes();
    }
}