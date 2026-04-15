package com.chrionline.client.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class UdpFloodProtectionService {
    private static final int MAX_PACKETS_PER_IP = 15;
    private static final int MAX_GLOBAL_PACKETS = 40;
    private static final Duration WINDOW = Duration.ofSeconds(1);
    private static final UdpFloodProtectionService INSTANCE = new UdpFloodProtectionService();

    private final ConcurrentMap<String, PacketBucket> ipBuckets = new ConcurrentHashMap<>();
    private final PacketBucket globalBucket = new PacketBucket(MAX_GLOBAL_PACKETS);

    private UdpFloodProtectionService() {
    }

    public static UdpFloodProtectionService getInstance() {
        return INSTANCE;
    }

    public boolean allowPacket(String ipAddress) {
        String normalizedIp = (ipAddress == null || ipAddress.isBlank()) ? "unknown-ip" : ipAddress.trim();
        Instant now = Instant.now();
        PacketBucket ipBucket = ipBuckets.computeIfAbsent(normalizedIp, ignored -> new PacketBucket(MAX_PACKETS_PER_IP));
        return globalBucket.allow(now) && ipBucket.allow(now);
    }

    private static final class PacketBucket {
        private final int maxPackets;
        private final Deque<Instant> packets = new ArrayDeque<>();

        private PacketBucket(int maxPackets) {
            this.maxPackets = maxPackets;
        }

        private synchronized boolean allow(Instant now) {
            Instant threshold = now.minus(WINDOW);
            while (!packets.isEmpty() && packets.peekFirst().isBefore(threshold)) {
                packets.removeFirst();
            }
            if (packets.size() >= maxPackets) {
                return false;
            }
            packets.addLast(now);
            return true;
        }
    }
}
