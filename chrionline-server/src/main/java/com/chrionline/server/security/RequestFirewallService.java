package com.chrionline.server.security;

import com.chrionline.common.Message;

public final class RequestFirewallService {
    private static final int MAX_PAYLOAD_LENGTH = 8_192;

    private RequestFirewallService() {
    }

    public static String validate(Message request) {
        if (request == null) {
            return "Requete vide";
        }
        if (request.getType() == null || request.getType().isBlank()) {
            return "Type de requete manquant";
        }
        String payload = request.getPayload();
        if (payload != null && payload.length() > MAX_PAYLOAD_LENGTH) {
            return "Requete rejetee : charge utile trop volumineuse";
        }
        if (containsForbiddenControlChars(payload)) {
            return "Requete rejetee : contenu binaire ou caracteres de controle interdits";
        }
        return null;
    }

    private static boolean containsForbiddenControlChars(String payload) {
        if (payload == null) {
            return false;
        }
        for (int i = 0; i < payload.length(); i++) {
            char current = payload.charAt(i);
            if (current == '\n' || current == '\r' || current == '\t') {
                continue;
            }
            if (Character.isISOControl(current)) {
                return true;
            }
        }
        return false;
    }
}
