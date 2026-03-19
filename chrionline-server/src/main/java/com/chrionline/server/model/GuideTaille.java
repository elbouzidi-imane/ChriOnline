package com.chrionline.server.model;

import java.io.Serializable;

public class GuideTaille implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private int    produitId;
    private String taille;
    private String poitrine;
    private String tailleCm;
    private String hanches;

    public GuideTaille() {}

    public GuideTaille(int produitId, String taille,
                       String poitrine, String tailleCm, String hanches) {
        this.produitId = produitId;
        this.taille    = taille;
        this.poitrine  = poitrine;
        this.tailleCm  = tailleCm;
        this.hanches   = hanches;
    }

    public int    getId()        { return id; }
    public int    getProduitId() { return produitId; }
    public String getTaille()    { return taille; }
    public String getPoitrine()  { return poitrine; }
    public String getTailleCm()  { return tailleCm; }
    public String getHanches()   { return hanches; }

    public void setId(int id)               { this.id = id; }
    public void setProduitId(int pid)       { this.produitId = pid; }
    public void setTaille(String t)         { this.taille = t; }
    public void setPoitrine(String p)       { this.poitrine = p; }
    public void setTailleCm(String t)       { this.tailleCm = t; }
    public void setHanches(String h)        { this.hanches = h; }

    @Override
    public String toString() {
        return "GuideTaille{produitId=" + produitId
                + ", taille='" + taille + "'}";
    }
}