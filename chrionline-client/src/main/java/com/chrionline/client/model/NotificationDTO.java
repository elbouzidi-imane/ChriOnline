package com.chrionline.client.model;

import java.util.Date;

public class NotificationDTO {
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
}
