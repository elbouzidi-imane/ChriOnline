package com.chrionline.server.network;
import com.chrionline.common.AppConstants;
import java.net.*;
public class UDPNotifier {
    public static void sendNotification(String clientHost, String message) {
        try (DatagramSocket s = new DatagramSocket()) {
            byte[] data = message.getBytes();
            DatagramPacket pkt = new DatagramPacket(data, data.length,
                InetAddress.getByName(clientHost), AppConstants.PORT_UDP);
            s.send(pkt);
        } catch (Exception e) { System.err.println("Erreur UDP : " + e.getMessage()); }
    }
}
