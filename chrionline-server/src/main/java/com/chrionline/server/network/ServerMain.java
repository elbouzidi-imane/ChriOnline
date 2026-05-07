package com.chrionline.server.network;

import com.chrionline.common.AppConstants;
import com.chrionline.common.CryptoRSA;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerMain {

    public static void main(String[] args) {
        System.out.println("=== ChriOnline Server ===");
        System.out.println("Demarrage sur le port " + AppConstants.PORT_TCP + "...");
        CryptoRSA cryptoRSA = new CryptoRSA();
        try {
            cryptoRSA.genererCles();
            System.out.println("Cles RSA generees avec succes");
        } catch (Exception e) {
            System.err.println("Generation RSA impossible : " + e.getMessage());
            return;
        }

        ThreadPoolExecutor clientExecutor = new ThreadPoolExecutor(
                AppConstants.SERVER_WORKER_THREADS,
                AppConstants.SERVER_WORKER_THREADS,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(AppConstants.SERVER_QUEUE_CAPACITY),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("client-handler-" + thread.getId());
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );

        try (ServerSocket serverSocket = new ServerSocket(
                AppConstants.PORT_TCP,
                AppConstants.TCP_ACCEPT_BACKLOG
        )) {
            System.out.println("Serveur pret - en attente de clients...");
            System.out.println("Protection connexion : backlog=" + AppConstants.TCP_ACCEPT_BACKLOG
                    + ", threads=" + AppConstants.SERVER_WORKER_THREADS
                    + ", fileAttente=" + AppConstants.SERVER_QUEUE_CAPACITY
                    + ", timeoutClientMs=" + AppConstants.CLIENT_SOCKET_TIMEOUT_MILLIS);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(AppConstants.CLIENT_SOCKET_TIMEOUT_MILLIS);
                System.out.println("Nouveau client connecte : "
                        + clientSocket.getInetAddress().getHostAddress());
                try {
                    clientExecutor.execute(new ClientHandler(clientSocket, cryptoRSA));
                } catch (RejectedExecutionException e) {
                    System.err.println("Connexion rejetee : serveur sature pour "
                            + clientSocket.getInetAddress().getHostAddress());
                    closeQuietly(clientSocket);
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        } finally {
            clientExecutor.shutdown();
        }
    }

    private static void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }
}
