package com.chrionline.client.network;

import com.chrionline.client.security.UdpFloodProtectionService;
import com.chrionline.client.util.UIUtils;
import com.chrionline.common.UdpNotificationSecurity;
import javafx.application.Platform;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UDPListener implements Runnable {
    private volatile boolean running = true;
    private final DatagramSocket socket;
    private final UdpFloodProtectionService floodProtection = new UdpFloodProtectionService();
    private final Set<String> seenNonces = ConcurrentHashMap.newKeySet();
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
                String sourceIp = packet.getAddress().getHostAddress();
                if (!floodProtection.allow(sourceIp)) {
                    System.err.println("UDP packet rejete par rate limit ip=" + sourceIp);
                    continue;
                }
                String packetPayload = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                UdpNotificationSecurity.VerificationResult verification =
                        UdpNotificationSecurity.verify(packetPayload);
                if (!verification.isAccepted()) {
                    System.err.println("UDP packet rejete par signature : ip=" + sourceIp
                            + ", raison=" + verification.getReason());
                    continue;
                }
                if (!seenNonces.add(verification.getNonce())) {
                    System.err.println("UDP packet rejete par nonce deja vu : ip=" + sourceIp);
                    continue;
                }
                Platform.runLater(() -> UIUtils.showInfo(verification.getMessage()));
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
