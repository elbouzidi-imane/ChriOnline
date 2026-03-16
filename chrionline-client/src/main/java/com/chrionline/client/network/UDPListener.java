package com.chrionline.client.network;
import com.chrionline.common.AppConstants;
import java.net.*;
public class UDPListener implements Runnable {
    @Override
    public void run() {
        try (DatagramSocket s = new DatagramSocket(AppConstants.PORT_UDP)) {
            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                s.receive(pkt);
                System.out.println("[NOTIF] " + new String(pkt.getData(), 0, pkt.getLength()));
                // TODO : afficher dans JavaFX
            }
        } catch (Exception e) { System.err.println("Erreur UDP : " + e.getMessage()); }
    }
}
