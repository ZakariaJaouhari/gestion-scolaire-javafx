package org.example.model;

import java.time.LocalDate;
import java.util.List;

public class Module {

    private int id;
    private String nom;
    private String matricule;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String heuresPratique;
    private int coefficient;
    private int formateurId;
    private int directeurId;

    // Relations
    private Formateur formateur;
    private List<Groupe> groupes;

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getHeuresPratique() { return heuresPratique; }
    public void setHeuresPratique(String heuresPratique) { this.heuresPratique = heuresPratique; }

    public int getCoefficient() { return coefficient; }
    public void setCoefficient(int coefficient) { this.coefficient = coefficient; }

    public int getFormateurId() { return formateurId; }
    public void setFormateurId(int formateurId) { this.formateurId = formateurId; }

    public int getDirecteurId() { return directeurId; }
    public void setDirecteurId(int directeurId) { this.directeurId = directeurId; }

    public Formateur getFormateur() { return formateur; }
    public void setFormateur(Formateur formateur) { this.formateur = formateur; }

    public List<Groupe> getGroupes() { return groupes; }
    public void setGroupes(List<Groupe> groupes) { this.groupes = groupes; }
}
