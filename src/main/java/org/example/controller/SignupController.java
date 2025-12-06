package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.dao.DirecteurDAO;
import org.example.model.Directeur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;

public class SignupController {

    @FXML private TextField nomDirecteurField;
    @FXML private TextField nomEcoleField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button loginButton;

    private DirecteurDAO directeurDAO = new DirecteurDAO();

    @FXML
    private void register() {
        System.out.println("=== TENTATIVE D'INSCRIPTION ===");

        String nomDirecteur = nomDirecteurField.getText().trim();
        String nomEcole = nomEcoleField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (nomDirecteur.isEmpty() || nomEcole.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Erreur", "Champs manquants",
                    "Veuillez remplir tous les champs.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Erreur", "Mots de passe différents",
                    "Les mots de passe ne correspondent pas.",
                    Alert.AlertType.ERROR);
            passwordField.clear();
            confirmPasswordField.clear();
            return;
        }

        if (password.length() < 6) {
            showAlert("Erreur", "Mot de passe trop court",
                    "Le mot de passe doit contenir au moins 6 caractères.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Vérifier si l'email existe déjà
        if (directeurDAO.emailExists(email)) {
            showAlert("Erreur", "Email déjà utilisé",
                    "Un compte existe déjà avec cet email.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            // Créer le directeur
            Directeur directeur = new Directeur();
            directeur.setNomDirecteur(nomDirecteur);
            directeur.setNomEcole(nomEcole);
            directeur.setEmail(email);
            directeur.setPassword(password); // Dans une vraie app, il faudrait hasher

            // CORRECTION ICI : La méthode create() retourne un int (ID), pas un boolean
            int directeurId = directeurDAO.create(directeur);

            if (directeurId > 0) {
                System.out.println("✅ Inscription réussie, ID: " + directeurId);

                // Récupérer le directeur créé pour la session
                var directeurOpt = directeurDAO.findById(directeurId);
                if (directeurOpt.isPresent()) {
                    Directeur createdDirecteur = directeurOpt.get();

                    // Sauvegarder dans la session
                    SessionManager.getInstance().setCurrentDirecteur(createdDirecteur);

                    // Rediriger vers le dashboard
                    switchToDashboard();
                } else {
                    showAlert("Erreur", "Problème technique",
                            "Compte créé mais impossible de récupérer les informations.",
                            Alert.AlertType.WARNING);
                }
            } else {
                System.out.println("❌ Échec de l'inscription");
                showAlert("Erreur", "Échec de l'inscription",
                        "Une erreur est survenue lors de la création du compte.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur technique lors de l'inscription: " + e.getMessage());
            showAlert("Erreur", "Erreur technique",
                    "Une erreur est survenue: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToLogin() {
        try {
            StageManager.loadScene("/view/login.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation vers la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToDashboard() {
        try {
            StageManager.loadScene("/view/dashboard.fxml", "/styles/dashboard.css", "Tableau de bord - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        System.out.println("SignupController initialisé");

        // Optionnel: Entrée sur le champ de confirmation pour register
        confirmPasswordField.setOnAction(event -> register());

        // Optionnel: Focus automatique
        nomDirecteurField.requestFocus();
    }
}