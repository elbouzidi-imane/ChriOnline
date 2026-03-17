package com.chrionline.server.network;

import com.chrionline.common.AppConstants;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        System.out.println("=== ChriOnline Server ===");
        System.out.println("Démarrage sur le port " + AppConstants.PORT_TCP + "...");

        try (ServerSocket serverSocket = new ServerSocket(AppConstants.PORT_TCP)) {
            System.out.println("Serveur prêt — en attente de clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : "
                        + clientSocket.getInetAddress().getHostAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }
}