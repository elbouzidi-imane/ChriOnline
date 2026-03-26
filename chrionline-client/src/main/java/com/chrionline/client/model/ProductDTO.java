package com.chrionline.client.model;

import java.util.ArrayList;
import java.util.List;

public class ProductDTO {
    private int id;
    private int categorieId;
    private String nom;
    private String description;
    private String matiere;
    private String couleur;
    private double prixOriginal;
    private double prixReduit;
    private int stock;
    private String statut;
    private String imageUrl;
    private CategoryDTO categorie;
    private List<ProductSizeDTO> tailles = new ArrayList<>();
    private List<GuideDTO> guides = new ArrayList<>();

    public int getId() {
        return id;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public String getMatiere() {
        return matiere;
    }

    public String getCouleur() {
        return couleur;
    }

    public double getPrixOriginal() {
        return prixOriginal;
    }

    public double getPrixReduit() {
        return prixReduit;
    }

    public int getStock() {
        return stock;
    }

    public String getStatut() {
        return statut;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public CategoryDTO getCategorie() {
        return categorie;
    }

    public List<ProductSizeDTO> getTailles() {
        return tailles == null ? List.of() : tailles;
    }

    public List<GuideDTO> getGuides() {
        return guides == null ? List.of() : guides;
    }

    public double getPrixAffiche() {
        return prixReduit > 0 ? prixReduit : prixOriginal;
    }

    public boolean hasReduction() {
        return prixReduit > 0 && prixReduit < prixOriginal;
    }

    public int getReductionPercentage() {
        if (!hasReduction() || prixOriginal <= 0) {
            return 0;
        }
        return (int) Math.round((1 - (prixReduit / prixOriginal)) * 100);
    }

    public boolean isDisponible() {
        return "ACTIF".equals(statut) && stock > 0;
    }

    @Override
    public String toString() {
        return nom;
    }
}
