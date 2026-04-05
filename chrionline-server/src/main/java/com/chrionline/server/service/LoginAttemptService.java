package com.chrionline.server.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoginAttemptService {
    private static final Logger LOGGER = LoggerFactory.getLogger("SECURITY_AUTH");
    public static final int CAPTCHA_REQUIRED_AFTER_FAILURES = 2;
    private static final Duration[] BLOCK_SEQUENCE = {
            Duration.ofMinutes(1),
            Duration.ofMinutes(4),
            Duration.ofHours(1)
    };

    private static final int MAX_EMAIL_ATTEMPTS = 5;
    private static final int MAX_IP_ATTEMPTS = 10;
    private static final int MAX_GLOBAL_ATTEMPTS = 30;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private static final LoginAttemptService INSTANCE = new LoginAttemptService();

    private final ConcurrentMap<String, AttemptBucket> emailBuckets = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AttemptBucket> ipBuckets = new ConcurrentHashMap<>();
    private final AttemptBucket globalBucket = new AttemptBucket(MAX_GLOBAL_ATTEMPTS, BLOCK_SEQUENCE);

    private LoginAttemptService() {
    }

    public static LoginAttemptService getInstance() {
        return INSTANCE;
    }

    public RateLimitDecision check(String email, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedIp = normalizeIp(ipAddress);
        Instant now = Instant.now();

        AttemptBucket emailBucket = emailBuckets.computeIfAbsent(
                normalizedEmail,
                ignored -> new AttemptBucket(MAX_EMAIL_ATTEMPTS, BLOCK_SEQUENCE)
        );
        AttemptBucket ipBucket = ipBuckets.computeIfAbsent(
                normalizedIp,
                ignored -> new AttemptBucket(MAX_IP_ATTEMPTS, BLOCK_SEQUENCE)
        );

        RateLimitDecision emailDecision = emailBucket.check(now, "email", normalizedEmail);
        if (emailDecision.blocked()) {
            return emailDecision;
        }

        RateLimitDecision ipDecision = ipBucket.check(now, "ip", normalizedIp);
        if (ipDecision.blocked()) {
            return ipDecision;
        }

        return globalBucket.check(now, "global", "global");
    }

    public LoginSecurityState getSecurityState(String email, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedIp = normalizeIp(ipAddress);
        Instant now = Instant.now();

        AttemptBucket emailBucket = emailBuckets.computeIfAbsent(
                normalizedEmail,
                ignored -> new AttemptBucket(MAX_EMAIL_ATTEMPTS, BLOCK_SEQUENCE)
        );
        AttemptBucket ipBucket = ipBuckets.computeIfAbsent(
                normalizedIp,
                ignored -> new AttemptBucket(MAX_IP_ATTEMPTS, BLOCK_SEQUENCE)
        );

        RateLimitDecision decision = emailBucket.check(now, "email", normalizedEmail);
        if (!decision.blocked()) {
            decision = ipBucket.check(now, "ip", normalizedIp);
        }
        if (!decision.blocked()) {
            decision = globalBucket.check(now, "global", "global");
        }

        int emailFailures = emailBucket.getAttemptCount(now);
        return new LoginSecurityState(
                decision,
                emailFailures,
                emailFailures >= CAPTCHA_REQUIRED_AFTER_FAILURES
        );
    }

    public void recordFailure(String email, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedIp = normalizeIp(ipAddress);
        Instant now = Instant.now();

        emailBuckets.computeIfAbsent(normalizedEmail, ignored -> new AttemptBucket(MAX_EMAIL_ATTEMPTS, BLOCK_SEQUENCE))
                .recordFailure(now, "email", normalizedEmail);
        ipBuckets.computeIfAbsent(normalizedIp, ignored -> new AttemptBucket(MAX_IP_ATTEMPTS, BLOCK_SEQUENCE))
                .recordFailure(now, "ip", normalizedIp);
        globalBucket.recordFailure(now, "global", "global");
    }

    public void recordSuccess(String email, String ipAddress) {
        String normalizedEmail = normalizeEmail(email);

        AttemptBucket emailBucket = emailBuckets.get(normalizedEmail);
        if (emailBucket != null) {
            emailBucket.reset();
        }
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

    public record RateLimitDecision(boolean blocked, String scope, String key, Instant blockedUntil) {
        public long retryAfterSeconds() {
            if (blockedUntil == null) {
                return 0;
            }
            return Math.max(0, Duration.between(Instant.now(), blockedUntil).getSeconds());
        }
    }

    public record LoginSecurityState(
            RateLimitDecision decision,
            int emailFailures,
            boolean captchaRequired
    ) {
    }

    private static final class AttemptBucket {
        private final int maxAttempts;
        private final Duration[] blockSequence;
        private final Deque<Instant> attempts = new ArrayDeque<>();
        private Instant blockedUntil;
        private int consecutiveBlocks;

        private AttemptBucket(int maxAttempts, Duration[] blockSequence) {
            this.maxAttempts = maxAttempts;
            this.blockSequence = blockSequence;
        }

        private synchronized RateLimitDecision check(Instant now, String scope, String key) {
            purgeExpired(now);
            if (blockedUntil != null && !blockedUntil.isAfter(now)) {
                clearBlock();
            }
            if (blockedUntil != null && blockedUntil.isAfter(now)) {
                return new RateLimitDecision(true, scope, key, blockedUntil);
            }
            if (attempts.size() >= maxAttempts) {
                consecutiveBlocks++;
                Duration blockDuration = computeBlockDuration();
                blockedUntil = now.plus(blockDuration);
                LOGGER.warn(
                        "Blocage temporaire authentification scope={}, key={}, durationMinutes={}, until={}",
                        scope,
                        key,
                        blockDuration.toMinutes(),
                        blockedUntil
                );
                return new RateLimitDecision(true, scope, key, blockedUntil);
            }
            return new RateLimitDecision(false, scope, key, null);
        }

        private synchronized void recordFailure(Instant now, String scope, String key) {
            purgeExpired(now);
            if (blockedUntil != null && !blockedUntil.isAfter(now)) {
                clearBlock();
            }
            attempts.addLast(now);
            LOGGER.info(
                    "Echec authentification scope={}, key={}, attempts={}/{}",
                    scope,
                    key,
                    attempts.size(),
                    maxAttempts
            );
        }

        private synchronized void reset() {
            attempts.clear();
            blockedUntil = null;
            consecutiveBlocks = 0;
        }

        private synchronized int getAttemptCount(Instant now) {
            purgeExpired(now);
            if (blockedUntil != null && !blockedUntil.isAfter(now)) {
                clearBlock();
            }
            return attempts.size();
        }

        private void purgeExpired(Instant now) {
            Instant threshold = now.minus(WINDOW);
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
                attempts.removeFirst();
            }
        }

        private Duration computeBlockDuration() {
            int index = Math.min(blockSequence.length - 1, Math.max(0, consecutiveBlocks - 1));
            return blockSequence[index];
        }

        private void clearBlock() {
            attempts.clear();
            blockedUntil = null;
        }
    }
}
