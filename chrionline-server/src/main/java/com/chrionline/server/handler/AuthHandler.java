package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.User;
import com.chrionline.server.security.PasswordPolicy;
import com.chrionline.server.security.SessionSecurityService;
import com.chrionline.server.service.LoginCaptchaService;
import com.chrionline.server.service.LoginAttemptService;
import com.chrionline.server.service.LoginTwoFactorService;
import com.chrionline.server.service.NotificationService;
import com.chrionline.server.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("SECURITY_AUTH");
    private static final String GENERIC_LOGIN_ERROR = "Authentification impossible. Verifiez vos identifiants ou reessayez plus tard.";

    private final UserService userService = new UserService();
    private final NotificationService notificationService = new NotificationService();
    private final LoginCaptchaService loginCaptchaService = LoginCaptchaService.getInstance();
    private final LoginAttemptService loginAttemptService = LoginAttemptService.getInstance();
    private final LoginTwoFactorService loginTwoFactorService = LoginTwoFactorService.getInstance();
    private final SessionSecurityService sessionSecurityService = SessionSecurityService.getInstance();
    private final Gson gson = new Gson();

    public Message handle(Message request, String clientIp) {
        return switch (request.getType()) {
            case Protocol.LOGIN -> handleLogin(request, clientIp);
            case Protocol.GET_LOGIN_CAPTCHA -> handleGetLoginCaptcha(request, clientIp);
            case Protocol.VERIFY_LOGIN_OTP -> handleVerifyLoginOtp(request, clientIp);
            case Protocol.RESEND_LOGIN_OTP -> handleResendLoginOtp(request);
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

    private Message handleLogin(Message req, String clientIp) {
        LoginRequest loginRequest = parseLoginRequest(req.getPayload());
        String email = loginRequest.email();
        String mdp = loginRequest.password();
        if (email.isEmpty() || mdp.isEmpty()) {
            return Message.error("Email et mot de passe obligatoires");
        }

        LoginAttemptService.LoginSecurityState securityState = loginAttemptService.getSecurityState(email, clientIp);
        LoginAttemptService.RateLimitDecision decision = securityState.decision();
        if (decision.blocked()) {
            LOGGER.warn(
                    "Tentative de login refusee pour rate limiting scope={}, key={}, ip={}, retryAfter={}s",
                    decision.scope(),
                    decision.key(),
                    clientIp,
                    decision.retryAfterSeconds()
            );
            return Message.error("Trop de tentatives de connexion. Reessayez dans quelques minutes.");
        }
        if (securityState.captchaRequired()
                && !loginCaptchaService.validate(email, clientIp, loginRequest.captchaId(), loginRequest.captchaAnswer())) {
            loginAttemptService.recordFailure(email, clientIp);
            LOGGER.info("Echec verification captcha pour email={}, ip={}", email, clientIp);
            return Message.error("CAPTCHA invalide ou manquant. Un nouveau CAPTCHA est requis.");
        }

        User user = userService.login(email, mdp);
        if (user == null) {
            loginAttemptService.recordFailure(email, clientIp);
            LOGGER.info("Echec de connexion pour email={}, ip={}", email, clientIp);
            return Message.error(GENERIC_LOGIN_ERROR);
        }
        LoginTwoFactorService.LoginChallenge challenge;
        try {
            challenge = loginTwoFactorService.initiate(user);
        } catch (Exception e) {
            LOGGER.error("Envoi OTP de connexion impossible pour email={}, ip={}", email, clientIp, e);
            return Message.error("Impossible d'envoyer le code OTP de connexion.");
        }
        LOGGER.info("OTP de connexion envoye pour email={}, ip={}", email, clientIp);
        return Message.ok(Protocol.LOGIN, gson.toJson(new LoginResponse(true, challenge.pendingToken(), challenge.message(), null)));
    }

    private Message handleGetLoginCaptcha(Message req, String clientIp) {
        String email = req.getPayload() == null ? "" : req.getPayload().trim();
        if (email.isEmpty()) {
            return Message.error("Email obligatoire pour generer le CAPTCHA");
        }
        LoginAttemptService.LoginSecurityState securityState = loginAttemptService.getSecurityState(email, clientIp);
        if (securityState.decision().blocked()) {
            return Message.error("Trop de tentatives de connexion. Reessayez dans quelques minutes.");
        }
        return Message.ok(Protocol.GET_LOGIN_CAPTCHA, gson.toJson(loginCaptchaService.createChallenge(email, clientIp)));
    }

    private Message handleVerifyLoginOtp(Message req, String clientIp) {
        OtpVerificationRequest verificationRequest = parseOtpVerificationRequest(req.getPayload());
        User user = loginTwoFactorService.verify(verificationRequest.pendingToken(), verificationRequest.code());
        if (user == null) {
            LOGGER.info("Echec verification OTP de connexion ip={}", clientIp);
            return Message.error("Code OTP invalide ou expire.");
        }
        user.setSessionToken(sessionSecurityService.createSession(user, clientIp));
        loginAttemptService.recordSuccess(user.getEmail(), clientIp);
        LOGGER.info("Connexion 2FA reussie pour email={}, ip={}", user.getEmail(), clientIp);
        return Message.ok(Protocol.VERIFY_LOGIN_OTP, gson.toJson(user));
    }

    private Message handleResendLoginOtp(Message req) {
        String pendingToken = req.getPayload() == null ? "" : req.getPayload().trim();
        try {
            if (!loginTwoFactorService.resend(pendingToken)) {
                return Message.error("Impossible de renvoyer le code OTP.");
            }
        } catch (Exception e) {
            LOGGER.error("Renvoi OTP de connexion impossible", e);
            return Message.error("Impossible de renvoyer le code OTP.");
        }
        return Message.ok(Protocol.RESEND_LOGIN_OTP, "Un nouveau code OTP a ete envoye par email.");
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
        String passwordError = PasswordPolicy.validate(
                mdp,
                PasswordPolicy.buildForbiddenTerms(email, nom, prenom, dateNaissance)
        );
        if (passwordError != null) return Message.error(passwordError);

        String result = userService.registerWithOtp(
                nom, prenom, email, mdp, telephone, adresse, dateNaissance);

        return switch (result) {
            case "EMAIL_EXISTS" -> Message.error("Email deja utilise");
            case "OTP_SENT" -> Message.ok(Protocol.REGISTER, "Code de confirmation envoye a " + email);
            default -> Message.error("Erreur lors de l'inscription");
        };
    }

    private Message handleLogout(Message req) {
        sessionSecurityService.invalidate(req.getSessionToken());
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
        User user = userService.getUserByEmail(email);
        String passwordError = PasswordPolicy.validate(
                nouveauMdp,
                PasswordPolicy.buildForbiddenTerms(
                        email,
                        user == null ? null : user.getNom(),
                        user == null ? null : user.getPrenom(),
                        user != null && user.getDateNaissance() != null
                                ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(user.getDateNaissance())
                                : null
                )
        );
        if (passwordError != null) return Message.error(passwordError);
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

    private LoginRequest parseLoginRequest(String payload) {
        if (payload == null || payload.isBlank()) {
            return new LoginRequest("", "", "", "");
        }
        if (payload.trim().startsWith("{")) {
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
            return new LoginRequest(
                    getJsonString(json, "email"),
                    getJsonString(json, "password"),
                    getJsonString(json, "captchaId"),
                    getJsonString(json, "captchaAnswer")
            );
        }
        if (!payload.contains(":")) {
            return new LoginRequest("", "", "", "");
        }
        int separator = payload.indexOf(":");
        return new LoginRequest(
                payload.substring(0, separator).trim(),
                payload.substring(separator + 1).trim(),
                "",
                ""
        );
    }

    private String getJsonString(JsonObject json, String property) {
        if (!json.has(property) || json.get(property).isJsonNull()) {
            return "";
        }
        return json.get(property).getAsString().trim();
    }

    private record LoginRequest(String email, String password, String captchaId, String captchaAnswer) {
    }

    private OtpVerificationRequest parseOtpVerificationRequest(String payload) {
        if (payload == null || payload.isBlank()) {
            return new OtpVerificationRequest("", "");
        }
        JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
        return new OtpVerificationRequest(
                getJsonString(json, "pendingToken"),
                getJsonString(json, "code")
        );
    }

    private record OtpVerificationRequest(String pendingToken, String code) {
    }

    private record LoginResponse(boolean requiresOtp, String pendingToken, String message, User user) {
    }
}
