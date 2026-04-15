package com.chrionline.server.network;

import com.chrionline.common.AppConstants;
import com.chrionline.server.security.ConnectionFloodProtectionService;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final ExecutorService CLIENT_POOL = Executors.newFixedThreadPool(60);

    public static void main(String[] args) {
        System.out.println("=== ChriOnline Server ===");
        System.out.println("Demarrage sur le port " + AppConstants.PORT_TCP + "...");

        try (ServerSocket serverSocket = new ServerSocket(AppConstants.PORT_TCP, 80)) {
            System.out.println("Serveur pret - en attente de clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                ConnectionFloodProtectionService.ConnectionDecision decision =
                        ConnectionFloodProtectionService.getInstance().tryAcquire(clientIp);
                if (!decision.allowed()) {
                    System.err.println("Connexion TCP refusee (anti-flood) : " + clientIp
                            + " | raison=" + decision.reason()
                            + " | retryAfter=" + decision.retryAfterSeconds() + "s");
                    clientSocket.close();
                    continue;
                }
                clientSocket.setSoTimeout(45_000);
                System.out.println("Nouveau client connecte : " + clientIp);
                CLIENT_POOL.submit(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}
