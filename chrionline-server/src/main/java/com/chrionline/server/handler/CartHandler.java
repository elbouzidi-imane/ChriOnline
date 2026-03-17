package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.Cart;
import com.chrionline.server.model.CartLine;
import com.chrionline.server.service.CartService;
import com.google.gson.Gson;

import java.util.List;

public class CartHandler {

    private final CartService cartService = new CartService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.GET_CART         -> handleGetCart(request);
            case Protocol.ADD_TO_CART      -> handleAddToCart(request);
            case Protocol.REMOVE_FROM_CART -> handleRemoveFromCart(request);
            case Protocol.UPDATE_CART      -> handleUpdateCart(request);
            case Protocol.CLEAR_CART       -> handleClearCart(request);
            default -> Message.error("Type non géré par CartHandler");
        };
    }

    // ── GET_CART ──────────────────────────────────────
    // payload : "userId"
    private Message handleGetCart(Message req) {
        try {
            int userId = Integer.parseInt(req.getPayload().trim());
            Cart cart  = cartService.getCart(userId);
            return Message.ok(Protocol.GET_CART, gson.toJson(cart));
        } catch (Exception e) {
            return Message.error("Erreur récupération panier : " + e.getMessage());
        }
    }

    // ── ADD_TO_CART ───────────────────────────────────
    // payload : "userId|produitId|tailleId|quantite"
    private Message handleAddToCart(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|");
            if (parts.length < 4) {
                return Message.error("Format invalide. Attendu : userId|produitId|tailleId|quantite");
            }
            int userId   = Integer.parseInt(parts[0].trim());
            int produitId = Integer.parseInt(parts[1].trim());
            int tailleId  = Integer.parseInt(parts[2].trim());
            int quantite  = Integer.parseInt(parts[3].trim());

            String erreur = cartService.addToCart(userId, produitId, tailleId, quantite);
            if (erreur != null) {
                return Message.error(erreur);
            }

            // Retourner le panier mis à jour
            Cart cart = cartService.getCart(userId);
            return Message.ok(Protocol.ADD_TO_CART, gson.toJson(cart));

        } catch (Exception e) {
            return Message.error("Erreur ajout panier : " + e.getMessage());
        }
    }

    // ── REMOVE_FROM_CART ──────────────────────────────
    // payload : "userId|ligneId"
    private Message handleRemoveFromCart(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|");
            int userId  = Integer.parseInt(parts[0].trim());
            int ligneId = Integer.parseInt(parts[1].trim());

            boolean ok = cartService.removeFromCart(ligneId);
            if (!ok) return Message.error("Ligne introuvable");

            Cart cart = cartService.getCart(userId);
            return Message.ok(Protocol.REMOVE_FROM_CART, gson.toJson(cart));

        } catch (Exception e) {
            return Message.error("Erreur suppression ligne : " + e.getMessage());
        }
    }

    // ── UPDATE_CART ───────────────────────────────────
    // payload : "userId|ligneId|newQuantite"
    private Message handleUpdateCart(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|");
            int userId      = Integer.parseInt(parts[0].trim());
            int ligneId     = Integer.parseInt(parts[1].trim());
            int newQuantite = Integer.parseInt(parts[2].trim());

            String erreur = cartService.updateQuantite(userId, ligneId, newQuantite);
            if (erreur != null) return Message.error(erreur);

            Cart cart = cartService.getCart(userId);
            return Message.ok(Protocol.UPDATE_CART, gson.toJson(cart));

        } catch (Exception e) {
            return Message.error("Erreur mise à jour panier : " + e.getMessage());
        }
    }

    // ── CLEAR_CART ────────────────────────────────────
    // payload : "userId"
    private Message handleClearCart(Message req) {
        try {
            int userId = Integer.parseInt(req.getPayload().trim());
            cartService.clearCart(userId);
            return Message.ok(Protocol.CLEAR_CART, "Panier vidé");
        } catch (Exception e) {
            return Message.error("Erreur vidage panier : " + e.getMessage());
        }
    }
}