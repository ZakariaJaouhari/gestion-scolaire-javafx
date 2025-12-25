package org.example.model;

import java.time.LocalDate;
import java.util.List;

public class Bulletin {
    private int id;
    private int etudiantId;
    private Etudiant etudiant;
    private int anneeScolaire;
    private String semestre;
    private double moyenneGenerale;
    private int rang;
    private int effectifClasse;
    private String appreciationGenerale;
    private LocalDate dateEdition;
    private List<Note> notes;

    // Getters et Setters
    // ... (similaire à Note)

    public void calculerMoyenne() {
        if (notes == null || notes.isEmpty()) {
            moyenneGenerale = 0;
            return;
        }

        double somme = 0;
        double totalCoefficients = 0;

        for (Note note : notes) {
            if (note.getModule() != null) {
                int coefficient = note.getModule().getCoefficient();
                somme += note.getNote() * coefficient;
                totalCoefficients += coefficient;
            }
        }

        moyenneGenerale = totalCoefficients > 0 ? somme / totalCoefficients : 0;
    }

    public String getMention() {
        if (moyenneGenerale < 10) return "Non admis";
        else if (moyenneGenerale < 12) return "Passable";
        else if (moyenneGenerale < 14) return "Assez bien";
        else if (moyenneGenerale < 16) return "Bien";
        else return "Très bien";
    }
}