package com.chrionline.client.service;

import com.chrionline.client.model.CategoryDTO;
import com.chrionline.client.model.CancellationConfigDTO;
import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.PaymentDTO;
import com.chrionline.client.model.PromoCodeDTO;
import com.chrionline.client.model.PromoUsageStatDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.ProductSizeDTO;
import com.chrionline.client.model.UserDTO;
import com.chrionline.client.model.GuideDTO;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.util.JsonUtils;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AdminService {
    private final TCPClient tcp = TCPClient.getInstance();

    public List<UserDTO> getUsers() throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_USERS, "");
        Type listType = new TypeToken<List<UserDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public List<OrderDTO> getOrders() throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_ORDERS, "");
        Type listType = new TypeToken<List<OrderDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public OrderDTO getOrderDetail(int orderId) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_ORDER_DETAIL, String.valueOf(orderId));
        return JsonUtils.GSON.fromJson(response.getPayload(), OrderDTO.class);
    }

    public ProductDTO addProduct(int categorieId, String nom, String description, String matiere,
                                 String couleur, double prixOriginal, String prixReduit, String imageUrl) throws Exception {
        String payload = categorieId + "|" + nom + "|" + description + "|" + matiere + "|" + couleur + "|"
                + prixOriginal + "|" + prixReduit + "|" + imageUrl;
        Message response = sendAdmin(Protocol.ADMIN_ADD_PRODUCT, payload);
        return JsonUtils.GSON.fromJson(response.getPayload(), ProductDTO.class);
    }

    public void updateProduct(int id, String nom, String description, String matiere,
                              String couleur, double prixOriginal, String prixReduit, String imageUrl) throws Exception {
        String payload = id + "|" + nom + "|" + description + "|" + matiere + "|" + couleur + "|"
                + prixOriginal + "|" + prixReduit + "|" + imageUrl;
        sendAdmin(Protocol.ADMIN_UPDATE_PRODUCT, payload);
    }

    public void deleteProduct(int productId) throws Exception {
        sendAdmin(Protocol.ADMIN_DELETE_PRODUCT, String.valueOf(productId));
    }

    public void suspendUser(int userId) throws Exception {
        sendAdmin(Protocol.ADMIN_SUSPEND_USER, String.valueOf(userId));
    }

    public void activateUser(int userId) throws Exception {
        sendAdmin(Protocol.ADMIN_ACTIVATE_USER, String.valueOf(userId));
    }

    public void updateStock(int tailleId, int newStock) throws Exception {
        sendAdmin(Protocol.ADMIN_UPDATE_STOCK, tailleId + "|" + newStock);
    }

    public ProductSizeDTO addSize(int produitId, String valeur, int stock) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_ADD_SIZE, produitId + "|" + valeur + "|" + stock);
        return JsonUtils.GSON.fromJson(response.getPayload(), ProductSizeDTO.class);
    }

    public void deleteSize(int tailleId) throws Exception {
        sendAdmin(Protocol.ADMIN_DELETE_SIZE, String.valueOf(tailleId));
    }

    public List<ProductSizeDTO> getSizes(int produitId) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_SIZES, String.valueOf(produitId));
        Type listType = new TypeToken<List<ProductSizeDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public GuideDTO addGuide(int produitId, String taille, String poitrine, String tailleCm, String hanches) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_ADD_GUIDE, String.join("|",
                String.valueOf(produitId), taille, poitrine, tailleCm, hanches));
        return JsonUtils.GSON.fromJson(response.getPayload(), GuideDTO.class);
    }

    public void deleteGuide(int guideId) throws Exception {
        sendAdmin(Protocol.ADMIN_DELETE_GUIDE, String.valueOf(guideId));
    }

    public List<GuideDTO> getGuides(int produitId) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_GUIDE, String.valueOf(produitId));
        Type listType = new TypeToken<List<GuideDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public void updateUser(int userId, String nom, String prenom, String telephone, String adresse) throws Exception {
        sendAdmin(Protocol.ADMIN_UPDATE_USER, String.join("|",
                String.valueOf(userId), nom, prenom, telephone, adresse));
    }

    public void updateOrderStatus(int orderId, String newStatus) throws Exception {
        sendAdmin(Protocol.ADMIN_UPDATE_ORDER_STATUT, orderId + "|" + newStatus);
    }

    public PaymentDTO getPayment(int commandeId) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_PAYMENT, String.valueOf(commandeId));
        return JsonUtils.GSON.fromJson(response.getPayload(), PaymentDTO.class);
    }

    public List<PaymentDTO> getPayments() throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_PAYMENTS, "");
        Type listType = new TypeToken<List<PaymentDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public void remboursement(int commandeId) throws Exception {
        sendAdmin(Protocol.ADMIN_REMBOURSE, String.valueOf(commandeId));
    }

    public void processRefundDecision(int commandeId, boolean approved, String detail, String estimatedDelay) throws Exception {
        sendAdmin(
                Protocol.ADMIN_REMBOURSE,
                String.join("|",
                        String.valueOf(commandeId),
                        approved ? "APPROVE" : "REFUSE",
                        detail == null ? "" : detail,
                        estimatedDelay == null ? "" : estimatedDelay)
        );
    }

    public CancellationConfigDTO getCancellationConfig() throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_CANCELLATION_CONFIG, "");
        return JsonUtils.GSON.fromJson(response.getPayload(), CancellationConfigDTO.class);
    }

    public CancellationConfigDTO updateCancellationConfig(String status, boolean reasonRequired,
                                                          boolean automaticRefund, String estimatedDelay) throws Exception {
        Message response = sendAdmin(
                Protocol.ADMIN_UPDATE_CANCELLATION_CONFIG,
                String.join("|",
                        status,
                        String.valueOf(reasonRequired),
                        String.valueOf(automaticRefund),
                        estimatedDelay == null ? "" : estimatedDelay)
        );
        return JsonUtils.GSON.fromJson(response.getPayload(), CancellationConfigDTO.class);
    }

    public CategoryDTO addCategory(String nom, String description) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_ADD_CATEGORY, nom + "|" + (description == null ? "" : description));
        return JsonUtils.GSON.fromJson(response.getPayload(), CategoryDTO.class);
    }

    public List<CategoryDTO> getAdminCategories() throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_CATEGORIES, "");
        Type listType = new TypeToken<List<CategoryDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public void addPromo(String code, String reductionType, double reductionValue, double minimumOrderAmount,
                         String startDate, String endDate, int maxUses, int maxUsagePerUser) throws Exception {
        sendAdmin(
                Protocol.ADMIN_ADD_PROMO,
                String.join("|",
                        code,
                        reductionType,
                        String.valueOf(reductionValue),
                        String.valueOf(minimumOrderAmount),
                        startDate,
                        endDate,
                        String.valueOf(maxUses),
                        String.valueOf(maxUsagePerUser))
        );
    }

    public List<PromoCodeDTO> getPromos() throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_PROMOS, "");
        Type listType = new TypeToken<List<PromoCodeDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public PromoUsageStatDTO getPromoStats(String code) throws Exception {
        Message response = sendAdmin(Protocol.ADMIN_GET_PROMO_STATS, code);
        return JsonUtils.GSON.fromJson(response.getPayload(), PromoUsageStatDTO.class);
    }

    public void togglePromo(int promoId, boolean active) throws Exception {
        sendAdmin(Protocol.ADMIN_TOGGLE_PROMO, promoId + "|" + active);
    }

    private Message sendAdmin(String type, String data) throws Exception {
        Message response = tcp.send(new Message(type, AppSession.getCurrentUser().getId() + ":" + data));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return response;
    }
}
