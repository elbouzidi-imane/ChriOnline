package com.chrionline.server.model;

import java.io.Serializable;

public class CartLine implements Serializable {
    private static final long serialVersionUID = 1L;

    private int         id;
    private int         panierId;
    private int         produitId;
    private int         tailleId;
    private int         quantite;
    private double      prixUnitaire;
    private Product     produit;
    private ProductSize taille;

    public CartLine() {}

    public CartLine(int panierId, int produitId,
                    int tailleId, int quantite, double prixUnitaire) {
        this.panierId     = panierId;
        this.produitId    = produitId;
        this.tailleId     = tailleId;
        this.quantite     = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int         getId()           { return id; }
    public int         getPanierId()     { return panierId; }
    public int         getProduitId()    { return produitId; }
    public int         getTailleId()     { return tailleId; }
    public int         getQuantite()     { return quantite; }
    public double      getPrixUnitaire() { return prixUnitaire; }
    public Product     getProduit()      { return produit; }
    public ProductSize getTaille()       { return taille; }

    public void setId(int id)                   { this.id = id; }
    public void setPanierId(int pid)            { this.panierId = pid; }
    public void setProduitId(int pid)           { this.produitId = pid; }
    public void setTailleId(int tid)            { this.tailleId = tid; }
    public void setQuantite(int q)              { this.quantite = q; }
    public void setPrixUnitaire(double p)       { this.prixUnitaire = p; }
    public void setProduit(Product p)           { this.produit = p; }
    public void setTaille(ProductSize t)        { this.taille = t; }

    public double getSousTotal() { return quantite * prixUnitaire; }

    @Override
    public String toString() {
        return "CartLine{produitId=" + produitId
                + ", quantite=" + quantite
                + ", sousTotal=" + getSousTotal() + "}";
    }
}