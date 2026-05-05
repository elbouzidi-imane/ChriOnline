package com.chrionline.client.security;

import com.chrionline.common.AppConstants;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpFloodProtectionService {

    private final Map<String, PacketBucket> bucketsByIp = new ConcurrentHashMap<>();
    private volatile long lastCleanupAt;

    public boolean allow(String ipAddress) {
        long now = System.currentTimeMillis();
        cleanupOldBuckets(now);
        PacketBucket bucket = bucketsByIp.compute(ipAddress, (ip, existing) -> {
            if (existing == null || now - existing.windowStart > AppConstants.UDP_RATE_LIMIT_WINDOW_MILLIS) {
                return new PacketBucket(now, 1);
            }
            existing.count++;
            return existing;
        });
        return bucket.count <= AppConstants.UDP_MAX_PACKETS_PER_IP;
    }

    private void cleanupOldBuckets(long now) {
        if (now - lastCleanupAt < AppConstants.UDP_RATE_LIMIT_WINDOW_MILLIS) {
            return;
        }
        lastCleanupAt = now;
        Iterator<Map.Entry<String, PacketBucket>> iterator = bucketsByIp.entrySet().iterator();
        while (iterator.hasNext()) {
            PacketBucket bucket = iterator.next().getValue();
            if (now - bucket.windowStart > AppConstants.UDP_RATE_LIMIT_WINDOW_MILLIS) {
                iterator.remove();
            }
        }
    }

    private static final class PacketBucket {
        private final long windowStart;
        private int count;

        private PacketBucket(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
