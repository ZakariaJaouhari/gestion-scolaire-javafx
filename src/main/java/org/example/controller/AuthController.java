package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.dao.DirecteurDAO;
import org.example.dao.EtudiantDAO;
import org.example.dao.FormateurDAO;
import org.example.model.Directeur;
import org.example.model.Etudiant;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.util.Optional;

public class AuthController {

    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Button registerButton;

    private DirecteurDAO directeurDAO = new DirecteurDAO();
    private FormateurDAO formateurDAO = new FormateurDAO();
    private EtudiantDAO etudiantDAO = new EtudiantDAO();

    @FXML
    private void login() {
        System.out.println("=== TENTATIVE DE CONNEXION AUTOMATIQUE ===");

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
            // Essayer d'abord comme DIRECTEUR
            Optional<Directeur> directeurOpt = directeurDAO.findByEmail(email);

            if (directeurOpt.isPresent()) {
                Directeur directeur = directeurOpt.get();

                if (directeur.getPassword().equals(password)) {
                    System.out.println("✅ Connexion DIRECTEUR réussie pour: " + directeur.getNomDirecteur());

                    // Sauvegarder dans la session
                    SessionManager.getInstance().setCurrentDirecteur(directeur);


                    // Afficher message de bienvenue
                    showAlert("Succès", "Connexion réussie",
                            "Bienvenue " + directeur.getNomDirecteur() + " !",
                            Alert.AlertType.INFORMATION);

                    switchToDashboard();
                    return;
                } else {
                    System.out.println("❌ Mot de passe directeur incorrect");
                }
            }

            // Si pas directeur ou mauvais mot de passe, essayer comme FORMATEUR
            Optional<Formateur> formateurOpt = formateurDAO.findByEmail(email);

            if (formateurOpt.isPresent()) {
                Formateur formateur = formateurOpt.get();

                if (formateur.getPassword().equals(password)) {
                    System.out.println("✅ Connexion FORMATEUR réussie pour: " + formateur.getNomComplet());

                    // Sauvegarder dans la session
                    SessionManager.getInstance().setCurrentFormateur(formateur);

                    // Rediriger vers dashboard formateur
                    switchToFormateurDashboard();
                    return;
                } else {
                    System.out.println("❌ Mot de passe formateur incorrect");
                }
            }

            // Si pas formateur ou mauvais mot de passe, essayer comme etudiant
            Optional<Etudiant> etudiantOpt = etudiantDAO.findByEmail(email);

            if (etudiantOpt.isPresent()) {
                Etudiant etudiant = etudiantOpt.get();

                if (etudiant.getPassword().equals(password)) {
                    System.out.println("✅ Connexion FORMATEUR réussie pour: " + etudiant.getNomComplet());

                    // Sauvegarder dans la session
                    SessionManager.getInstance().setCurrentEtudiant(etudiant);

                    // Rediriger vers dashboard formateur
                    switchToEtudiantDashboard();
                    return;
                } else {
                    System.out.println("❌ Mot de passe formateur incorrect");
                }
            }

            // Si on arrive ici, c'est que l'utilisateur n'existe pas ou mauvais mot de passe
            System.out.println("❌ Identifiants incorrects pour: " + email);
            showAlert("Erreur", "Identifiants incorrects",
                    "Email ou mot de passe incorrect. Vérifiez vos informations.",
                    Alert.AlertType.ERROR);
            loginPassword.clear();

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
            // Charger le dashboard directeur
            StageManager.loadScene("/view/directeur/dashboard.fxml", "/styles/dashboard.css", "Tableau de bord - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void switchToFormateurDashboard() {
        try {
            // Charger le dashboard formateur
            StageManager.loadScene("/view/formateur/dashboardF.fxml", "/styles/dashboardF.css", "Tableau de bord Formateur - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard formateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void switchToEtudiantDashboard() {
        try {
            // Charger le dashboard formateur
            StageManager.loadScene("/view/etudiant/dashboardE.fxml", "/styles/dashboard.css", "Tableau de bord Etudiant - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard etudiant: " + e.getMessage());
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
        System.out.println("AuthController initialisé - Détection automatique");

        // Entrée sur le champ mot de passe pour login
        loginPassword.setOnAction(event -> login());

        // Focus automatique
        loginEmail.requestFocus();
    }
}