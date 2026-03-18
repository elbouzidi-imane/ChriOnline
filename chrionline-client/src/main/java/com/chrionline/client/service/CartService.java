package com.chrionline.client.service;

import com.chrionline.client.model.CartDTO;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.util.JsonUtils;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;

public class CartService {
    private final TCPClient tcp = TCPClient.getInstance();

    public CartDTO getCart() throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_CART, String.valueOf(AppSession.getCurrentUser().getId())));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        CartDTO cart = JsonUtils.GSON.fromJson(response.getPayload(), CartDTO.class);
        AppSession.setCurrentCart(cart);
        return cart;
    }

    public CartDTO addToCart(int produitId, int tailleId, int qte) throws Exception {
        String payload = AppSession.getCurrentUser().getId() + "|" + produitId + "|" + tailleId + "|" + qte;
        Message response = tcp.send(new Message(Protocol.ADD_TO_CART, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        CartDTO cart = JsonUtils.GSON.fromJson(response.getPayload(), CartDTO.class);
        AppSession.setCurrentCart(cart);
        return cart;
    }

    public CartDTO removeFromCart(int ligneId) throws Exception {
        String payload = AppSession.getCurrentUser().getId() + "|" + ligneId;
        Message response = tcp.send(new Message(Protocol.REMOVE_FROM_CART, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        CartDTO cart = JsonUtils.GSON.fromJson(response.getPayload(), CartDTO.class);
        AppSession.setCurrentCart(cart);
        return cart;
    }

    public CartDTO updateCart(int ligneId, int quantite) throws Exception {
        String payload = AppSession.getCurrentUser().getId() + "|" + ligneId + "|" + quantite;
        Message response = tcp.send(new Message(Protocol.UPDATE_CART, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        CartDTO cart = JsonUtils.GSON.fromJson(response.getPayload(), CartDTO.class);
        AppSession.setCurrentCart(cart);
        return cart;
    }

    public void clearCart() throws Exception {
        Message response = tcp.send(new Message(Protocol.CLEAR_CART, String.valueOf(AppSession.getCurrentUser().getId())));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        AppSession.setCurrentCart(new CartDTO());
    }
}
