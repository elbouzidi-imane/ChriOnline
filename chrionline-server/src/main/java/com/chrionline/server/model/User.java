package com.chrionline.server.model;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int    id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String adresse;
    private String role;       // CLIENT ou ADMIN
    private String statut;     // ACTIF ou SUSPENDU
    private Date   dateInscription;

    public User() {}

    public User(String nom, String prenom, String email,
                String motDePasse, String telephone, String adresse) {
        this.nom         = nom;
        this.prenom      = prenom;
        this.email       = email;
        this.motDePasse  = motDePasse;
        this.telephone   = telephone;
        this.adresse     = adresse;
        this.role        = "CLIENT";
        this.statut      = "ACTIF";
    }

    // Getters & Setters
    public int    getId()            { return id; }
    public String getNom()           { return nom; }
    public String getPrenom()        { return prenom; }
    public String getEmail()         { return email; }
    public String getMotDePasse()    { return motDePasse; }
    public String getTelephone()     { return telephone; }
    public String getAdresse()       { return adresse; }
    public String getRole()          { return role; }
    public String getStatut()        { return statut; }
    public Date   getDateInscription(){ return dateInscription; }

    public void setId(int id)                       { this.id = id; }
    public void setNom(String nom)                  { this.nom = nom; }
    public void setPrenom(String prenom)            { this.prenom = prenom; }
    public void setEmail(String email)              { this.email = email; }
    public void setMotDePasse(String motDePasse)    { this.motDePasse = motDePasse; }
    public void setTelephone(String telephone)      { this.telephone = telephone; }
    public void setAdresse(String adresse)          { this.adresse = adresse; }
    public void setRole(String role)                { this.role = role; }
    public void setStatut(String statut)            { this.statut = statut; }
    public void setDateInscription(Date d)          { this.dateInscription = d; }

    public boolean isAdmin()    { return "ADMIN".equals(role); }
    public boolean isActif()    { return "ACTIF".equals(statut); }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email
                + "', role='" + role + "', statut='" + statut + "'}";
    }
}