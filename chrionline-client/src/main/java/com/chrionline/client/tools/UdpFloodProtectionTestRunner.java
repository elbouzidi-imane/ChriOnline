package com.chrionline.client.tools;

import com.chrionline.client.security.UdpFloodProtectionService;
import com.chrionline.common.AppConstants;

public final class UdpFloodProtectionTestRunner {

    private UdpFloodProtectionTestRunner() {
    }

    public static void main(String[] args) throws Exception {
        UdpFloodProtectionService limiter = new UdpFloodProtectionService();
        String ip = "127.0.0.1";
        int accepted = 0;
        int rejected = 0;

        for (int i = 1; i <= 25; i++) {
            if (limiter.allow(ip)) {
                accepted++;
            } else {
                rejected++;
            }
        }

        Thread.sleep(AppConstants.UDP_RATE_LIMIT_WINDOW_MILLIS + 500);
        boolean acceptedAfterWindow = limiter.allow(ip);

        System.out.println("=== UDP flood rate limit test ===");
        System.out.println("Limite : " + AppConstants.UDP_MAX_PACKETS_PER_IP
                + " paquets / " + AppConstants.UDP_RATE_LIMIT_WINDOW_MILLIS + " ms");
        System.out.println("Paquets acceptes sur 25 : " + accepted);
        System.out.println("Paquets rejetes sur 25 : " + rejected);
        System.out.println("Accepte apres nouvelle fenetre : " + acceptedAfterWindow);
        System.out.println(accepted == AppConstants.UDP_MAX_PACKETS_PER_IP
                && rejected == 5
                && acceptedAfterWindow
                ? "RESULTAT: PASS"
                : "RESULTAT: FAIL");
    }
}
