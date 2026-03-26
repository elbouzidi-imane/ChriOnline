package com.chrionline.server.network;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectedClientRegistry {
    private static final Map<Integer, ClientEndpoint> USER_ENDPOINTS = new ConcurrentHashMap<>();
    private static final Set<Integer> ADMIN_IDS = ConcurrentHashMap.newKeySet();

    private ConnectedClientRegistry() {
    }

    public static void registerUser(int userId, boolean admin, String host, int port) {
        if (userId <= 0 || host == null || host.isBlank() || port <= 0) {
            return;
        }
        USER_ENDPOINTS.put(userId, new ClientEndpoint(host, port));
        if (admin) {
            ADMIN_IDS.add(userId);
        } else {
            ADMIN_IDS.remove(userId);
        }
        System.out.println("[UDP] Enregistre user=" + userId + " admin=" + admin + " host=" + host + " port=" + port);
    }

    public static void unregisterUser(int userId) {
        if (userId <= 0) {
            return;
        }
        USER_ENDPOINTS.remove(userId);
        ADMIN_IDS.remove(userId);
        System.out.println("[UDP] Desinscription user=" + userId);
    }

    public static void notifyUser(int userId, String message) {
        ClientEndpoint endpoint = USER_ENDPOINTS.get(userId);
        if (endpoint == null || endpoint.host().isBlank() || endpoint.port() <= 0) {
            System.out.println("[UDP] Aucun client connecte pour user=" + userId);
            return;
        }
        UDPNotifier.sendNotification(endpoint.host(), endpoint.port(), message);
    }

    public static void notifyAdmins(String message) {
        for (Integer adminId : ADMIN_IDS) {
            notifyUser(adminId, message);
        }
    }

    private static final class ClientEndpoint {
        private final String host;
        private final int port;

        private ClientEndpoint(String host, int port) {
            this.host = host;
            this.port = port;
        }

        private String host() {
            return host;
        }

        private int port() {
            return port;
        }
    }
}
