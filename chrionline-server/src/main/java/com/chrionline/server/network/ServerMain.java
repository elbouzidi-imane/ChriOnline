package com.chrionline.server.network;
import com.chrionline.common.AppConstants;
import java.net.*;
public class ServerMain {
    public static void main(String[] args) {
        System.out.println("ChriOnline Server sur port " + AppConstants.PORT_TCP);
        try (ServerSocket ss = new ServerSocket(AppConstants.PORT_TCP)) {
            while (true) {
                Socket client = ss.accept();
                System.out.println("Client connecté : " + client.getInetAddress());
                new Thread(new ClientHandler(client)).start();
            }
        } catch (Exception e) { System.err.println("Erreur : " + e.getMessage()); }
    }
}
