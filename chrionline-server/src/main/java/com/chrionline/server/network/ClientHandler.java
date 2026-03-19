package com.chrionline.server.network;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.handler.*;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;

    // ── Handlers (un par domaine) ─────────────────────
    private final AuthHandler    authHandler    = new AuthHandler();
    private final ProductHandler productHandler = new ProductHandler();
    private final CartHandler    cartHandler    = new CartHandler();
    private final OrderHandler   orderHandler   = new OrderHandler();
    private final AdminHandler   adminHandler   = new AdminHandler();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientIp = socket.getInetAddress().getHostAddress();

        try (
                // IMPORTANT : ObjectOutputStream AVANT ObjectInputStream
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())
        ) {
            Message request;
            while ((request = (Message) in.readObject()) != null) {
                System.out.println("[" + clientIp + "] → " + request.getType());
                Message response = route(request);
                out.writeObject(response);
                out.flush();
                System.out.println("[" + clientIp + "] ← " + response.getStatus());
            }

        } catch (EOFException e) {
            System.out.println("Client déconnecté : " + clientIp);
        } catch (Exception e) {
            System.err.println("Erreur avec " + clientIp + " : " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    // ── Routeur — dirige vers le bon handler ──────────
    private Message route(Message req) {
        return switch (req.getType()) {

            case Protocol.LOGIN,
                 Protocol.REGISTER,
                 Protocol.LOGOUT
                    -> authHandler.handle(req);

            case Protocol.GET_PRODUCTS,
                 Protocol.GET_PRODUCT,
                 Protocol.GET_CATEGORIES,
                 Protocol.GET_PRODUCTS_BY_CATEGORIE
                    -> productHandler.handle(req);

            case Protocol.GET_CART,
                 Protocol.ADD_TO_CART,
                 Protocol.REMOVE_FROM_CART,
                 Protocol.UPDATE_CART,
                 Protocol.CLEAR_CART
                    -> cartHandler.handle(req);

            case Protocol.PLACE_ORDER,
                 Protocol.GET_ORDERS,
                 Protocol.GET_ORDER
                    -> orderHandler.handle(req);

            case Protocol.ADMIN_ADD_PRODUCT,
                 Protocol.ADMIN_UPDATE_PRODUCT,
                 Protocol.ADMIN_DELETE_PRODUCT,
                 Protocol.ADMIN_ADD_SIZE,
                 Protocol.ADMIN_DELETE_SIZE,
                 Protocol.ADMIN_GET_SIZES,
                 Protocol.ADMIN_ADD_GUIDE,
                 Protocol.ADMIN_DELETE_GUIDE,
                 Protocol.ADMIN_GET_GUIDE,
                 Protocol.ADMIN_UPDATE_STOCK,
                 Protocol.ADMIN_GET_USERS,
                 Protocol.ADMIN_SUSPEND_USER,
                 Protocol.ADMIN_ACTIVATE_USER,
                 Protocol.ADMIN_UPDATE_USER,
                 Protocol.ADMIN_GET_ORDERS,
                 Protocol.ADMIN_GET_ORDER_DETAIL,
                 Protocol.ADMIN_UPDATE_ORDER_STATUT,
                 Protocol.ADMIN_GET_PAYMENT,
                 Protocol.ADMIN_REMBOURSE
                    -> adminHandler.handle(req);

            default -> Message.error("Type inconnu : " + req.getType());
        };
    }
}