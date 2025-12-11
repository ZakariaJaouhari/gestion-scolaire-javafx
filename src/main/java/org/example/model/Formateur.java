package org.example.model;

import org.example.util.SessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Formateur {
    private int id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String nom;
    private String prenom;
    private String matricule;
    private Sexe sexe;
    private LocalDate dateNaissance;
    private Situation situation;
    private String cin;
    private LocalDate dateRecrutement;
    private String email;
    private String password;
    private String profilePicture;
    private int directeurId;



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

    public enum Situation {
        MARIE("Marié(e)"),
        CELIBATAIRE("Célibataire");

        private final String valeur;

        Situation(String valeur) {
            this.valeur = valeur;
        }

        public String getValeur() {
            return valeur;
        }

        public static Situation fromString(String text) {
            for (Situation s : Situation.values()) {
                if (s.valeur.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Situation invalide: " + text);
        }
    }

    // Constructeurs
    public Formateur() {}

    public Formateur(String nom, String prenom, String matricule, Sexe sexe,
                     LocalDate dateNaissance, Situation situation, String cin,
                     LocalDate dateRecrutement, String email, String password,
                     String profilePicture, int directeurId) {
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.sexe = sexe;
        this.dateNaissance = dateNaissance;
        this.situation = situation;
        this.cin = cin;
        this.dateRecrutement = dateRecrutement;
        this.email = email;
        this.password = password;
        this.profilePicture = profilePicture;
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

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
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

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Situation getSituation() {
        return situation;
    }

    public void setSituation(Situation situation) {
        this.situation = situation;
    }

    public void setSituation(String situation) {
        this.situation = Situation.fromString(situation);
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public LocalDate getDateRecrutement() {
        return dateRecrutement;
    }

    public void setDateRecrutement(LocalDate dateRecrutement) {
        this.dateRecrutement = dateRecrutement;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
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

    public int getAnciennete() {
        return LocalDate.now().getYear() - dateRecrutement.getYear();
    }

    @Override
    public String toString() {
        return "Formateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", matricule='" + matricule + '\'' +
                ", sexe=" + sexe +
                ", email='" + email + '\'' +
                '}';
    }
}