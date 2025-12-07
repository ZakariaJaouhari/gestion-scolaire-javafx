package org.example.model;

import java.time.LocalDateTime;

public class Groupe {
    private int id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String matricule;
    private Niveau niveau;
    private int directeurId;

    // Enum pour le niveau
    public enum Niveau {
        PREMIERE_ANNEE("1ér année"),
        DEUXIEME_ANNEE("2éme année"),
        TROISIEME_ANNEE("3éme année"),
        QUATRIEME_ANNEE("4éme année"),
        CINQUIEME_ANNEE("5éme année");

        private final String valeur;

        Niveau(String valeur) {
            this.valeur = valeur;
        }

        public String getValeur() {
            return valeur;
        }

        public static Niveau fromString(String text) {
            for (Niveau n : Niveau.values()) {
                if (n.valeur.equalsIgnoreCase(text)) {
                    return n;
                }
            }
            throw new IllegalArgumentException("Niveau invalide: " + text);
        }
    }

    // Constructeurs
    public Groupe() {}

    public Groupe(String matricule, Niveau niveau, int directeurId) {
        this.matricule = matricule;
        this.niveau = niveau;
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

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = Niveau.fromString(niveau);
    }

    public int getDirecteurId() {
        return directeurId;
    }

    public void setDirecteurId(int directeurId) {
        this.directeurId = directeurId;
    }

    // Méthodes utilitaires
    public String getNiveauComplet() {
        return niveau.getValeur();
    }

    public String getDescription() {
        return matricule + " - " + niveau.getValeur();
    }

    @Override
    public String toString() {
        return "Groupe{" +
                "id=" + id +
                ", matricule='" + matricule + '\'' +
                ", niveau=" + niveau +
                ", directeurId=" + directeurId +
                '}';
    }
}