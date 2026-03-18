package com.chrionline.client.model;

public class ProductSizeDTO {
    private int id;
    private int produitId;
    private String valeur;
    private int stock;

    public int getId() {
        return id;
    }

    public int getProduitId() {
        return produitId;
    }

    public String getValeur() {
        return valeur;
    }

    public int getStock() {
        return stock;
    }

    public boolean isDisponible() {
        return stock > 0;
    }
}
