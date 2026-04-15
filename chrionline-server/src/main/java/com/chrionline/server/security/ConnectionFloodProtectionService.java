package com.chrionline.server.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConnectionFloodProtectionService {
    private static final int MAX_GLOBAL_CONNECTIONS = 120;
    private static final int MAX_CONNECTIONS_PER_IP = 12;
    private static final int MAX_CONNECTION_ATTEMPTS_PER_IP = 25;
    private static final Duration ATTEMPT_WINDOW = Duration.ofSeconds(10);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(2);
    private static final ConnectionFloodProtectionService INSTANCE = new ConnectionFloodProtectionService();

    private final AtomicInteger globalConnections = new AtomicInteger();
    private final Map<String, AtomicInteger> ipConnections = new ConcurrentHashMap<>();
    private final Map<String, AttemptBucket> ipAttemptBuckets = new ConcurrentHashMap<>();
    private final Map<String, Instant> blockedIps = new ConcurrentHashMap<>();

    private ConnectionFloodProtectionService() {
    }

    public static ConnectionFloodProtectionService getInstance() {
        return INSTANCE;
    }

    public ConnectionDecision tryAcquire(String ipAddress) {
        String normalizedIp = normalizeIp(ipAddress);
        Instant now = Instant.now();
        purgeExpiredBlocks(now);

        Instant blockedUntil = blockedIps.get(normalizedIp);
        if (blockedUntil != null && blockedUntil.isAfter(now)) {
            return ConnectionDecision.deny(blockedUntil, "IP temporairement bloquee apres surcharge TCP");
        }

        AttemptBucket attemptBucket = ipAttemptBuckets.computeIfAbsent(normalizedIp, ignored -> new AttemptBucket());
        if (!attemptBucket.allow(now)) {
            Instant newBlockedUntil = now.plus(BLOCK_DURATION);
            blockedIps.put(normalizedIp, newBlockedUntil);
            return ConnectionDecision.deny(newBlockedUntil, "Trop de tentatives TCP sur une courte periode");
        }

        AtomicInteger ipCount = ipConnections.computeIfAbsent(normalizedIp, ignored -> new AtomicInteger());
        int newGlobal = globalConnections.incrementAndGet();
        int newIp = ipCount.incrementAndGet();
        if (newGlobal > MAX_GLOBAL_CONNECTIONS || newIp > MAX_CONNECTIONS_PER_IP) {
            release(normalizedIp);
            Instant newBlockedUntil = now.plus(BLOCK_DURATION);
            blockedIps.put(normalizedIp, newBlockedUntil);
            return ConnectionDecision.deny(newBlockedUntil, "Nombre maximal de connexions simultanees depasse");
        }
        return ConnectionDecision.allow();
    }

    public void release(String ipAddress) {
        String normalizedIp = normalizeIp(ipAddress);
        globalConnections.updateAndGet(value -> Math.max(0, value - 1));
        AtomicInteger ipCount = ipConnections.get(normalizedIp);
        if (ipCount == null) {
            return;
        }
        int remaining = ipCount.updateAndGet(value -> Math.max(0, value - 1));
        if (remaining == 0) {
            ipConnections.remove(normalizedIp, ipCount);
        }
    }

    private void purgeExpiredBlocks(Instant now) {
        blockedIps.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }

    private String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "unknown-ip";
        }
        return ipAddress.trim();
    }

    public record ConnectionDecision(boolean allowed, Instant blockedUntil, String reason) {
        public static ConnectionDecision allow() {
            return new ConnectionDecision(true, null, "");
        }

        public static ConnectionDecision deny(Instant blockedUntil, String reason) {
            return new ConnectionDecision(false, blockedUntil, reason);
        }

        public long retryAfterSeconds() {
            if (blockedUntil == null) {
                return 0L;
            }
            return Math.max(0L, Duration.between(Instant.now(), blockedUntil).getSeconds());
        }
    }

    private static final class AttemptBucket {
        private final Deque<Instant> attempts = new ArrayDeque<>();

        private synchronized boolean allow(Instant now) {
            Instant threshold = now.minus(ATTEMPT_WINDOW);
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
                attempts.removeFirst();
            }
            if (attempts.size() >= MAX_CONNECTION_ATTEMPTS_PER_IP) {
                return false;
            }
            attempts.addLast(now);
            return true;
        }
    }
}
