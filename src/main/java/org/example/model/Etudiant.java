package org.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Etudiant {
    private int id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String cin;
    private Sexe sexe;
    private int groupeId;
    private String email;
    private String password;
    private int directeurId;
    private Groupe groupe;

    // Enums
    public enum Sexe {
        HOMME("Homme"),
        FEMME("Femme");

        private final String valeur;

        Sexe(String valeur) {
            this.valeur = valeur;
        }

        public String getValeur() {
            return valeur;
        }

        public static Sexe fromString(String text) {
            for (Sexe s : Sexe.values()) {
                if (s.valeur.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Sexe invalide: " + text);
        }
    }

    // Constructeurs
    public Etudiant() {}

    public Etudiant(String nom, String prenom, LocalDate dateNaissance, String cin,
                    Sexe sexe, int groupeId, String email, String password, int directeurId) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.cin = cin;
        this.sexe = sexe;
        this.groupeId = groupeId;
        this.email = email;
        this.password = password;
        this.directeurId = directeurId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = Sexe.fromString(sexe);
    }

    public int getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(int groupeId) {
        this.groupeId = groupeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDirecteurId() {
        return directeurId;
    }

    public void setDirecteurId(int directeurId) {
        this.directeurId = directeurId;
    }

    // Méthodes utilitaires
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateNaissance.getYear();
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }

    @Override
    public String toString() {
        return "Etudiant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", cin='" + cin + '\'' +
                ", email='" + email + '\'' +
                ", groupeId=" + groupeId +
                '}';
    }
}