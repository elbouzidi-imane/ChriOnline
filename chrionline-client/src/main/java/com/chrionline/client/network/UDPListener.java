package com.chrionline.client.network;

import com.chrionline.client.util.UIUtils;
import javafx.application.Platform;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UDPListener implements Runnable {
    private volatile boolean running = true;
    private final DatagramSocket socket;
    private static volatile int boundPort;

    public UDPListener() throws SocketException {
        socket = new DatagramSocket(0);
        boundPort = socket.getLocalPort();
        System.out.println("UDP listener demarre sur le port " + boundPort);
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                Platform.runLater(() -> UIUtils.showInfo(message));
            }
        } catch (SocketException e) {
            if (running) {
                System.err.println("Erreur UDP : " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Erreur UDP : " + e.getMessage());
        }
    }

    public void stopListening() {
        running = false;
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    public static int getBoundPort() {
        return boundPort;
    }
}
