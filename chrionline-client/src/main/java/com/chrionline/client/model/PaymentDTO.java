package com.chrionline.client.model;

import java.util.Date;

public class PaymentDTO {
    private int id;
    private int commandeId;
    private double montant;
    private String modePaiement;
    private String statut;
    private Date datePaiement;
    private String reference;

    public int getId() {
        return id;
    }

    public int getCommandeId() {
        return commandeId;
    }

    public double getMontant() {
        return montant;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public String getStatut() {
        return statut;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public String getReference() {
        return reference;
    }
}
