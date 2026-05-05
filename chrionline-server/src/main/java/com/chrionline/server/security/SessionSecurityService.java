package com.chrionline.server.security;

import com.chrionline.common.AppConstants;
import com.chrionline.server.model.User;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionSecurityService {

    private static final SessionSecurityService INSTANCE = new SessionSecurityService();

    private final ConcurrentMap<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    private SessionSecurityService() {
    }

    public static SessionSecurityService getInstance() {
        return INSTANCE;
    }

    public SessionRecord create(User user) {
        purgeExpired();
        String token = UUID.randomUUID().toString();
        SessionRecord record = new SessionRecord(
                token,
                user.getId(),
                user.getEmail(),
                user.isAdmin(),
                Instant.now().plusMillis(AppConstants.SESSION_TTL_MILLIS)
        );
        sessions.put(token, record);
        return record;
    }

    public SessionValidationResult validateAndRotate(String token) {
        purgeExpired();
        if (token == null || token.isBlank()) {
            return SessionValidationResult.rejected("Session manquante");
        }
        SessionRecord record = sessions.remove(token.trim());
        if (record == null) {
            return SessionValidationResult.rejected("Session invalide ou expiree");
        }
        if (record.expiresAt().isBefore(Instant.now())) {
            return SessionValidationResult.rejected("Session expiree");
        }
        String rotatedToken = UUID.randomUUID().toString();
        SessionRecord rotated = new SessionRecord(
                rotatedToken,
                record.userId(),
                record.email(),
                record.admin(),
                Instant.now().plusMillis(AppConstants.SESSION_TTL_MILLIS)
        );
        sessions.put(rotatedToken, rotated);
        return SessionValidationResult.accepted(rotated);
    }

    public void invalidate(String token) {
        if (token != null) {
            sessions.remove(token.trim());
        }
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    public record SessionRecord(String token, int userId, String email, boolean admin, Instant expiresAt) {
    }

    public static final class SessionValidationResult {
        private final boolean accepted;
        private final String reason;
        private final SessionRecord session;

        private SessionValidationResult(boolean accepted, String reason, SessionRecord session) {
            this.accepted = accepted;
            this.reason = reason;
            this.session = session;
        }

        public static SessionValidationResult accepted(SessionRecord session) {
            return new SessionValidationResult(true, "", session);
        }

        public static SessionValidationResult rejected(String reason) {
            return new SessionValidationResult(false, reason, null);
        }

        public boolean isAccepted() {
            return accepted;
        }

        public String getReason() {
            return reason;
        }

        public SessionRecord getSession() {
            return session;
        }
    }
}
