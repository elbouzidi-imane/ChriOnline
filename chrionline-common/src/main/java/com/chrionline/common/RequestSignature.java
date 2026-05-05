package com.chrionline.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class RequestSignature {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RequestSignature() {
    }

    public static void sign(Message message) {
        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        message.setTimestamp(timestamp);
        message.setNonce(nonce);
        message.setSignature(compute(message.getType(), message.getPayload(), timestamp, nonce, secret()));
    }

    public static boolean isValid(Message message, String secret) {
        if (message == null || isBlank(message.getType()) || isBlank(message.getNonce())
                || isBlank(message.getSignature()) || message.getTimestamp() <= 0) {
            return false;
        }
        String expected = compute(
                message.getType(),
                message.getPayload(),
                message.getTimestamp(),
                message.getNonce(),
                secret
        );
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                message.getSignature().getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String secret() {
        String property = System.getProperty("chrionline.hmac.secret");
        if (!isBlank(property)) {
            return property;
        }
        String env = System.getenv("CHRIONLINE_HMAC_SECRET");
        if (!isBlank(env)) {
            return env;
        }
        return AppConstants.DEFAULT_HMAC_SECRET;
    }

    private static String compute(String type, String payload, long timestamp, String nonce, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(canonical(type, payload, timestamp, nonce).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de calculer la signature HMAC", e);
        }
    }

    private static String canonical(String type, String payload, long timestamp, String nonce) {
        return value(type) + "\n" + value(payload) + "\n" + timestamp + "\n" + value(nonce);
    }

    private static String generateNonce() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String value(String input) {
        return input == null ? "" : input;
    }

    private static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
