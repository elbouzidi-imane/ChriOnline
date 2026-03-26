package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.CancellationResult;
import com.chrionline.server.model.Order;
import com.chrionline.server.model.PromoValidationResult;
import com.chrionline.server.service.OrderService;
import com.chrionline.server.service.PromoService;
import com.google.gson.Gson;

import java.util.List;

public class OrderHandler {

    private final OrderService orderService = new OrderService();
    private final PromoService promoService = new PromoService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.PLACE_ORDER -> handlePlaceOrder(request);
            case Protocol.GET_ORDERS -> handleGetOrders(request);
            case Protocol.GET_ORDER -> handleGetOrder(request);
            case Protocol.PAY -> handlePay(request);
            case Protocol.CANCEL_ORDER -> handleCancelOrder(request);
            case Protocol.GET_CANCELLATION_CONFIG -> handleGetCancellationConfig();
            case Protocol.APPLY_PROMO -> handleApplyPromo(request);
            default -> Message.error("Type non gere par OrderHandler");
        };
    }

    private Message handlePlaceOrder(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 4) {
                return Message.error("Format invalide. Attendu : userId|adresse|modePaiement|modeLivraison|promoCode");
            }

            int userId = Integer.parseInt(parts[0].trim());
            String adresse = parts[1].trim();
            String modePaiement = parts[2].trim();
            String modeLivraison = parts[3].trim();
            String promoCode = parts.length > 4 ? parts[4].trim() : "";

            if (adresse.isEmpty()) {
                return Message.error("Adresse de livraison obligatoire");
            }

            Order order = orderService.validerCommande(userId, adresse, modePaiement, modeLivraison, promoCode);
            if (order == null) {
                return Message.error("Impossible de valider la commande (panier vide, promo invalide ou stock insuffisant)");
            }

            return Message.ok(Protocol.PLACE_ORDER, order.getReference());
        } catch (Exception e) {
            return Message.error("Erreur validation commande : " + e.getMessage());
        }
    }

    private Message handleGetOrders(Message req) {
        try {
            int userId = Integer.parseInt(req.getPayload().trim());
            List<Order> orders = orderService.getOrdersByUser(userId);
            return Message.ok(Protocol.GET_ORDERS, gson.toJson(orders));
        } catch (Exception e) {
            return Message.error("Erreur recuperation commandes : " + e.getMessage());
        }
    }

    private Message handleGetOrder(Message req) {
        try {
            int orderId = Integer.parseInt(req.getPayload().trim());
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Message.error("Commande introuvable");
            }
            return Message.ok(Protocol.GET_ORDER, gson.toJson(order));
        } catch (Exception e) {
            return Message.error("Erreur recuperation commande : " + e.getMessage());
        }
    }

    private Message handlePay(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 3) {
                return Message.error("Format invalide");
            }
            String modePaiement = parts[1].trim();
            double montant = Double.parseDouble(parts[2].trim());
            return Message.ok(Protocol.PAY, "Paiement de " + montant + " EUR accepte via " + modePaiement);
        } catch (Exception e) {
            return Message.error("Erreur paiement : " + e.getMessage());
        }
    }

    private Message handleCancelOrder(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 2) {
                return Message.error("Format invalide");
            }
            int userId = Integer.parseInt(parts[0].trim());
            int orderId = Integer.parseInt(parts[1].trim());
            String reason = parts.length > 2 ? parts[2].trim() : "";

            CancellationResult result = orderService.cancelOrder(userId, orderId, reason);
            if (result == null) {
                return Message.error("Annulation impossible pour cette commande.");
            }
            return Message.ok(Protocol.CANCEL_ORDER, gson.toJson(result));
        } catch (Exception e) {
            return Message.error("Erreur annulation commande : " + e.getMessage());
        }
    }

    private Message handleGetCancellationConfig() {
        return Message.ok(Protocol.GET_CANCELLATION_CONFIG, gson.toJson(orderService.getCancellationConfig()));
    }

    private Message handleApplyPromo(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 3) {
                return Message.error("Format invalide");
            }
            int userId = Integer.parseInt(parts[0].trim());
            double cartTotal = Double.parseDouble(parts[1].trim());
            String code = parts[2].trim();

            PromoValidationResult result = promoService.validatePromo(code, cartTotal, userId);
            if (!result.isValid()) {
                return Message.error(result.getMessage());
            }
            return Message.ok(Protocol.APPLY_PROMO, gson.toJson(result));
        } catch (Exception e) {
            return Message.error("Erreur promo : " + e.getMessage());
        }
    }
}
