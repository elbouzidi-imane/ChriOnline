package com.chrionline.server.network;

import com.chrionline.common.AppConstants;
import java.net.*;

public class UDPNotifier {

    /**
     * Envoie une notification UDP au client.
     * Appelé après validation d'une commande.
     *
     * @param clientHost  IP du client (ex: "192.168.1.10")
     * @param message     texte à envoyer (ex: "Commande CMD-2026-00001 confirmée !")
     */
    public static void sendNotification(String clientHost, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] data    = message.getBytes();
            InetAddress ip = InetAddress.getByName(clientHost);
            DatagramPacket packet = new DatagramPacket(
                    data, data.length, ip, AppConstants.PORT_UDP);
            socket.send(packet);
            System.out.println("[UDP] Notification envoyée à " + clientHost
                    + " : " + message);
        } catch (Exception e) {
            System.err.println("[UDP] Erreur envoi notification : " + e.getMessage());
        }
    }
}
