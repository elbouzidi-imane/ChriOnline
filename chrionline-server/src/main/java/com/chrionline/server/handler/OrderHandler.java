package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.Order;
import com.chrionline.server.service.OrderService;
import com.google.gson.Gson;

import java.util.List;

public class OrderHandler {

    private final OrderService orderService = new OrderService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.PLACE_ORDER -> handlePlaceOrder(request);
            case Protocol.GET_ORDERS  -> handleGetOrders(request);
            case Protocol.GET_ORDER   -> handleGetOrder(request);
            case Protocol.PAY -> handlePay(request);
            default -> Message.error("Type non géré par OrderHandler");
        };
    }

    // ── PLACE_ORDER ───────────────────────────────────
    // payload : "userId|adresse|modePaiement|modeLivraison"
    private Message handlePlaceOrder(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 4) {
                return Message.error(
                        "Format invalide. Attendu : userId|adresse|modePaiement|modeLivraison");
            }

            int    userId       = Integer.parseInt(parts[0].trim());
            String adresse      = parts[1].trim();
            String modePaiement = parts[2].trim();
            String modeLivraison = parts[3].trim();

            if (adresse.isEmpty()) {
                return Message.error("Adresse de livraison obligatoire");
            }

            Order order = orderService.validerCommande(
                    userId, adresse, modePaiement, modeLivraison);

            if (order == null) {
                return Message.error(
                        "Impossible de valider la commande (panier vide ou stock insuffisant)");
            }

            return Message.ok(Protocol.PLACE_ORDER, order.getReference());

        } catch (Exception e) {
            return Message.error("Erreur validation commande : " + e.getMessage());
        }
    }

    // ── GET_ORDERS ────────────────────────────────────
    // payload : "userId"
    private Message handleGetOrders(Message req) {
        try {
            int userId = Integer.parseInt(req.getPayload().trim());
            List<Order> orders = orderService.getOrdersByUser(userId);
            return Message.ok(Protocol.GET_ORDERS, gson.toJson(orders));
        } catch (Exception e) {
            return Message.error("Erreur récupération commandes : " + e.getMessage());
        }
    }

    // ── GET_ORDER ─────────────────────────────────────
    // payload : "orderId"
    private Message handleGetOrder(Message req) {
        try {
            int orderId = Integer.parseInt(req.getPayload().trim());
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Message.error("Commande introuvable");
            }
            return Message.ok(Protocol.GET_ORDER, gson.toJson(order));
        } catch (Exception e) {
            return Message.error("Erreur récupération commande : " + e.getMessage());
        }
    }
    // payload : "userId|modePaiement|montant"
    private Message handlePay(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 3) return Message.error("Format invalide");
            int    userId       = Integer.parseInt(parts[0].trim());
            String modePaiement = parts[1].trim();
            double montant      = Double.parseDouble(parts[2].trim());
            // Paiement simulé — toujours accepté
            return Message.ok(Protocol.PAY,
                    "Paiement de " + montant + " € accepté via " + modePaiement);
        } catch (Exception e) {
            return Message.error("Erreur paiement : " + e.getMessage());
        }
    }
    

}