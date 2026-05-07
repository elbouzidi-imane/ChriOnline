package com.chrionline.server.security;

import com.chrionline.common.CryptoAES;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class DatabaseCryptoService {

    private static final DatabaseCryptoService INSTANCE = new DatabaseCryptoService();
    private static final String PREFIX = "ENC:";
    private static final String AES_ALGORITHM = "AES";

    private final CryptoAES aes = new CryptoAES();

    private DatabaseCryptoService() {
        try {
            byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(secret().getBytes(StandardCharsets.UTF_8));
            aes.setCle(new SecretKeySpec(key, AES_ALGORITHM));
        } catch (Exception e) {
            throw new IllegalStateException("Initialisation chiffrement BDD impossible", e);
        }
    }

    public static DatabaseCryptoService getInstance() {
        return INSTANCE;
    }

    public String encryptNullable(String value) throws Exception {
        if (value == null || value.isBlank() || isEncrypted(value)) {
            return value;
        }
        return PREFIX + aes.chiffrerBase64(value);
    }

    public String decryptNullable(String value) {
        if (value == null || value.isBlank() || !isEncrypted(value)) {
            return value;
        }
        try {
            return aes.dechiffrerBase64(value.substring(PREFIX.length()));
        } catch (Exception e) {
            System.err.println("DatabaseCryptoService.decryptNullable : " + e.getMessage());
            return value;
        }
    }

    private boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private String secret() {
        String property = System.getProperty("chrionline.db.crypto.secret");
        if (property != null && !property.isBlank()) {
            return property;
        }
        String env = System.getenv("CHRIONLINE_DB_CRYPTO_SECRET");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return "change-this-db-crypto-secret";
    }
}
