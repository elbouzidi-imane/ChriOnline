package com.chrionline.server.security;

import com.chrionline.server.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionSecurityService {
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(15);
    private static final SessionSecurityService INSTANCE = new SessionSecurityService();

    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    private SessionSecurityService() {
    }

    public static SessionSecurityService getInstance() {
        return INSTANCE;
    }

    public String createSession(User user, String clientIp) {
        purgeExpired();
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur obligatoire");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionRecord(
                token,
                user.getId(),
                user.isAdmin(),
                normalizeIp(clientIp),
                Instant.now().plus(SESSION_TIMEOUT)
        ));
        return token;
    }

    public SessionValidationResult validate(String token, String clientIp) {
        purgeExpired();
        if (token == null || token.isBlank()) {
            return SessionValidationResult.invalid("Session manquante");
        }
        SessionRecord record = sessions.get(token.trim());
        if (record == null) {
            return SessionValidationResult.invalid("Session invalide ou expiree");
        }
        if (!record.ipAddress().equals(normalizeIp(clientIp))) {
            sessions.remove(token.trim());
            return SessionValidationResult.invalid("Session invalide : adresse IP differente");
        }
        SessionRecord refreshed = record.touch(Instant.now().plus(SESSION_TIMEOUT));
        sessions.put(token.trim(), refreshed);
        return SessionValidationResult.valid(refreshed);
    }

    public void invalidate(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        sessions.remove(token.trim());
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown-ip";
        }
        return clientIp.trim();
    }

    private record SessionRecord(
            String token,
            int userId,
            boolean admin,
            String ipAddress,
            Instant expiresAt
    ) {
        private SessionRecord touch(Instant newExpiry) {
            return new SessionRecord(token, userId, admin, ipAddress, newExpiry);
        }
    }

    public record SessionValidationResult(
            boolean valid,
            String message,
            int userId,
            boolean admin
    ) {
        private static SessionValidationResult invalid(String message) {
            return new SessionValidationResult(false, message, 0, false);
        }

        private static SessionValidationResult valid(SessionRecord record) {
            return new SessionValidationResult(true, "", record.userId(), record.admin());
        }
    }
}
