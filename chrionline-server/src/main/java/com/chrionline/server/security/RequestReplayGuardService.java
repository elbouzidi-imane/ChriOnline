package com.chrionline.server.security;

import com.chrionline.common.AppConstants;
import com.chrionline.common.Message;
import com.chrionline.common.RequestSignature;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestReplayGuardService {

    private static final RequestReplayGuardService INSTANCE = new RequestReplayGuardService();

    private final Map<String, Long> usedNonces = new ConcurrentHashMap<>();
    private volatile long lastCleanupAt;

    private RequestReplayGuardService() {
    }

    public static RequestReplayGuardService getInstance() {
        return INSTANCE;
    }

    public VerificationResult verify(Message request) {
        long now = System.currentTimeMillis();
        cleanupExpiredNonces(now);

        if (Math.abs(now - request.getTimestamp()) > AppConstants.REQUEST_SIGNATURE_WINDOW_MILLIS) {
            return VerificationResult.rejected("Requete expiree");
        }
        if (!RequestSignature.isValid(request, RequestSignature.secret())) {
            return VerificationResult.rejected("Signature HMAC invalide");
        }

        Long previous = usedNonces.putIfAbsent(request.getNonce(), request.getTimestamp());
        if (previous != null) {
            return VerificationResult.rejected("Requete deja utilisee");
        }
        return VerificationResult.accepted();
    }

    private void cleanupExpiredNonces(long now) {
        if (now - lastCleanupAt < AppConstants.REQUEST_SIGNATURE_WINDOW_MILLIS) {
            return;
        }
        lastCleanupAt = now;
        long minTimestamp = now - AppConstants.REQUEST_SIGNATURE_WINDOW_MILLIS;
        Iterator<Map.Entry<String, Long>> iterator = usedNonces.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() < minTimestamp) {
                iterator.remove();
            }
        }
    }

    public static final class VerificationResult {
        private final boolean accepted;
        private final String reason;

        private VerificationResult(boolean accepted, String reason) {
            this.accepted = accepted;
            this.reason = reason;
        }

        public static VerificationResult accepted() {
            return new VerificationResult(true, "");
        }

        public static VerificationResult rejected(String reason) {
            return new VerificationResult(false, reason);
        }

        public boolean isAccepted() {
            return accepted;
        }

        public String getReason() {
            return reason;
        }
    }
}
