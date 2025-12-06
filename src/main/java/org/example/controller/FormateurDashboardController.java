package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.dao.FormateurDAO;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;

public class FormateurDashboardController {

    @FXML private Label welcomeNameLabel;
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomFormateurLabel;
    @FXML private Label nombreGroupesLabel;
    @FXML private Label nombreStagiairesLabel;
    @FXML private Label nombreModulesLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox cardGroupes;
    @FXML private VBox cardStagiaires;
    @FXML private VBox cardModules;

    private FormateurDAO formateurDAO;

    @FXML
    public void initialize() {
        System.out.println("FormateurDashboardController initialisé");

        // Vérifier que l'utilisateur est bien un formateur
        SessionManager session = SessionManager.getInstance();
        if (!session.isFormateur()) {
            System.out.println("❌ Utilisateur non connecté ou pas formateur, redirection vers login");
            try {
                StageManager.loadScene("/view/login.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Récupérer le formateur connecté
        Formateur formateur = session.getCurrentFormateur();

        if (formateur != null) {
            System.out.println("✅ Formateur trouvé: " + formateur.getNomComplet());

            // Initialiser les labels - VÉRIFIEZ QUE LES IDs CORRESPONDENT AU FXML
            if (welcomeNameLabel != null) {
                welcomeNameLabel.setText(formateur.getPrenom());
            } else {
                System.err.println("⚠️ welcomeNameLabel est null dans le FXML");
            }

            if (nomFormateurLabel != null) {
                nomFormateurLabel.setText(formateur.getNomComplet());
            } else {
                System.err.println("⚠️ nomFormateurLabel est null dans le FXML");
            }

            if (nomEcoleLabel != null) {
                // Pour le nom de l'école, vous devez récupérer via le directeur
                nomEcoleLabel.setText("École ID: " + formateur.getDirecteurId());
            }

            // Initialiser les statistiques
            if (nombreGroupesLabel != null) {
                nombreGroupesLabel.setText("0");
            }
            if (nombreStagiairesLabel != null) {
                nombreStagiairesLabel.setText("0");
            }
            if (nombreModulesLabel != null) {
                nombreModulesLabel.setText("0");
            }

            System.out.println("✅ Dashboard formateur initialisé pour: " + formateur.getNomComplet());
        } else {
            System.err.println("❌ Formateur est null dans la session");
            try {
                StageManager.loadScene("/view/login.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Méthodes de navigation
    @FXML
    private void handleHome() {
        System.out.println("Home formateur clicked");
    }

    @FXML
    private void handleMesGroupes() {
        System.out.println("Mes Groupes clicked");
    }

    @FXML
    private void handleMesModules() {
        System.out.println("Mes Modules clicked");
    }

    @FXML
    private void handleNotes() {
        System.out.println("Notes clicked");
    }

    @FXML
    private void handleProfil() {
        System.out.println("Profil clicked");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            StageManager.loadScene("/view/login.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}