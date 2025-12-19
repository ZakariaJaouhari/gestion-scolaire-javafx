package org.example.model;

import java.time.LocalDate;
import java.util.List;

public class Module {

    private int id;
    private String nom;
    private String matricule;
    private LocalDate date_D;
    private LocalDate date_F;
    private String heures_P;
    private int coefficient;
    private int formateurId;
    private int directeurId;

    // Relations
    private Formateur formateur;
    private List<Groupe> groupes;

    // ✅ CONSTRUCTEUR VIDE (OBLIGATOIRE POUR DAO / FXML / JDBC)
    public Module() {
    }

    // Constructeur complet
    public Module(String nom, String matricule, LocalDate date_D, LocalDate date_F,
                  String heures_P, Integer coefficient, int formateurId, int directeurId) {
        this.nom = nom;
        this.matricule = matricule;
        this.date_D = date_D;
        this.date_F = date_F;
        this.heures_P = heures_P;
        this.coefficient = coefficient;
        this.formateurId = formateurId;
        this.directeurId = directeurId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public LocalDate getDateDebut() { return date_D; }
    public void setDateDebut(LocalDate date_D) { this.date_D = date_D; }

    public LocalDate getDateFin() { return date_F; }
    public void setDateFin(LocalDate date_F) { this.date_F = date_F; }

    public String getHeuresPratique() { return heures_P; }
    public void setHeuresPratique(String heures_P) { this.heures_P = heures_P; }

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
