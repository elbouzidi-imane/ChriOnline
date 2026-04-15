package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.CancellationResult;
import com.chrionline.server.model.Order;
import com.chrionline.server.model.ProductReview;
import com.chrionline.server.model.PromoValidationResult;
import com.chrionline.server.service.OrderService;
import com.chrionline.server.service.PaymentReplayGuardService;
import com.chrionline.server.service.PromoService;
import com.chrionline.server.service.ReviewService;
import com.google.gson.Gson;

import java.util.List;

public class OrderHandler {

    private final OrderService orderService = new OrderService();
    private final PromoService promoService = new PromoService();
    private final ReviewService reviewService = new ReviewService();
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
            case Protocol.ADD_PRODUCT_REVIEW -> handleAddProductReview(request);
            default -> Message.error("Type non gere par OrderHandler");
        };
    }

    private Message handlePlaceOrder(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 4) {
                return Message.error("Format invalide. Attendu : userId|adresse|modePaiement|modeLivraison|promoCode|nonce|timestamp");
            }

            int userId = Integer.parseInt(parts[0].trim());
            String adresse = parts[1].trim();
            String modePaiement = parts[2].trim();
            String modeLivraison = parts[3].trim();
            String promoCode = parts.length > 4 ? parts[4].trim() : "";
            String nonce = parts.length > 5 ? parts[5].trim() : "";
            long timestamp = parts.length > 6 ? Long.parseLong(parts[6].trim()) : 0L;

            if (adresse.isEmpty()) {
                return Message.error("Adresse de livraison obligatoire");
            }
            String replayError = PaymentReplayGuardService.validate(nonce, timestamp);
            if (replayError != null) {
                return Message.error(replayError);
            }
            String fingerprint = orderService.buildReplayFingerprint(userId, adresse, modePaiement, modeLivraison, promoCode);
            String duplicateError = PaymentReplayGuardService.validateBusinessFingerprint(fingerprint, timestamp);
            if (duplicateError != null) {
                return Message.error(duplicateError);
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
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 2) {
                return Message.error("Format invalide");
            }
            int userId = Integer.parseInt(parts[0].trim());
            int orderId = Integer.parseInt(parts[1].trim());
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Message.error("Commande introuvable");
            }
            if (order.getUtilisateurId() != userId) {
                return Message.error("Acces refuse a cette commande");
            }
            return Message.ok(Protocol.GET_ORDER, gson.toJson(order));
        } catch (Exception e) {
            return Message.error("Erreur recuperation commande : " + e.getMessage());
        }
    }

    private Message handlePay(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 5) {
                return Message.error("Format invalide");
            }
            int userId = Integer.parseInt(parts[0].trim());
            String modePaiement = parts[1].trim();
            double montant = Double.parseDouble(parts[2].trim());
            String nonce = parts[3].trim();
            long timestamp = Long.parseLong(parts[4].trim());
            String replayError = PaymentReplayGuardService.validate(nonce, timestamp);
            if (replayError != null) {
                return Message.error(replayError);
            }
            String fingerprint = userId + "|" + modePaiement.toUpperCase() + "|" + String.format("%.2f", montant);
            String duplicateError = PaymentReplayGuardService.validateBusinessFingerprint(fingerprint, timestamp);
            if (duplicateError != null) {
                return Message.error(duplicateError);
            }
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

    private Message handleAddProductReview(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 6) {
                return Message.error("Format invalide");
            }
            int userId = Integer.parseInt(parts[0].trim());
            int orderId = Integer.parseInt(parts[1].trim());
            int productId = Integer.parseInt(parts[2].trim());
            int note = Integer.parseInt(parts[3].trim());
            String avisTaille = parts[4].trim();
            String commentaire = parts[5].trim();

            ProductReview review = reviewService.addReview(userId, orderId, productId, note, commentaire, avisTaille);
            if (review == null) {
                return Message.error("Impossible d'ajouter l'avis. Verifiez la commande, le produit ou un doublon.");
            }
            return Message.ok(Protocol.ADD_PRODUCT_REVIEW, gson.toJson(review));
        } catch (Exception e) {
            return Message.error("Erreur ajout avis : " + e.getMessage());
        }
    }
}
