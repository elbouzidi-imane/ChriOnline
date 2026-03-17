package com.chrionline.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private int           id;
    private int           utilisateurId;
    private Date          dateCreation;
    private List<CartLine> lignes = new ArrayList<>();

    public Cart() {}

    public Cart(int utilisateurId) {
        this.utilisateurId = utilisateurId;
        this.dateCreation  = new Date();
    }

    public int            getId()            { return id; }
    public int            getUtilisateurId() { return utilisateurId; }
    public Date           getDateCreation()  { return dateCreation; }
    public List<CartLine> getLignes()        { return lignes; }

    public void setId(int id)                      { this.id = id; }
    public void setUtilisateurId(int uid)           { this.utilisateurId = uid; }
    public void setDateCreation(Date d)            { this.dateCreation = d; }
    public void setLignes(List<CartLine> lignes)   { this.lignes = lignes; }

    /** Total du panier */
    public double getTotal() {
        double total = 0;
        for (CartLine l : lignes) total += l.getSousTotal();
        return total;
    }

    /** Nombre total d'articles */
    public int getNombreArticles() {
        int n = 0;
        for (CartLine l : lignes) n += l.getQuantite();
        return n;
    }

    public boolean isEmpty() { return lignes.isEmpty(); }

    @Override
    public String toString() {
        return "Cart{id=" + id + ", articles="
                + getNombreArticles() + ", total=" + getTotal() + "}";
    }
}