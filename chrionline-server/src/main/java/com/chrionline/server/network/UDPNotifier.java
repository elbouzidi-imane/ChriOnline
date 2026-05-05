package com.chrionline.server.network;

import com.chrionline.common.UdpNotificationSecurity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPNotifier {

    private UDPNotifier() {
    }

    public static void sendNotification(String clientHost, int clientPort, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] data = UdpNotificationSecurity.sign(message).getBytes();
            InetAddress ip = InetAddress.getByName(clientHost);
            DatagramPacket packet = new DatagramPacket(data, data.length, ip, clientPort);
            socket.send(packet);
            System.out.println("[UDP] Notification envoyee a " + clientHost + ":" + clientPort + " : " + message);
        } catch (Exception e) {
            System.err.println("[UDP] Erreur envoi notification : " + e.getMessage());
        }
    }
}
