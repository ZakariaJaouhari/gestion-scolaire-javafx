package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.util.StageManager;
import org.example.model.Directeur; // ⚠️ MANQUANT
import org.example.dao.DirecteurDAO; // ⚠️ MANQUANT
import java.util.regex.Pattern;
import java.io.IOException;

public class SignupController {

    @FXML private TextField nomEcole;
    @FXML private TextField nomDirecteur;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private TextField annee;
    @FXML private TextField academie;
    @FXML private TextField direction;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    private DirecteurDAO directeurDAO = new DirecteurDAO();

    // Méthode appelée quand on clique sur "S'inscrire"
    @FXML
    private void register() {
        if (validateForm()) {
            try {
                // Créer l'objet Directeur
                Directeur nouveauDirecteur = new Directeur(
                        nomEcole.getText().trim(),
                        nomDirecteur.getText().trim(),
                        academie.getText().trim(),
                        direction.getText().trim(),
                        annee.getText().trim(),
                        email.getText().trim(),
                        password.getText()  // Note: À hasher en production!
                );

                // Insérer dans la base de données
                boolean success = directeurDAO.create(nouveauDirecteur);

                if (success) {
                    showAlert("Succès", "Inscription réussie !",
                            "Le directeur a été ajouté avec succès.",
                            Alert.AlertType.INFORMATION);

                    // Optionnel: Rediriger vers la page de connexion
                    switchToLogin();

                    // Optionnel: Vider le formulaire
                    clearForm();
                } else {
                    showAlert("Erreur", "Échec de l'inscription",
                            "L'email existe peut-être déjà dans la base.",
                            Alert.AlertType.ERROR);
                }

            } catch (Exception e) {
                showAlert("Erreur", "Erreur technique",
                        "Une erreur est survenue: " + e.getMessage(),
                        Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    // Méthode de validation du formulaire
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Validation du nom d'école
        if (nomEcole.getText().trim().isEmpty()) {
            errors.append("• Le nom de l'école est obligatoire\n");
        } else if (nomEcole.getText().trim().length() < 3) {
            errors.append("• Le nom de l'école doit faire au moins 3 caractères\n");
        }

        // Validation du nom du directeur
        if (nomDirecteur.getText().trim().isEmpty()) {
            errors.append("• Le nom du directeur est obligatoire\n");
        }

        // Validation de l'email
        if (email.getText().trim().isEmpty()) {
            errors.append("• L'email est obligatoire\n");
        } else if (!isValidEmail(email.getText().trim())) {
            errors.append("• Format d'email invalide\n");
        }

        // Validation du mot de passe
        if (password.getText().isEmpty()) {
            errors.append("• Le mot de passe est obligatoire\n");
        } else if (password.getText().length() < 6) {
            errors.append("• Le mot de passe doit faire au moins 6 caractères\n");
        }

        // Validation de l'année scolaire
        if (annee.getText().trim().isEmpty()) {
            errors.append("• L'année scolaire est obligatoire\n");
        }

        // Validation de l'académie
        if (academie.getText().trim().isEmpty()) {
            errors.append("• L'académie est obligatoire\n");
        }

        // Validation de la direction
        if (direction.getText().trim().isEmpty()) {
            errors.append("• La direction est obligatoire\n");
        }

        // Si des erreurs, afficher l'alerte
        if (errors.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs",
                    errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    // Vérification du format email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    // Méthode pour vider le formulaire
    private void clearForm() {
        nomEcole.clear();
        nomDirecteur.clear();
        email.clear();
        password.clear();
        annee.clear();
        academie.clear();
        direction.clear();
    }

    // Méthode pour afficher des alertes
    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void switchToLogin() {
        try {
            StageManager.loadScene("/view/login_register.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors du retour à la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}