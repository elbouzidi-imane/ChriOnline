package com.chrionline.server.network;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.handler.AdminHandler;
import com.chrionline.server.handler.AuthHandler;
import com.chrionline.server.handler.CartHandler;
import com.chrionline.server.handler.OrderHandler;
import com.chrionline.server.handler.ProductHandler;
import com.chrionline.server.security.RequestReplayGuardService;
import com.chrionline.server.security.SensitiveActionOtpService;
import com.chrionline.server.security.SessionSecurityService;
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
    private final RequestReplayGuardService replayGuard = RequestReplayGuardService.getInstance();
    private final SessionSecurityService sessionSecurity = SessionSecurityService.getInstance();
    private final SensitiveActionOtpService sensitiveOtp = SensitiveActionOtpService.getInstance();
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
            socket.setSoTimeout(0);
            Message request;
            while ((request = (Message) in.readObject()) != null) {
                System.out.println("[" + clientIp + "] -> " + request.getType());
                Message response = verifyAndRoute(request);
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

    private Message verifyAndRoute(Message req) {
        RequestReplayGuardService.VerificationResult result = replayGuard.verify(req);
        if (!result.isAccepted()) {
            System.err.println("Requete rejetee avant routage : type=" + req.getType()
                    + ", raison=" + result.getReason());
            return Message.error("Requete rejetee : " + result.getReason());
        }
        SessionSecurityService.SessionRecord session = null;
        if (requiresSession(req.getType())) {
            SessionSecurityService.SessionValidationResult sessionResult =
                    sessionSecurity.validateAndRotate(req.getSessionToken());
            if (!sessionResult.isAccepted()) {
                return Message.error(sessionResult.getReason());
            }
            session = sessionResult.getSession();
            if (isAdminRequest(req.getType()) && !session.admin()) {
                return Message.error("Acces admin refuse");
            }
            if (requiresSensitiveOtp(req.getType())
                    && !sensitiveOtp.verifyOrSend(session.email(), req.getSensitiveOtp(), req.getType())) {
                Message otpRequired = Message.error("OTP_ACTION_REQUIRED: Un code OTP a ete envoye pour confirmer " + req.getType());
                otpRequired.setSessionToken(session.token());
                return otpRequired;
            }
        }
        Message response = route(req);
        if (session != null && response != null && !response.isError()) {
            if (Protocol.LOGOUT.equals(req.getType())) {
                sessionSecurity.invalidate(session.token());
            } else {
                response.setSessionToken(session.token());
            }
        }
        return response;
    }

    private boolean requiresSession(String type) {
        return !switch (type) {
            case Protocol.LOGIN,
                 Protocol.GET_LOGIN_CAPTCHA,
                 Protocol.VERIFY_LOGIN_OTP,
                 Protocol.RESEND_LOGIN_OTP,
                 Protocol.REGISTER,
                 Protocol.VERIFY_EMAIL,
                 Protocol.RESEND_OTP,
                 Protocol.FORGOT_PASSWORD,
                 Protocol.VERIFY_RESET_OTP,
                 Protocol.RESET_PASSWORD,
                 Protocol.GET_PRODUCTS,
                 Protocol.GET_PRODUCT,
                 Protocol.GET_CATEGORIES,
                 Protocol.GET_PRODUCTS_BY_CATEGORIE,
                 Protocol.GET_PRODUCT_REVIEWS -> true;
            default -> false;
        };
    }

    private boolean isAdminRequest(String type) {
        return type != null && type.startsWith("ADMIN_");
    }

    private boolean requiresSensitiveOtp(String type) {
        return switch (type) {
            case Protocol.DELETE_ACCOUNT,
                 Protocol.DEACTIVATE_ACCOUNT,
                 Protocol.UPDATE_PROFILE,
                 Protocol.PLACE_ORDER,
                 Protocol.PAY,
                 Protocol.CANCEL_ORDER,
                 Protocol.ADMIN_DELETE_PRODUCT,
                 Protocol.ADMIN_SUSPEND_USER,
                 Protocol.ADMIN_ACTIVATE_USER,
                 Protocol.ADMIN_UPDATE_USER,
                 Protocol.ADMIN_UPDATE_ORDER_STATUT,
                 Protocol.ADMIN_UPDATE_CANCELLATION_CONFIG,
                 Protocol.ADMIN_REMBOURSE,
                 Protocol.ADMIN_TOGGLE_PROMO -> true;
            default -> false;
        };
    }

    private Message route(Message req) {
        return switch (req.getType()) {
            case Protocol.LOGIN,
                 Protocol.GET_LOGIN_CAPTCHA,
                 Protocol.VERIFY_LOGIN_OTP,
                 Protocol.RESEND_LOGIN_OTP,
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
                if (user != null && user.has("id")) {
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
        if (Protocol.VERIFY_LOGIN_OTP.equals(request.getType())) {
            try {
                JsonObject user = JsonParser.parseString(response.getPayload()).getAsJsonObject();
                if (user != null && user.has("id")) {
                    int newUserId = user.get("id").getAsInt();
                    if (currentUserId > 0 && currentUserId != newUserId) {
                        ConnectedClientRegistry.unregisterUser(currentUserId);
                    }
                    currentUserId = newUserId;
                    String role = user.has("role") && !user.get("role").isJsonNull()
                            ? user.get("role").getAsString()
                            : "";
                    currentUserAdmin = "ADMIN".equalsIgnoreCase(role);
                    com.chrionline.server.model.User sessionUser = new com.chrionline.server.model.User();
                    sessionUser.setId(currentUserId);
                    sessionUser.setEmail(user.has("email") && !user.get("email").isJsonNull()
                            ? user.get("email").getAsString()
                            : "");
                    sessionUser.setRole(role);
                    response.setSessionToken(sessionSecurity.create(sessionUser).token());
                }
            } catch (Exception e) {
                System.err.println("ClientHandler.updateConnectionRegistry otp : " + e.getMessage());
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
            sessionSecurity.invalidate(request.getSessionToken());
            ConnectedClientRegistry.unregisterUser(currentUserId);
            currentUserId = 0;
            currentUserAdmin = false;
        }
    }
}
