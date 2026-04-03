package com.chrionline.server.network;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.handler.AdminHandler;
import com.chrionline.server.handler.AuthHandler;
import com.chrionline.server.handler.CartHandler;
import com.chrionline.server.handler.OrderHandler;
import com.chrionline.server.handler.ProductHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuthHandler authHandler = new AuthHandler();
    private final ProductHandler productHandler = new ProductHandler();
    private final CartHandler cartHandler = new CartHandler();
    private final OrderHandler orderHandler = new OrderHandler();
    private final AdminHandler adminHandler = new AdminHandler();
    private int currentUserId;
    private boolean currentUserAdmin;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientIp = socket.getInetAddress().getHostAddress();

        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Message request;
            while ((request = (Message) in.readObject()) != null) {
                System.out.println("[" + clientIp + "] -> " + request.getType());
                Message response = route(request);
                updateConnectionRegistry(request, response, clientIp);
                out.writeObject(response);
                out.flush();
                System.out.println("[" + clientIp + "] <- " + response.getStatus());
            }
        } catch (EOFException e) {
            System.out.println("Client deconnecte : " + clientIp);
        } catch (Exception e) {
            System.err.println("Erreur avec " + clientIp + " : " + e.getMessage());
        } finally {
            ConnectedClientRegistry.unregisterUser(currentUserId);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private Message route(Message req) {
        return switch (req.getType()) {
            case Protocol.LOGIN,
                 Protocol.REGISTER,
                 Protocol.LOGOUT,
                 Protocol.VERIFY_EMAIL,
                 Protocol.RESEND_OTP,
                 Protocol.FORGOT_PASSWORD,
                 Protocol.VERIFY_RESET_OTP,
                 Protocol.RESET_PASSWORD,
                 Protocol.UPDATE_PROFILE,
                 Protocol.UPDATE_NOTIFICATION_PREFERENCE,
                 Protocol.REGISTER_UDP_PORT,
                 Protocol.GET_NOTIFICATIONS,
                 Protocol.MARK_NOTIFICATION_READ,
                 Protocol.DEACTIVATE_ACCOUNT,
                 Protocol.DELETE_ACCOUNT -> authHandler.handle(req, socket.getInetAddress().getHostAddress());

            case Protocol.GET_PRODUCTS,
                 Protocol.GET_PRODUCT,
                 Protocol.GET_CATEGORIES,
                 Protocol.GET_PRODUCTS_BY_CATEGORIE,
                 Protocol.GET_PRODUCT_REVIEWS -> productHandler.handle(req);

            case Protocol.GET_CART,
                 Protocol.ADD_TO_CART,
                 Protocol.REMOVE_FROM_CART,
                 Protocol.UPDATE_CART,
                 Protocol.CLEAR_CART -> cartHandler.handle(req);

            case Protocol.PLACE_ORDER,
                 Protocol.GET_ORDERS,
                 Protocol.GET_ORDER,
                 Protocol.CANCEL_ORDER,
                 Protocol.GET_CANCELLATION_CONFIG,
                 Protocol.APPLY_PROMO,
                 Protocol.ADD_PRODUCT_REVIEW,
                 Protocol.PAY -> orderHandler.handle(req);

            case Protocol.ADMIN_ADD_PRODUCT,
                 Protocol.ADMIN_UPDATE_PRODUCT,
                 Protocol.ADMIN_DELETE_PRODUCT,
                 Protocol.ADMIN_ADD_CATEGORY,
                 Protocol.ADMIN_GET_CATEGORIES,
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
                 Protocol.ADMIN_UPDATE_CANCELLATION_CONFIG,
                 Protocol.ADMIN_GET_CANCELLATION_CONFIG,
                 Protocol.ADMIN_GET_PAYMENT,
                 Protocol.ADMIN_GET_PAYMENTS,
                 Protocol.ADMIN_REMBOURSE,
                 Protocol.ADMIN_ADD_PROMO,
                 Protocol.ADMIN_GET_PROMOS,
                 Protocol.ADMIN_GET_PROMO_STATS,
                 Protocol.ADMIN_TOGGLE_PROMO -> adminHandler.handle(req);

            default -> Message.error("Type inconnu : " + req.getType());
        };
    }

    private void updateConnectionRegistry(Message request, Message response, String clientIp) {
        if (response == null || response.isError()) {
            return;
        }
        if (Protocol.LOGIN.equals(request.getType())) {
            try {
                JsonObject user = JsonParser.parseString(response.getPayload()).getAsJsonObject();
                if (user != null) {
                    int newUserId = user.get("id").getAsInt();
                    if (currentUserId > 0 && currentUserId != newUserId) {
                        ConnectedClientRegistry.unregisterUser(currentUserId);
                    }
                    currentUserId = newUserId;
                    String role = user.has("role") && !user.get("role").isJsonNull()
                            ? user.get("role").getAsString()
                            : "";
                    currentUserAdmin = "ADMIN".equalsIgnoreCase(role);
                }
            } catch (Exception e) {
                System.err.println("ClientHandler.updateConnectionRegistry login : " + e.getMessage());
            }
            return;
        }
        if (Protocol.REGISTER_UDP_PORT.equals(request.getType())) {
            try {
                if (currentUserId <= 0) {
                    System.err.println("ClientHandler.updateConnectionRegistry udp : utilisateur non authentifie");
                    return;
                }
                int udpPort = Integer.parseInt(request.getPayload().trim());
                ConnectedClientRegistry.registerUser(currentUserId, currentUserAdmin, clientIp, udpPort);
            } catch (Exception e) {
                System.err.println("ClientHandler.updateConnectionRegistry udp : " + e.getMessage());
            }
            return;
        }
        if (Protocol.LOGOUT.equals(request.getType())) {
            ConnectedClientRegistry.unregisterUser(currentUserId);
            currentUserId = 0;
            currentUserAdmin = false;
        }
    }
}
