package org.example.model;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int etudiantId;
    private Etudiant etudiant;
    private int moduleId;
    private Module module;
    private int formateurId;
    private Formateur formateur;
    private double note; // de 0 à 20
    private String appreciation;
    private String observation;
    private int anneeScolaire;
    private String semestre; // "Semestre 1", "Semestre 2"
    private String typeEvaluation; // "Contrôle continu", "Examen", "TP", "Projet"



    // Constructeurs
    public Note() {}

    public Note(int etudiantId, int moduleId, int formateurId, double note,
                int anneeScolaire, String semestre, String typeEvaluation) {
        this.etudiantId = etudiantId;
        this.moduleId = moduleId;
        this.formateurId = formateurId;
        this.note = note;
        this.anneeScolaire = anneeScolaire;
        this.semestre = semestre;
        this.typeEvaluation = typeEvaluation;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public Etudiant getEtudiant() { return etudiant; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public int getFormateurId() { return formateurId; }
    public void setFormateurId(int formateurId) { this.formateurId = formateurId; }

    public Formateur getFormateur() { return formateur; }
    public void setFormateur(Formateur formateur) { this.formateur = formateur; }

    public double getNote() { return note; }
    public void setNote(double note) {
        if (note < 0 || note > 20) {
            throw new IllegalArgumentException("La note doit être entre 0 et 20");
        }
        this.note = note;
    }

    public String getAppreciation() { return appreciation; }
    public void setAppreciation(String appreciation) { this.appreciation = appreciation; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public int getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(int anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }

    public String getTypeEvaluation() { return typeEvaluation; }
    public void setTypeEvaluation(String typeEvaluation) { this.typeEvaluation = typeEvaluation; }



    public String getNoteCouleur() {
        if (note < 10) return "#ef4444"; // Rouge
        else if (note < 12) return "#f59e0b"; // Orange
        else if (note < 14) return "#84cc16"; // Lime
        else return "#10b981"; // Vert
    }

    public String getNoteTextuelle() {
        if (note < 5) return "Très faible";
        else if (note < 10) return "Insuffisant";
        else if (note < 12) return "Passable";
        else if (note < 14) return "Assez bien";
        else if (note < 16) return "Bien";
        else return "Très bien";
    }

    public boolean isValide() {
        return note >= 10;
    }
}