package com.chrionline.client.model;

import java.util.ArrayList;
import java.util.List;

public class CartDTO {
    private int id;
    private int utilisateurId;
    private List<CartLineDTO> lignes = new ArrayList<>();

    public int getId() {
        return id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public List<CartLineDTO> getLignes() {
        return lignes == null ? List.of() : lignes;
    }

    public double getTotal() {
        return getLignes().stream().mapToDouble(CartLineDTO::getSousTotal).sum();
    }

    public boolean isEmpty() {
        return getLignes().isEmpty();
    }
}
