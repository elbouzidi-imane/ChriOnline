package com.chrionline.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class UdpNotificationSecurity {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String SEPARATOR = "|";

    private UdpNotificationSecurity() {
    }

    public static String sign(String message) {
        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        String encodedMessage = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value(message).getBytes(StandardCharsets.UTF_8));
        String signature = hmac(encodedMessage, timestamp, nonce);
        return timestamp + SEPARATOR + nonce + SEPARATOR + encodedMessage + SEPARATOR + signature;
    }

    public static VerificationResult verify(String packetPayload) {
        if (packetPayload == null || packetPayload.isBlank()) {
            return VerificationResult.rejected("Paquet UDP vide");
        }
        String[] parts = packetPayload.split("\\|", -1);
        if (parts.length != 4) {
            return VerificationResult.rejected("Format UDP signe invalide");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            return VerificationResult.rejected("Timestamp UDP invalide");
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > AppConstants.REQUEST_SIGNATURE_WINDOW_MILLIS) {
            return VerificationResult.rejected("Timestamp UDP expire");
        }
        String expected = hmac(parts[2], timestamp, parts[1]);
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                parts[3].getBytes(StandardCharsets.UTF_8))) {
            return VerificationResult.rejected("Signature UDP invalide");
        }
        try {
            String message = new String(Base64.getUrlDecoder().decode(parts[2]), StandardCharsets.UTF_8);
            return VerificationResult.accepted(parts[1], message);
        } catch (IllegalArgumentException e) {
            return VerificationResult.rejected("Message UDP invalide");
        }
    }

    private static String hmac(String encodedMessage, long timestamp, String nonce) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(RequestSignature.secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            String canonical = "UDP\n" + value(encodedMessage) + "\n" + timestamp + "\n" + value(nonce);
            byte[] digest = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de calculer la signature UDP", e);
        }
    }

    private static String generateNonce() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String value(String input) {
        return input == null ? "" : input;
    }

    public static final class VerificationResult {
        private final boolean accepted;
        private final String reason;
        private final String nonce;
        private final String message;

        private VerificationResult(boolean accepted, String reason, String nonce, String message) {
            this.accepted = accepted;
            this.reason = reason;
            this.nonce = nonce;
            this.message = message;
        }

        public static VerificationResult accepted(String nonce, String message) {
            return new VerificationResult(true, "", nonce, message);
        }

        public static VerificationResult rejected(String reason) {
            return new VerificationResult(false, reason, "", "");
        }

        public boolean isAccepted() {
            return accepted;
        }

        public String getReason() {
            return reason;
        }

        public String getNonce() {
            return nonce;
        }

        public String getMessage() {
            return message;
        }
    }
}
