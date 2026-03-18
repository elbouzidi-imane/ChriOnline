package com.chrionline.client.network;

import com.chrionline.client.util.UIUtils;
import com.chrionline.common.AppConstants;
import javafx.application.Platform;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UDPListener implements Runnable {
    private volatile boolean running = true;
    private DatagramSocket socket;

    @Override
    public void run() {
        try (DatagramSocket datagramSocket = new DatagramSocket(AppConstants.PORT_UDP)) {
            socket = datagramSocket;
            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);
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
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
