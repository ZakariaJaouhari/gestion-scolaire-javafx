package org.example.model;

public class Directeur {
    private int id;
    private String nomEcole;
    private String nomDirecteur;
    private String academie;
    private String direction;
    private String annee;
    private String email;
    private String password;

    // Constructeurs
    public Directeur() {}

    public Directeur(String nomEcole, String nomDirecteur, String academie,
                     String direction, String annee, String email, String password) {
        this.nomEcole = nomEcole;
        this.nomDirecteur = nomDirecteur;
        this.academie = academie;
        this.direction = direction;
        this.annee = annee;
        this.email = email;
        this.password = password;
    }

    // Getters et Setters (générés automatiquement)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomEcole() { return nomEcole; }
    public void setNomEcole(String nomEcole) { this.nomEcole = nomEcole; }

    public String getNomDirecteur() { return nomDirecteur; }
    public void setNomDirecteur(String nomDirecteur) { this.nomDirecteur = nomDirecteur; }

    public String getAcademie() { return academie; }
    public void setAcademie(String academie) { this.academie = academie; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getAnnee() { return annee; }
    public void setAnnee(String annee) { this.annee = annee; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}