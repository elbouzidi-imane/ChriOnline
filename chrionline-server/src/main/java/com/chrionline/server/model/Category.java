package com.chrionline.server.model;

import java.io.Serializable;

public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private String nom;
    private String description;

    public Category() {}

    public Category(String nom, String description) {
        this.nom         = nom;
        this.description = description;
    }

    public int    getId()          { return id; }
    public String getNom()         { return nom; }
    public String getDescription() { return description; }

    public void setId(int id)                    { this.id = id; }
    public void setNom(String nom)               { this.nom = nom; }
    public void setDescription(String desc)      { this.description = desc; }

    @Override
    public String toString() {
        return "Category{id=" + id + ", nom='" + nom + "'}";
    }
}