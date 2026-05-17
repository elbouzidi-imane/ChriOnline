package com.chrionline.client.network;

import com.chrionline.common.AppConstants;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

public class PaymentTlsClient {
 //Créer un serveur TLS dédié au paiement.
    public PaymentTlsResult validatePayment(int userId, String modePaiement, double montant) throws Exception {
        //Le client prépare TLS.
        //Il charge son truststore pour vérifier le serveur.
        SSLContext context = buildClientContext();
        SSLSocketFactory factory = context.getSocketFactory();
        //Le client se connecte au serveur TLS :
        try (SSLSocket socket = (SSLSocket) factory.createSocket(AppConstants.HOST, AppConstants.PORT_PAYMENT_TLS)) {
            socket.setSoTimeout(AppConstants.PAYMENT_TLS_TIMEOUT_MILLIS);
            //Le client vérifie le certificat du serveur.//Si le certificat est accepté, la connexion TLS est ouverte.

            socket.startHandshake();
            System.out.println("[TLS PAYMENT] Session TLS etablie avec le serveur paiement");

            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                //Le client envoie les données paiement.
                //Grâce à TLS, elles sont chiffrées sur le réseau.
                out.println("userId=" + userId + "|mode=" + value(modePaiement) + "|amount=" + montant);
                String response = in.readLine();
                if (response == null || response.isBlank()) {
                    return PaymentTlsResult.failed("Reponse TLS paiement absente");
                }
                //Si le serveur répond OK, le paiement est accepté.
                if (response.startsWith("PAYMENT_OK|")) {
                    return PaymentTlsResult.ok(response.substring("PAYMENT_OK|".length()));
                }
                return PaymentTlsResult.failed(response.replace("PAYMENT_REFUSED|", ""));
            }
        }
    }

    private SSLContext buildClientContext() throws Exception {
        String trustStorePath = System.getProperty("chrionline.payment.truststore", AppConstants.PAYMENT_TLS_TRUSTSTORE_PATH);
        String password = System.getProperty("chrionline.payment.truststore.password", AppConstants.PAYMENT_TLS_STORE_PASSWORD);
        //Le client prépare le truststore.
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream input = openTrustStore(trustStorePath)) {
            //Il charge payment-truststore.jks.
            trustStore.load(input, password.toCharArray());
        }
//Le client utilise ce truststore pour vérifier le certificat serveur.
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
//Le client crée sa configuration TLS.
//Il peut maintenant vérifier le serveur.
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagerFactory.getTrustManagers(), null);
        return context;
    }

    private InputStream openTrustStore(String trustStorePath) throws Exception {
        InputStream classpathStore = PaymentTlsClient.class.getResourceAsStream("/tls/payment-truststore.jks");
        if (classpathStore != null) {
            return classpathStore;
        }
        return Files.newInputStream(Path.of(trustStorePath));
    }

    public static boolean isStrictMode() {
        return Boolean.parseBoolean(System.getProperty("chrionline.payment.tls.required", "false"));
    }

    private String value(String input) {
        return input == null ? "" : input;
    }

    public record PaymentTlsResult(boolean accepted, String reference, String reason) {
        public static PaymentTlsResult ok(String reference) {
            return new PaymentTlsResult(true, reference, "");
        }

        public static PaymentTlsResult failed(String reason) {
            return new PaymentTlsResult(false, "", reason);
        }
    }
}
