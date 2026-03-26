package com.chrionline.client.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderDTO {
    private int id;
    private String reference;
    private int utilisateurId;
    private Date dateCommande;
    private String statut;
    private double montantTotal;
    private String adresseLivraison;
    private String motifAnnulation;
    private List<OrderLineDTO> lignes = new ArrayList<>();

    public int getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public Date getDateCommande() {
        return dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public List<OrderLineDTO> getLignes() {
        return lignes == null ? List.of() : lignes;
    }
}
