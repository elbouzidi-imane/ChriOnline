package com.chrionline.client.model;

public class CartLineDTO {
    private int id;
    private int panierId;
    private int produitId;
    private int tailleId;
    private int quantite;
    private double prixUnitaire;
    private ProductDTO produit;
    private ProductSizeDTO taille;

    public int getId() {
        return id;
    }

    public int getPanierId() {
        return panierId;
    }

    public int getProduitId() {
        return produitId;
    }

    public int getTailleId() {
        return tailleId;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public ProductDTO getProduit() {
        return produit;
    }

    public ProductSizeDTO getTaille() {
        return taille;
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }
}
