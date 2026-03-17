package com.chrionline.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private int          id;
    private int          categorieId;
    private String       nom;
    private String       description;
    private String       matiere;
    private String       couleur;
    private double       prixOriginal;
    private double       prixReduit;
    private int          stock;
    private String       statut;       // ACTIF ou INACTIF
    private int          nombreVentes;
    private Date         dateDebutVente;
    private String       imageUrl;
    private Category     categorie;
    private List<ProductSize> tailles = new ArrayList<>();

    public Product() {}

    public Product(int categorieId, String nom, String description,
                   String matiere, String couleur,
                   double prixOriginal, double prixReduit) {
        this.categorieId  = categorieId;
        this.nom          = nom;
        this.description  = description;
        this.matiere      = matiere;
        this.couleur      = couleur;
        this.prixOriginal = prixOriginal;
        this.prixReduit   = prixReduit;
        this.statut       = "ACTIF";
    }

    // ── Getters ───────────────────────────────────────
    public int       getId()            { return id; }
    public int       getCategorieId()   { return categorieId; }
    public String    getNom()           { return nom; }
    public String    getDescription()   { return description; }
    public String    getMatiere()       { return matiere; }
    public String    getCouleur()       { return couleur; }
    public double    getPrixOriginal()  { return prixOriginal; }
    public double    getPrixReduit()    { return prixReduit; }
    public int       getStock()         { return stock; }
    public String    getStatut()        { return statut; }
    public int       getNombreVentes()  { return nombreVentes; }
    public Date      getDateDebutVente(){ return dateDebutVente; }
    public String    getImageUrl()      { return imageUrl; }
    public Category  getCategorie()     { return categorie; }
    public List<ProductSize> getTailles(){ return tailles; }

    // ── Setters ───────────────────────────────────────
    public void setId(int id)                        { this.id = id; }
    public void setCategorieId(int cid)              { this.categorieId = cid; }
    public void setNom(String nom)                   { this.nom = nom; }
    public void setDescription(String desc)          { this.description = desc; }
    public void setMatiere(String matiere)           { this.matiere = matiere; }
    public void setCouleur(String couleur)           { this.couleur = couleur; }
    public void setPrixOriginal(double prix)         { this.prixOriginal = prix; }
    public void setPrixReduit(double prix)           { this.prixReduit = prix; }
    public void setStock(int stock)                  { this.stock = stock; }
    public void setStatut(String statut)             { this.statut = statut; }
    public void setNombreVentes(int n)               { this.nombreVentes = n; }
    public void setDateDebutVente(Date d)            { this.dateDebutVente = d; }
    public void setImageUrl(String url)              { this.imageUrl = url; }
    public void setCategorie(Category cat)           { this.categorie = cat; }
    public void setTailles(List<ProductSize> t)      { this.tailles = t; }

    // ── Méthodes métier ───────────────────────────────

    /** Prix à afficher : prix réduit si dispo, sinon prix original */
    public double getPrixAffiche() {
        return (prixReduit > 0) ? prixReduit : prixOriginal;
    }

    /** Produit achetable : actif ET stock > 0 */
    public boolean isDisponible() {
        return "ACTIF".equals(statut) && stock > 0;
    }

    /** Tailles encore en stock */
    public List<ProductSize> getTaillesDisponibles() {
        List<ProductSize> dispo = new ArrayList<>();
        for (ProductSize t : tailles) {
            if (t.isDisponible()) dispo.add(t);
        }
        return dispo;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", nom='" + nom
                + "', prix=" + getPrixAffiche()
                + ", stock=" + stock + "}";
    }
}