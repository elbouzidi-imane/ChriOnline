package com.chrionline.server.model;

import java.io.Serializable;

public class ProductSize implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private int    produitId;
    private String valeur;   // XS, S, M, L, XL, XXL ou 36, 38...
    private int    stock;

    public ProductSize() {}

    public ProductSize(int produitId, String valeur, int stock) {
        this.produitId = produitId;
        this.valeur    = valeur;
        this.stock     = stock;
    }

    public int    getId()       { return id; }
    public int    getProduitId(){ return produitId; }
    public String getValeur()   { return valeur; }
    public int    getStock()    { return stock; }

    public void setId(int id)            { this.id = id; }
    public void setProduitId(int pid)    { this.produitId = pid; }
    public void setValeur(String valeur) { this.valeur = valeur; }
    public void setStock(int stock)      { this.stock = stock; }

    public boolean isDisponible()        { return stock > 0; }

    public boolean decrementerStock(int qte) {
        if (stock < qte) return false;
        this.stock -= qte;
        return true;
    }

    public void incrementerStock(int qte) { this.stock += qte; }

    @Override
    public String toString() {
        return "ProductSize{valeur='" + valeur + "', stock=" + stock + "}";
    }
}