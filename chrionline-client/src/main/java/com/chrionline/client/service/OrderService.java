package com.chrionline.client.service;

import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.CancellationConfigDTO;
import com.chrionline.client.model.CancellationResultDTO;
import com.chrionline.client.model.ProductReviewDTO;
import com.chrionline.client.model.PromoValidationResultDTO;
import com.chrionline.client.network.PaymentTlsClient;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.util.JsonUtils;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class OrderService {
    private final TCPClient tcp = TCPClient.getInstance();
    private final PaymentTlsClient paymentTlsClient = new PaymentTlsClient();

    public String placeOrder(String adresse, String modePaiement, String modeLivraison, String promoCode) throws Exception {
        String payload = AppSession.getCurrentUser().getId() + "|" + adresse + "|" + modePaiement + "|" + modeLivraison
                + "|" + (promoCode == null ? "" : promoCode.trim());
        Message response = tcp.send(new Message(Protocol.PLACE_ORDER, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return response.getPayload();
    }

    public List<OrderDTO> getOrders() throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_ORDERS, String.valueOf(AppSession.getCurrentUser().getId())));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        Type listType = new TypeToken<List<OrderDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public OrderDTO getOrderById(int id) throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_ORDER, String.valueOf(id)));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), OrderDTO.class);
    }

    public String pay(String modePaiement, double montant) throws Exception {
        validatePaymentWithTls(modePaiement, montant);
        String payload = AppSession.getCurrentUser().getId() + "|" + modePaiement + "|" + montant;
        Message response = tcp.send(new Message(Protocol.PAY, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return response.getPayload();
    }

    private void validatePaymentWithTls(String modePaiement, double montant) {
        try {
            PaymentTlsClient.PaymentTlsResult result = paymentTlsClient.validatePayment(
                    AppSession.getCurrentUser().getId(),
                    modePaiement,
                    montant
            );
            if (!result.accepted()) {
                throw new IllegalStateException(result.reason());
            }
            System.out.println("[TLS PAYMENT] Validation TLS acceptee : " + result.reference());
        } catch (Exception e) {
            if (PaymentTlsClient.isStrictMode()) {
                throw new IllegalStateException("Paiement TLS refuse : " + e.getMessage(), e);
            }
            System.err.println("[TLS PAYMENT] Validation TLS ignoree (mode non strict) : " + e.getMessage());
        }
    }

    public CancellationConfigDTO getCancellationConfig() throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_CANCELLATION_CONFIG, ""));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), CancellationConfigDTO.class);
    }

    public CancellationResultDTO cancelOrder(int orderId, String reason) throws Exception {
        String payload = AppSession.getCurrentUser().getId() + "|" + orderId + "|" + (reason == null ? "" : reason);
        Message response = tcp.send(new Message(Protocol.CANCEL_ORDER, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), CancellationResultDTO.class);
    }

    public PromoValidationResultDTO applyPromo(double cartTotal, String promoCode) throws Exception {
        String payload = AppSession.getCurrentUser().getId() + "|" + cartTotal + "|" + promoCode;
        Message response = tcp.send(new Message(Protocol.APPLY_PROMO, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), PromoValidationResultDTO.class);
    }

    public ProductReviewDTO addProductReview(int orderId, int productId, int note, String avisTaille, String commentaire) throws Exception {
        String payload = String.join("|",
                String.valueOf(AppSession.getCurrentUser().getId()),
                String.valueOf(orderId),
                String.valueOf(productId),
                String.valueOf(note),
                avisTaille,
                commentaire == null ? "" : commentaire
        );
        Message response = tcp.send(new Message(Protocol.ADD_PRODUCT_REVIEW, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), ProductReviewDTO.class);
    }
}
