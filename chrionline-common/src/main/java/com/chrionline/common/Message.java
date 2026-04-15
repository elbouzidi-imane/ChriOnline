package com.chrionline.common;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String payload;
    private String status;
    private String requestId;
    private long timestamp;
    private String sessionToken;

    public Message() {
    }

    public Message(String type, String payload) {
        this.type = type;
        this.payload = payload;
        this.status = Protocol.OK;
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public static Message ok(String type, String payload) {
        return new Message(type, payload);
    }

    public static Message ok(String type) {
        return new Message(type, "");
    }

    public static Message error(String reason) {
        Message message = new Message(Protocol.ERROR, reason);
        message.status = Protocol.ERROR;
        return message;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }

    public String getRequestId() {
        return requestId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isOk() {
        return Protocol.OK.equals(status);
    }

    public boolean isError() {
        return Protocol.ERROR.equals(status);
    }

    @Override
    public String toString() {
        return "Message{type='" + type + "', status='" + status
                + "', requestId='" + requestId + "', payload='" + payload + "'}";
    }
}
