package com.chrionline.server.model;

import java.io.Serializable;
import java.util.Date;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private int    commandeId;
    private double montant;
    private String modePaiement;  // CARTE_BANCAIRE, PAYPAL, VIREMENT, FICTIF
    private String statut;        // EN_ATTENTE, VALIDE, REFUSE, REMBOURSE
    private Date   datePaiement;
    private String reference;     // TXN-XXXXXXXX

    public Payment() {}

    public Payment(int commandeId, double montant, String modePaiement) {
        this.commandeId   = commandeId;
        this.montant      = montant;
        this.modePaiement = modePaiement;
        this.statut       = "EN_ATTENTE";
        this.datePaiement = new Date();
    }

    public int    getId()           { return id; }
    public int    getCommandeId()   { return commandeId; }
    public double getMontant()      { return montant; }
    public String getModePaiement() { return modePaiement; }
    public String getStatut()       { return statut; }
    public Date   getDatePaiement() { return datePaiement; }
    public String getReference()    { return reference; }

    public void setId(int id)                    { this.id = id; }
    public void setCommandeId(int cid)           { this.commandeId = cid; }
    public void setMontant(double m)             { this.montant = m; }
    public void setModePaiement(String mode)     { this.modePaiement = mode; }
    public void setStatut(String statut)         { this.statut = statut; }
    public void setDatePaiement(Date d)          { this.datePaiement = d; }
    public void setReference(String ref)         { this.reference = ref; }

    public boolean isValide() { return "VALIDE".equals(statut); }

    @Override
    public String toString() {
        return "Payment{commandeId=" + commandeId
                + ", montant=" + montant
                + ", statut='" + statut + "'}";
    }
}