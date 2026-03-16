package com.chrionline.common;
import java.io.Serializable;
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private String payload;
    private String status;
    public Message() {}
    public Message(String type, String payload) {
        this.type = type; this.payload = payload; this.status = Protocol.OK;
    }
    public static Message ok(String type, String payload) { return new Message(type, payload); }
    public static Message error(String reason) {
        Message m = new Message(Protocol.ERROR, reason); m.status = Protocol.ERROR; return m;
    }
    public String getType()    { return type; }
    public String getPayload() { return payload; }
    public String getStatus()  { return status; }
    public boolean isOk()      { return Protocol.OK.equals(status); }
    public void setType(String t)    { this.type = t; }
    public void setPayload(String p) { this.payload = p; }
    public void setStatus(String s)  { this.status = s; }
    @Override public String toString() {
        return "Message{type='" + type + "', status='" + status + "'}";
    }
}
