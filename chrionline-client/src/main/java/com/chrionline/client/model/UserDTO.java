package com.chrionline.client.model;

import java.util.Date;

public class UserDTO {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String role;
    private String statut;
    private Date dateInscription;
    private Date dateNaissance;

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getRole() {
        return role;
    }

    public String getStatut() {
        return statut;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
