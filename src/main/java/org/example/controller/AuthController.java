package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.dao.DirecteurDAO;
import org.example.model.Directeur;
import org.example.util.SessionManager;
import org.example.util.StageManager;
import java.io.IOException;
import java.util.Optional;

public class AuthController {

    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Button registerButton;

    private DirecteurDAO directeurDAO = new DirecteurDAO();

    @FXML
    private void login() {
        System.out.println("=== TENTATIVE DE CONNEXION ===");

        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();

        // Validation basique
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Champs manquants",
                    "Veuillez remplir tous les champs.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            // Chercher le directeur par email
            Optional<Directeur> directeurOpt = directeurDAO.findByEmail(email);

            if (directeurOpt.isPresent()) {
                Directeur directeur = directeurOpt.get();

                // Vérifier le mot de passe (dans une vraie app, il faudrait hasher)
                if (directeur.getPassword().equals(password)) {
                    System.out.println("✅ Connexion réussie pour: " + directeur.getNomDirecteur());

                    // Sauvegarder l'utilisateur connecté (singleton)
                    SessionManager.getInstance().setCurrentDirecteur(directeur);

                    // Afficher message de bienvenue
                    showAlert("Succès", "Connexion réussie",
                            "Bienvenue " + directeur.getNomDirecteur() + " !",
                            Alert.AlertType.INFORMATION);

                    // Rediriger vers le dashboard
                    switchToDashboard();

                } else {
                    System.out.println("❌ Mot de passe incorrect");
                    showAlert("Erreur", "Authentification échouée",
                            "Mot de passe incorrect.",
                            Alert.AlertType.ERROR);
                    loginPassword.clear();
                }
            } else {
                System.out.println("❌ Email non trouvé: " + email);
                showAlert("Erreur", "Compte non trouvé",
                        "Aucun compte trouvé avec cet email. Voulez-vous vous inscrire ?",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur technique lors du login: " + e.getMessage());
            showAlert("Erreur", "Erreur technique",
                    "Une erreur est survenue: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void switchToRegister() {
        try {
            StageManager.loadScene("/view/signup.fxml", "/styles/auth.css", "Inscription - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors de la navigation vers l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void switchToDashboard() {
        try {
            // Charger le dashboard selon le rôle
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
        System.out.println("AuthController initialisé");

        // Optionnel: Entrée sur le champ mot de passe pour login
        loginPassword.setOnAction(event -> login());

        // Optionnel: Focus automatique
        loginEmail.requestFocus();
    }
}