package com.chrionline.client.model;

import java.util.Date;

public class ProductReviewDTO {
    private int id;
    private int utilisateurId;
    private int produitId;
    private int commandeId;
    private int note;
    private String commentaire;
    private String avisTaille;
    private Date createdAt;

    public int getId() { return id; }
    public int getUtilisateurId() { return utilisateurId; }
    public int getProduitId() { return produitId; }
    public int getCommandeId() { return commandeId; }
    public int getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public String getAvisTaille() { return avisTaille; }
    public Date getCreatedAt() { return createdAt; }
}
