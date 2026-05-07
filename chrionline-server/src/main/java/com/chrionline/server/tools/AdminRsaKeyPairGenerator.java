package com.chrionline.server.tools;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class AdminRsaKeyPairGenerator {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

        System.out.println("=== CLE PUBLIQUE ADMIN (BDD utilisateur.cle_publique_rsa) ===");
        System.out.println(publicKey);
        System.out.println("=== CLE PRIVEE ADMIN (a garder cote admin) ===");
        System.out.println(privateKey);
        System.out.println("=== SQL EXEMPLE ===");
        System.out.println("UPDATE utilisateur SET cle_publique_rsa = '" + publicKey + "' WHERE role = 'ADMIN';");
    }
}
