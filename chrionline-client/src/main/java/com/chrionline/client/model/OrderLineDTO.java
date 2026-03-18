package com.chrionline.client.model;

public class OrderLineDTO {
    private int id;
    private int commandeId;
    private int produitId;
    private int tailleId;
    private int quantite;
    private double prixUnitaire;

    public int getId() {
        return id;
    }

    public int getCommandeId() {
        return commandeId;
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

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }
}
