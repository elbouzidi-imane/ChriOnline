package com.chrionline.common;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class RsaSignatureUtils {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String RSA_ALGORITHM = "RSA";

    private RsaSignatureUtils() {
    }

    public static byte[] sign(String challenge, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(value(challenge).getBytes(StandardCharsets.UTF_8));
        return signature.sign();
    }

    public static boolean verify(String challenge, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(value(challenge).getBytes(StandardCharsets.UTF_8));
        return signature.verify(signatureBytes);
    }

    public static PrivateKey privateKeyFromBase64(String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(cleanBase64(privateKeyBase64));
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    public static PublicKey publicKeyFromBase64(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(cleanBase64(publicKeyBase64));
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public static String signBase64(String challenge, String privateKeyBase64) throws Exception {
        return Base64.getEncoder().encodeToString(sign(challenge, privateKeyFromBase64(privateKeyBase64)));
    }

    private static String cleanBase64(String input) {
        return value(input)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private static String value(String input) {
        return input == null ? "" : input;
    }
}
