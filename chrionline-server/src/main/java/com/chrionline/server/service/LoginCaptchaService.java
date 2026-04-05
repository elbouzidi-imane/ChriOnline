package com.chrionline.server.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

public final class LoginCaptchaService {
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    private static final LoginCaptchaService INSTANCE = new LoginCaptchaService();

    private final ConcurrentMap<String, CaptchaChallenge> challenges = new ConcurrentHashMap<>();

    private LoginCaptchaService() {
    }

    public static LoginCaptchaService getInstance() {
        return INSTANCE;
    }

    public CaptchaPayload createChallenge(String email, String ipAddress) {
        purgeExpired();
        String captchaId = UUID.randomUUID().toString();
        int left = ThreadLocalRandom.current().nextInt(2, 10);
        int right = ThreadLocalRandom.current().nextInt(1, 10);
        String answer = Integer.toString(left + right);
        challenges.put(captchaId, new CaptchaChallenge(
                captchaId,
                normalizeEmail(email),
                normalizeIp(ipAddress),
                answer,
                Instant.now().plus(CAPTCHA_TTL)
        ));
        return new CaptchaPayload(captchaId, "Combien font " + left + " + " + right + " ?");
    }

    public boolean validate(String email, String ipAddress, String captchaId, String captchaAnswer) {
        purgeExpired();
        if (captchaId == null || captchaId.isBlank() || captchaAnswer == null || captchaAnswer.isBlank()) {
            return false;
        }
        CaptchaChallenge challenge = challenges.remove(captchaId.trim());
        if (challenge == null) {
            return false;
        }
        if (challenge.expiresAt().isBefore(Instant.now())) {
            return false;
        }
        return challenge.email().equals(normalizeEmail(email))
                && challenge.ipAddress().equals(normalizeIp(ipAddress))
                && challenge.answer().equals(captchaAnswer.trim());
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown-email";
        }
        return email.trim().toLowerCase();
    }

    private String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "unknown-ip";
        }
        return ipAddress.trim();
    }

    private record CaptchaChallenge(
            String captchaId,
            String email,
            String ipAddress,
            String answer,
            Instant expiresAt
    ) {
    }

    public record CaptchaPayload(String captchaId, String challengeText) {
    }
}
