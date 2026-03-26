package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.User;
import com.chrionline.server.service.NotificationService;
import com.chrionline.server.service.UserService;
import com.google.gson.Gson;

public class AuthHandler {

    private final UserService userService = new UserService();
    private final NotificationService notificationService = new NotificationService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.LOGIN -> handleLogin(request);
            case Protocol.REGISTER -> handleRegister(request);
            case Protocol.LOGOUT -> handleLogout(request);
            case Protocol.VERIFY_EMAIL -> handleVerifyEmail(request);
            case Protocol.RESEND_OTP -> handleResendOtp(request);
            case Protocol.FORGOT_PASSWORD -> handleForgotPassword(request);
            case Protocol.VERIFY_RESET_OTP -> handleVerifyResetOtp(request);
            case Protocol.RESET_PASSWORD -> handleResetPassword(request);
            case Protocol.UPDATE_PROFILE -> handleUpdateProfile(request);
            case Protocol.UPDATE_NOTIFICATION_PREFERENCE -> handleUpdateNotificationPreference(request);
            case Protocol.REGISTER_UDP_PORT -> Message.ok(Protocol.REGISTER_UDP_PORT, "Port UDP enregistre");
            case Protocol.GET_NOTIFICATIONS -> handleGetNotifications(request);
            case Protocol.MARK_NOTIFICATION_READ -> handleMarkNotificationRead(request);
            case Protocol.DEACTIVATE_ACCOUNT -> handleDeactivateAccount(request);
            case Protocol.DELETE_ACCOUNT -> handleDeleteAccount(request);
            default -> Message.error("Type non gere par AuthHandler");
        };
    }

    private Message handleLogin(Message req) {
        String payload = req.getPayload();
        if (payload == null || !payload.contains(":")) {
            return Message.error("Format invalide. Attendu : email:motdepasse");
        }
        int separateur = payload.indexOf(":");
        String email = payload.substring(0, separateur).trim();
        String mdp = payload.substring(separateur + 1).trim();
        if (email.isEmpty() || mdp.isEmpty()) {
            return Message.error("Email et mot de passe obligatoires");
        }
        User user = userService.login(email, mdp);
        if (user == null) {
            return Message.error("Identifiants incorrects ou compte suspendu");
        }
        return Message.ok(Protocol.LOGIN, gson.toJson(user));
    }

    private Message handleRegister(Message req) {
        String payload = req.getPayload();
        if (payload == null) return Message.error("Donnees manquantes");

        String[] parts = payload.split("\\|", -1);
        if (parts.length < 4) return Message.error("Format invalide");

        String nom = parts[0].trim();
        String prenom = parts[1].trim();
        String email = parts[2].trim();
        String mdp = parts[3].trim();
        String telephone = parts.length > 4 ? parts[4].trim() : "";
        String adresse = parts.length > 5 ? parts[5].trim() : "";
        String dateNaissance = parts.length > 6 ? parts[6].trim() : "";

        if (nom.isEmpty() || prenom.isEmpty()) return Message.error("Nom et prenom obligatoires");
        if (email.isEmpty() || !email.contains("@")) return Message.error("Email invalide");
        if (mdp.length() < 4) return Message.error("Mot de passe trop court (minimum 4 caracteres)");

        String result = userService.registerWithOtp(
                nom, prenom, email, mdp, telephone, adresse, dateNaissance);

        return switch (result) {
            case "EMAIL_EXISTS" -> Message.error("Email deja utilise");
            case "OTP_SENT" -> Message.ok(Protocol.REGISTER, "Code de confirmation envoye a " + email);
            default -> Message.error("Erreur lors de l'inscription");
        };
    }

    private Message handleLogout(Message req) {
        return Message.ok(Protocol.LOGOUT, "Deconnexion reussie");
    }

    private Message handleVerifyEmail(Message req) {
        String[] parts = req.getPayload().split("\\|");
        if (parts.length < 2) return Message.error("Format invalide");
        String email = parts[0].trim();
        String code = parts[1].trim();
        boolean ok = userService.verifyEmail(email, code);
        if (!ok) return Message.error("Code invalide ou expire");
        return Message.ok(Protocol.VERIFY_EMAIL, "Email confirme ! Vous pouvez vous connecter.");
    }

    private Message handleResendOtp(Message req) {
        String[] parts = req.getPayload().split("\\|");
        if (parts.length < 2) return Message.error("Format invalide");
        String email = parts[0].trim();
        String type = parts[1].trim();
        boolean ok = userService.resendOtp(email, type);
        if (!ok) return Message.error("Email introuvable");
        return Message.ok(Protocol.RESEND_OTP, "Nouveau code envoye a " + email);
    }

    private Message handleForgotPassword(Message req) {
        String email = req.getPayload().trim();
        if (email.isEmpty()) return Message.error("Email obligatoire");
        boolean ok = userService.forgotPassword(email);
        if (!ok) return Message.error("Email introuvable");
        return Message.ok(Protocol.FORGOT_PASSWORD, "Code de reinitialisation envoye a " + email);
    }

    private Message handleVerifyResetOtp(Message req) {
        String[] parts = req.getPayload().split("\\|");
        if (parts.length < 2) return Message.error("Format invalide");
        String email = parts[0].trim();
        String code = parts[1].trim();
        boolean ok = userService.verifyResetOtp(email, code);
        if (!ok) return Message.error("Code invalide ou expire");
        return Message.ok(Protocol.VERIFY_RESET_OTP, "Code verifie - vous pouvez changer votre mot de passe");
    }

    private Message handleResetPassword(Message req) {
        String[] parts = req.getPayload().split("\\|");
        if (parts.length < 2) return Message.error("Format invalide");
        String email = parts[0].trim();
        String nouveauMdp = parts[1].trim();
        if (nouveauMdp.length() < 4) return Message.error("Mot de passe trop court");
        boolean ok = userService.resetPassword(email, nouveauMdp);
        if (!ok) return Message.error("Erreur reinitialisation");
        return Message.ok(Protocol.RESET_PASSWORD, "Mot de passe reinitialise avec succes");
    }

    private Message handleUpdateProfile(Message req) {
        String[] parts = req.getPayload().split("\\|", -1);
        if (parts.length < 5) return Message.error("Format invalide");
        int userId = Integer.parseInt(parts[0].trim());
        String nom = parts[1].trim();
        String prenom = parts[2].trim();
        String telephone = parts[3].trim();
        String adresse = parts[4].trim();
        String dateNaissance = parts.length > 5 ? parts[5].trim() : "";
        User existingUser = userService.getUserById(userId);
        boolean ok = userService.updateProfile(userId, nom, prenom, telephone, adresse, dateNaissance);
        if (!ok) return Message.error("Erreur mise a jour profil");
        if (existingUser != null && !existingUser.isAdmin()) {
            notificationService.notifyAdminsAboutClient(userId, "a mis a jour son profil.");
        }
        return Message.ok(Protocol.UPDATE_PROFILE, "Profil mis a jour");
    }

    private Message handleUpdateNotificationPreference(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 2) return Message.error("Format invalide");
            int userId = Integer.parseInt(parts[0].trim());
            boolean enabled = Boolean.parseBoolean(parts[1].trim());
            boolean ok = userService.updateNotificationPreference(userId, enabled);
            if (!ok) return Message.error("Impossible de mettre a jour les notifications");
            return Message.ok(
                    Protocol.UPDATE_NOTIFICATION_PREFERENCE,
                    enabled ? "Notifications activees" : "Notifications desactivees"
            );
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleDeactivateAccount(Message req) {
        try {
            int userId = Integer.parseInt(req.getPayload().trim());
            User user = userService.getUserById(userId);
            boolean ok = userService.deactivateAccount(userId);
            if (!ok) return Message.error("Compte introuvable");
            if (user != null && !user.isAdmin()) {
                notificationService.notifyAdminsAboutClient(userId, "a desactive son compte.");
            }
            return Message.ok(Protocol.DEACTIVATE_ACCOUNT, "Compte desactive");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleDeleteAccount(Message req) {
        try {
            int userId = Integer.parseInt(req.getPayload().trim());
            User user = userService.getUserById(userId);
            boolean ok = userService.deleteAccount(userId);
            if (!ok) return Message.error("Compte introuvable");
            if (user != null && !user.isAdmin()) {
                notificationService.notifyAdminsAboutClient(userId, "a supprime son compte.");
            }
            return Message.ok(Protocol.DELETE_ACCOUNT, "Compte supprime definitivement");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetNotifications(Message req) {
        try {
            int userId = Integer.parseInt(req.getPayload().trim());
            return Message.ok(Protocol.GET_NOTIFICATIONS, gson.toJson(notificationService.getNotifications(userId)));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleMarkNotificationRead(Message req) {
        try {
            String[] parts = req.getPayload().split("\\|", -1);
            if (parts.length < 2) {
                return Message.error("Format invalide");
            }
            int userId = Integer.parseInt(parts[0].trim());
            int notificationId = Integer.parseInt(parts[1].trim());
            boolean ok = notificationService.markAsRead(userId, notificationId);
            if (!ok) {
                return Message.error("Notification introuvable");
            }
            return Message.ok(Protocol.MARK_NOTIFICATION_READ, "Notification marquee comme lue");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }
}
