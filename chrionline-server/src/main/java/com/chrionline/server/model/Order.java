package com.chrionline.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private int             id;
    private String          reference;       // CMD-2026-00001
    private int             utilisateurId;
    private Date            dateCommande;
    private String          statut;          // EN_ATTENTE, VALIDEE, EXPEDIEE, LIVREE, ANNULEE
    private double          montantTotal;
    private String          adresseLivraison;
    private List<OrderLine> lignes = new ArrayList<>();

    public Order() {}

    public Order(int utilisateurId, double montantTotal, String adresseLivraison) {
        this.utilisateurId   = utilisateurId;
        this.montantTotal    = montantTotal;
        this.adresseLivraison = adresseLivraison;
        this.statut          = "EN_ATTENTE";
        this.dateCommande    = new Date();
    }

    public int             getId()               { return id; }
    public String          getReference()        { return reference; }
    public int             getUtilisateurId()    { return utilisateurId; }
    public Date            getDateCommande()     { return dateCommande; }
    public String          getStatut()           { return statut; }
    public double          getMontantTotal()     { return montantTotal; }
    public String          getAdresseLivraison() { return adresseLivraison; }
    public List<OrderLine> getLignes()           { return lignes; }

    public void setId(int id)                         { this.id = id; }
    public void setReference(String ref)              { this.reference = ref; }
    public void setUtilisateurId(int uid)             { this.utilisateurId = uid; }
    public void setDateCommande(Date d)               { this.dateCommande = d; }
    public void setStatut(String statut)              { this.statut = statut; }
    public void setMontantTotal(double m)             { this.montantTotal = m; }
    public void setAdresseLivraison(String adr)       { this.adresseLivraison = adr; }
    public void setLignes(List<OrderLine> lignes)     { this.lignes = lignes; }

    @Override
    public String toString() {
        return "Order{ref='" + reference + "', statut='"
                + statut + "', total=" + montantTotal + "}";
    }
}