package com.chrionline.server.model;

import java.io.Serializable;

public class OrderLine implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private int    commandeId;
    private int    produitId;
    private int    tailleId;
    private int    quantite;
    private double prixUnitaire;

    public OrderLine() {}

    public OrderLine(int commandeId, int produitId,
                     int tailleId, int quantite, double prixUnitaire) {
        this.commandeId   = commandeId;
        this.produitId    = produitId;
        this.tailleId     = tailleId;
        this.quantite     = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int    getId()           { return id; }
    public int    getCommandeId()   { return commandeId; }
    public int    getProduitId()    { return produitId; }
    public int    getTailleId()     { return tailleId; }
    public int    getQuantite()     { return quantite; }
    public double getPrixUnitaire() { return prixUnitaire; }

    public void setId(int id)               { this.id = id; }
    public void setCommandeId(int cid)      { this.commandeId = cid; }
    public void setProduitId(int pid)       { this.produitId = pid; }
    public void setTailleId(int tid)        { this.tailleId = tid; }
    public void setQuantite(int q)          { this.quantite = q; }
    public void setPrixUnitaire(double p)   { this.prixUnitaire = p; }

    public double getSousTotal() { return quantite * prixUnitaire; }

    @Override
    public String toString() {
        return "OrderLine{produitId=" + produitId
                + ", quantite=" + quantite
                + ", sousTotal=" + getSousTotal() + "}";
    }
}