package com.chrionline.common;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class CryptoAES {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";//chiffre message et ajouter une protection
    private static final int AES_KEY_BITS = 256;
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecretKey cleAES;

    public void genererCle() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance(AES_ALGORITHM);
        generator.init(AES_KEY_BITS);
        this.cleAES = generator.generateKey();
    }

    public void setCle(SecretKey cle) {
        this.cleAES = cle;
    }

    public void setCleDepuisBytes(byte[] cleBytes) {
        this.cleAES = new SecretKeySpec(cleBytes, AES_ALGORITHM);
    }

    public SecretKey getCle() {
        return cleAES;
    }

    public byte[] chiffrer(String message) throws Exception {
        ensureKey();
        byte[] iv = new byte[GCM_IV_BYTES];
        SECURE_RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, cleAES, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] chiffre = cipher.doFinal(value(message).getBytes(StandardCharsets.UTF_8));

        byte[] resultat = new byte[iv.length + chiffre.length];
        System.arraycopy(iv, 0, resultat, 0, iv.length);
        System.arraycopy(chiffre, 0, resultat, iv.length, chiffre.length);
        return resultat;
    }

    public String dechiffrer(byte[] donnees) throws Exception {
        ensureKey();
        if (donnees == null || donnees.length <= GCM_IV_BYTES) {
            throw new IllegalArgumentException("Donnees AES invalides");
        }
        byte[] iv = Arrays.copyOfRange(donnees, 0, GCM_IV_BYTES);
        byte[] chiffre = Arrays.copyOfRange(donnees, GCM_IV_BYTES, donnees.length);

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, cleAES, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return new String(cipher.doFinal(chiffre), StandardCharsets.UTF_8);
    }

    public String chiffrerBase64(String message) throws Exception {
        return Base64.getEncoder().encodeToString(chiffrer(message));
    }

    public String dechiffrerBase64(String messageChiffre) throws Exception {
        return dechiffrer(Base64.getDecoder().decode(messageChiffre));
    }

    private void ensureKey() {
        if (cleAES == null) {
            throw new IllegalStateException("Cle AES non initialisee");
        }
    }

    private String value(String input) {
        return input == null ? "" : input;
    }
}
