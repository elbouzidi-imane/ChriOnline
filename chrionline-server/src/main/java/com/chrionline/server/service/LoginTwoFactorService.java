package com.chrionline.server.service;

import com.chrionline.server.model.OtpCode;
import com.chrionline.server.model.User;
import com.chrionline.server.repository.OtpDAO;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LoginTwoFactorService {
    private static final String LOGIN_OTP_TYPE = "LOGIN_2FA";
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final LoginTwoFactorService INSTANCE = new LoginTwoFactorService();

    private final ConcurrentMap<String, PendingLogin> pendingLogins = new ConcurrentHashMap<>();
    private final OtpDAO otpDAO = new OtpDAO();
    private final EmailService emailService = EmailService.getInstance();

    private LoginTwoFactorService() {
    }

    public static LoginTwoFactorService getInstance() {
        return INSTANCE;
    }

    public LoginChallenge initiate(User user) {
        purgeExpired();
        String token = UUID.randomUUID().toString();
        PendingLogin pendingLogin = new PendingLogin(user, Instant.now().plus(OTP_TTL));
        pendingLogins.put(token, pendingLogin);
        sendOtp(user.getEmail());
        return new LoginChallenge(token, "Un code OTP a ete envoye a votre adresse email.");
    }

    public User verify(String token, String code) {
        purgeExpired();
        if (token == null || token.isBlank() || code == null || code.isBlank()) {
            return null;
        }
        PendingLogin pendingLogin = pendingLogins.get(token.trim());
        if (pendingLogin == null || pendingLogin.expiresAt().isBefore(Instant.now())) {
            pendingLogins.remove(token.trim());
            return null;
        }
        OtpCode otp = otpDAO.findValid(pendingLogin.user().getEmail(), code.trim(), LOGIN_OTP_TYPE);
        if (otp == null) {
            return null;
        }
        otpDAO.markAsUsed(otp.getId());
        pendingLogins.remove(token.trim());
        return pendingLogin.user();
    }

    public boolean resend(String token) {
        purgeExpired();
        if (token == null || token.isBlank()) {
            return false;
        }
        PendingLogin pendingLogin = pendingLogins.get(token.trim());
        if (pendingLogin == null || pendingLogin.expiresAt().isBefore(Instant.now())) {
            pendingLogins.remove(token.trim());
            return false;
        }
        sendOtp(pendingLogin.user().getEmail());
        return true;
    }

    private void sendOtp(String email) {
        String code = emailService.generateOtp();
        Date expireAt = new Date(System.currentTimeMillis() + OTP_TTL.toMillis());
        otpDAO.save(new OtpCode(email, code, LOGIN_OTP_TYPE, expireAt));
        if (!emailService.sendLoginOtpEmail(email, code)) {
            throw new IllegalStateException("Impossible d'envoyer le code OTP par email.");
        }
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        pendingLogins.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record PendingLogin(User user, Instant expiresAt) {
    }

    public record LoginChallenge(String pendingToken, String message) {
    }
}
