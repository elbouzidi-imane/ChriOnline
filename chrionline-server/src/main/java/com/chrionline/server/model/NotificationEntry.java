package com.chrionline.server.model;

import java.io.Serializable;
import java.util.Date;

public class NotificationEntry implements Serializable {
    private int id;
    private int utilisateurId;
    private String message;
    private boolean lue;
    private Date createdAt;

    public int getId() {
        return id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isLue() {
        return lue;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLue(boolean lue) {
        this.lue = lue;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
