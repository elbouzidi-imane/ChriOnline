package com.chrionline.client.service;

import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.UserDTO;
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

    private Message sendAdmin(String type, String data) throws Exception {
        Message response = tcp.send(new Message(type, AppSession.getCurrentUser().getId() + ":" + data));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return response;
    }
}
