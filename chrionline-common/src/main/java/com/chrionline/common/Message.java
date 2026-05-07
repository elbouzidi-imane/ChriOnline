package com.chrionline.common;

import java.io.Serializable;

/**
 * Objet échangé entre client et serveur via TCP (ObjectOutputStream).
 *
 * Exemple envoi :
 *   Message req = new Message(Protocol.LOGIN, "jean@email.com:monmdp");
 *
 * Exemple réponse :
 *   Message rep = Message.ok(Protocol.LOGIN, "CLIENT:42");
 *   Message err = Message.error("Identifiants incorrects");
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;      // code Protocol : LOGIN, GET_PRODUCTS...
    private String payload;   // données : JSON, texte délimité, ou vide
    private String status;    // "OK" ou "ERROR"
    private long timestamp;
    private String nonce;
    private String signature;
    private String requestId;
    private String sessionToken;
    private String sensitiveOtp;

    // ── Constructeurs ────────────────────────────────

    public Message() {}

    public Message(String type, String payload) {
        this.type    = type;
        this.payload = payload;
        this.status  = Protocol.OK;
    }

    public Message(Message source) {
        this.type = source.type;
        this.payload = source.payload;
        this.status = source.status;
        this.timestamp = source.timestamp;
        this.nonce = source.nonce;
        this.signature = source.signature;
        this.requestId = source.requestId;
        this.sessionToken = source.sessionToken;
        this.sensitiveOtp = source.sensitiveOtp;
    }

    // ── Factories statiques ───────────────────────────

    /** Crée une réponse OK avec données */
    public static Message ok(String type, String payload) {
        return new Message(type, payload);
    }

    /** Crée une réponse OK sans données */
    public static Message ok(String type) {
        return new Message(type, "");
    }

    /** Crée une réponse ERROR avec la raison */
    public static Message error(String reason) {
        Message m = new Message(Protocol.ERROR, reason);
        m.status = Protocol.ERROR;
        return m;
    }

    // ── Getters / Setters ─────────────────────────────

    public String getType()    { return type; }
    public String getPayload() { return payload; }
    public String getStatus()  { return status; }
    public long getTimestamp() { return timestamp; }
    public String getNonce() { return nonce; }
    public String getSignature() { return signature; }
    public String getRequestId() { return requestId; }
    public String getSessionToken() { return sessionToken; }
    public String getSensitiveOtp() { return sensitiveOtp; }

    public void setType(String type)       { this.type    = type; }
    public void setPayload(String payload) { this.payload = payload; }
    public void setStatus(String status)   { this.status  = status; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setNonce(String nonce) { this.nonce = nonce; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public void setSensitiveOtp(String sensitiveOtp) { this.sensitiveOtp = sensitiveOtp; }

    // ── Utilitaires ───────────────────────────────────

    /** Retourne true si le serveur a répondu OK */
    public boolean isOk() {
        return Protocol.OK.equals(status);
    }

    /** Retourne true si le serveur a répondu ERROR */
    public boolean isError() {
        return Protocol.ERROR.equals(status);
    }

    @Override
    public String toString() {
        return "Message{type='" + type + "', status='" + status
                + "', payload='" + payload + "'}";
    }
}
