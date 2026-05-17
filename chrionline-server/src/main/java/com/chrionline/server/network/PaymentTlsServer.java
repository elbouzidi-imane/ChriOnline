package com.chrionline.server.network;

import com.chrionline.common.AppConstants;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class PaymentTlsServer {

    public static void main(String[] args) {
        try {
            //demmare le port 8443 de TLS
            //Le serveur prépare TLS.
            //Il charge son certificat.
            //Il prépare sa clé privée.
            SSLContext context = buildServerContext();
            //le serveur creer un socket securise pas normal (TLS)
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            try (SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(AppConstants.PORT_PAYMENT_TLS)) {
                System.out.println("[TLS PAYMENT] Serveur TLS paiement demarre sur port " + AppConstants.PORT_PAYMENT_TLS);
                while (true) {
                    //le serveur attend un client paiement se connecter
                    SSLSocket socket = (SSLSocket) serverSocket.accept();
                    Thread thread = new Thread(() -> handlePayment(socket));
                    thread.setName("payment-tls-" + thread.getId());
                    thread.start();
                }
            }
        } catch (Exception e) {
            System.err.println("[TLS PAYMENT] Demarrage impossible : " + e.getMessage());
            System.err.println("[TLS PAYMENT] Verifiez le keystore TLS. Voir resources/tls/README_TLS.md");
        }
    }

    private static void handlePayment(SSLSocket socket) {
        String client = socket.getInetAddress().getHostAddress();
        try (SSLSocket tlsSocket = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(tlsSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(tlsSocket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            //C’est ici que TLS commence vraiment.
            //Le serveur donne son certificat.
            //Le client le vérifie.
            //Après ça, la connexion est chiffrée.
            tlsSocket.startHandshake();
            System.out.println("[TLS PAYMENT] Connexion TLS acceptee : " + client);
            //Le serveur reçoit les données paiement.
            String line = in.readLine();
            if (line == null || line.isBlank()) {
                out.println("PAYMENT_REFUSED|Donnees paiement absentes");
                return;
            }

            Map<String, String> data = parsePayload(line);
            String amount = data.getOrDefault("amount", "");
            String userId = data.getOrDefault("userId", "");
            String mode = data.getOrDefault("mode", "");
            System.out.println("[TLS PAYMENT] Donnees paiement recues : user=" + userId
                    + ", mode=" + mode + ", amount=" + amount);

            if (userId.isBlank() || mode.isBlank() || !isPositiveAmount(amount)) {
                out.println("PAYMENT_REFUSED|Donnees paiement invalides");
                return;
            }
//Si le paiement est accepté, le serveur répond avec une référence.
            out.println("PAYMENT_OK|TXN-TLS-" + System.currentTimeMillis());
            System.out.println("[TLS PAYMENT] Paiement valide");
        } catch (Exception e) {
            System.err.println("[TLS PAYMENT] Erreur client " + client + " : " + e.getMessage());
        }
    }

    private static SSLContext buildServerContext() throws Exception {
        String keyStorePath = System.getProperty("chrionline.payment.keystore", AppConstants.PAYMENT_TLS_KEYSTORE_PATH);
        String password = System.getProperty("chrionline.payment.keystore.password", AppConstants.PAYMENT_TLS_STORE_PASSWORD);
        //On prépare un objet pour lire le keystore.
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (var input = Files.newInputStream(Path.of(keyStorePath))) {
            keyStore.load(input, password.toCharArray());
        }
//On donne à TLS la clé privée et le certificat du serveur.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
//On crée la configuration TLS du serveur.
//Le serveur est prêt à accepter des connexions TLS.
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), null, null);
        return context;
    }

    private static Map<String, String> parsePayload(String payload) {
        Map<String, String> values = new HashMap<>();
        for (String part : payload.split("\\|")) {
            int separator = part.indexOf('=');
            if (separator > 0) {
                values.put(part.substring(0, separator), part.substring(separator + 1));
            }
        }
        return values;
    }

    private static boolean isPositiveAmount(String value) {
        try {
            return Double.parseDouble(value) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
