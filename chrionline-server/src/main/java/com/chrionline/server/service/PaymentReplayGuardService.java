package com.chrionline.server.service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PaymentReplayGuardService {
    private static final long MAX_AGE_MILLIS = 30_000L;
    private static final ConcurrentHashMap<String, Long> USED_NONCES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> USED_FINGERPRINTS = new ConcurrentHashMap<>();

    private PaymentReplayGuardService() {
    }

    public static String validate(String nonce, long timestamp) {
        long now = System.currentTimeMillis();
        cleanup(now);

        if (nonce == null || nonce.isBlank()) {
            return "Paiement refuse : nonce manquant";
        }
        if (timestamp <= 0L) {
            return "Paiement refuse : timestamp invalide";
        }
        if (Math.abs(now - timestamp) > MAX_AGE_MILLIS) {
            return "Paiement refuse : message trop ancien";
        }
        if (USED_NONCES.putIfAbsent(nonce, timestamp) != null) {
            return "Paiement refuse : rejeu detecte";
        }
        return null;
    }

    public static String validateBusinessFingerprint(String fingerprint, long timestamp) {
        long now = System.currentTimeMillis();
        cleanup(now);

        if (fingerprint == null || fingerprint.isBlank()) {
            return null;
        }
        if (Math.abs(now - timestamp) > MAX_AGE_MILLIS) {
            return "Paiement refuse : message trop ancien";
        }
        if (USED_FINGERPRINTS.putIfAbsent(fingerprint, timestamp) != null) {
            return "Paiement refuse : meme commande/paiement deja recu";
        }
        return null;
    }

    private static void cleanup(long now) {
        Iterator<Map.Entry<String, Long>> iterator = USED_NONCES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() > MAX_AGE_MILLIS) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<String, Long>> fingerprintIterator = USED_FINGERPRINTS.entrySet().iterator();
        while (fingerprintIterator.hasNext()) {
            Map.Entry<String, Long> entry = fingerprintIterator.next();
            if (now - entry.getValue() > MAX_AGE_MILLIS) {
                fingerprintIterator.remove();
            }
        }
    }
}
