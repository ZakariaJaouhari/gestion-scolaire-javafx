package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
    @FXML private TextField annee;
    @FXML private TextField academie;
    @FXML private TextField direction;
    @FXML private Button loginButton;

    private DirecteurDAO directeurDAO = new DirecteurDAO();

    @FXML
    private void register() {
        System.out.println("=== TENTATIVE D'INSCRIPTION ===");

        String nomDirecteur = nomDirecteurField.getText().trim();
        String nomEcole = nomEcoleField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String anneeScolaire = annee.getText().trim();
        String academieValue = academie.getText().trim();
        String directionValue = direction.getText().trim();


        // Validation
        if (nomDirecteur.isEmpty() || nomEcole.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Champs manquants",
                    "Veuillez remplir tous les champs.",
                    Alert.AlertType.WARNING);
            return;
        }
        if (anneeScolaire.isEmpty() || academieValue.isEmpty() || directionValue.isEmpty()) {
            showAlert("Erreur", "Champs manquants",
                    "Veuillez remplir toutes les informations de l'école.",
                    Alert.AlertType.WARNING);
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

            // Validation minimale pour les nouveaux champs
            if (anneeScolaire.isEmpty() || academieValue.isEmpty() || directionValue.isEmpty()) {
                showAlert("Erreur", "Champs manquants",
                        "Veuillez remplir toutes les informations de l'école.",
                        Alert.AlertType.WARNING);
                return;
            }

            // Créer le directeur
            Directeur directeur = new Directeur();
            directeur.setNomDirecteur(nomDirecteur);
            directeur.setNomEcole(nomEcole);
            directeur.setEmail(email);

            // 🔒 Hasher le mot de passe avant stockage
            String hashedPassword = org.apache.commons.codec.digest.DigestUtils.sha256Hex(password);
            directeur.setPassword(hashedPassword);

            // Ajouter les champs supplémentaires
            directeur.setAnnee(anneeScolaire);
            directeur.setAcademie(academieValue);
            directeur.setDirection(directionValue);

            // CORRECTION: create() retourne l'ID du directeur
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
            StageManager.loadScene("/view/login_register.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation vers la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToDashboard() {
        try {
            StageManager.loadScene("/view/directeur/dashboard.fxml", "/styles/dashboard.css", "Dashboard");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(
            String title,
            String header,
            String content,
            Alert.AlertType type
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) nomEcoleField.getScene().getWindow(); // n'importe quel champ
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);


    }

    @FXML
    private void initialize() {
        System.out.println("SignupController initialisé");


        // Optionnel: Focus automatique
        nomDirecteurField.requestFocus();
    }
}