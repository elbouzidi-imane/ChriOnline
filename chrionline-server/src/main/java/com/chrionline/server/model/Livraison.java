package com.chrionline.server.model;

import java.io.Serializable;
import java.util.Date;

public class Livraison implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private int    commandeId;
    private String modeLivraison;  // STANDARD, EXPRESS, POINT_RELAIS
    private String statut;         // EN_ATTENTE, EN_COURS, EXPEDIEE, LIVREE
    private Date   dateEstimee;
    private Date   dateEffective;
    private boolean suiviActif;

    public Livraison() {}

    public Livraison(int commandeId, String modeLivraison) {
        this.commandeId    = commandeId;
        this.modeLivraison = modeLivraison;
        this.statut        = "EN_ATTENTE";
        this.suiviActif    = false;
        this.dateEstimee   = calculerDateEstimee(modeLivraison);
    }

    private Date calculerDateEstimee(String mode) {
        long now = System.currentTimeMillis();
        long jours = switch (mode) {
            case "EXPRESS"      -> 1L;
            case "POINT_RELAIS" -> 7L;
            default             -> 5L; // STANDARD
        };
        return new Date(now + jours * 24 * 60 * 60 * 1000);
    }

    public int     getId()            { return id; }
    public int     getCommandeId()    { return commandeId; }
    public String  getModeLivraison() { return modeLivraison; }
    public String  getStatut()        { return statut; }
    public Date    getDateEstimee()   { return dateEstimee; }
    public Date    getDateEffective() { return dateEffective; }
    public boolean isSuiviActif()     { return suiviActif; }

    public void setId(int id)                      { this.id = id; }
    public void setCommandeId(int cid)             { this.commandeId = cid; }
    public void setModeLivraison(String mode)      { this.modeLivraison = mode; }
    public void setStatut(String statut)           { this.statut = statut; }
    public void setDateEstimee(Date d)             { this.dateEstimee = d; }
    public void setDateEffective(Date d)           { this.dateEffective = d; }
    public void setSuiviActif(boolean b)           { this.suiviActif = b; }

    public boolean isLivree() { return "LIVREE".equals(statut); }

    @Override
    public String toString() {
        return "Livraison{commandeId=" + commandeId
                + ", mode='" + modeLivraison
                + "', statut='" + statut + "'}";
    }
}