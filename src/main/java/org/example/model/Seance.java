package org.example.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Seance {
    private int id;
    private int moduleId;
    private int groupeId;
    private int formateurId;
    private int directeurId; // nouveau champ
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String salle;

    // Relations utiles
    private Module module;
    private Groupe groupe;
    private Formateur formateur;

    // Constructeur
    public Seance() {}

    public Seance(int moduleId, int groupeId, int formateurId, int directeurId,
                  LocalDate date, LocalTime heureDebut, LocalTime heureFin, String salle) {
        this.moduleId = moduleId;
        this.groupeId = groupeId;
        this.formateurId = formateurId;
        this.directeurId = directeurId;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public int getGroupeId() { return groupeId; }
    public void setGroupeId(int groupeId) { this.groupeId = groupeId; }

    public int getFormateurId() { return formateurId; }
    public void setFormateurId(int formateurId) { this.formateurId = formateurId; }

    public int getDirecteurId() { return directeurId; }
    public void setDirecteurId(int directeurId) { this.directeurId = directeurId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public Groupe getGroupe() { return groupe; }
    public void setGroupe(Groupe groupe) { this.groupe = groupe; }

    public Formateur getFormateur() { return formateur; }
    public void setFormateur(Formateur formateur) { this.formateur = formateur; }
}
