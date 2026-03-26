package com.chrionline.server.model;

import java.io.Serializable;
import java.util.Date;

public class ProductReview implements Serializable {
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

    public void setId(int id) { this.id = id; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    public void setProduitId(int produitId) { this.produitId = produitId; }
    public void setCommandeId(int commandeId) { this.commandeId = commandeId; }
    public void setNote(int note) { this.note = note; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public void setAvisTaille(String avisTaille) { this.avisTaille = avisTaille; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
