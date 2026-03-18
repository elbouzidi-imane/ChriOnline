package com.chrionline.client.model;

public class CategoryDTO {
    private int id;
    private String nom;
    private String description;

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return nom;
    }
}
