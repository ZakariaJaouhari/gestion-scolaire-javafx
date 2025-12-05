package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.util.StageManager;

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

    @FXML
    public void initialize() {
        System.out.println("SignupController initialisé!");
    }

    @FXML
    public void register() {
        System.out.println("Tentative d'inscription avec:");
        System.out.println("Nom école: " + nomEcole.getText());
        System.out.println("Nom directeur: " + nomDirecteur.getText());
        System.out.println("Email: " + email.getText());
        System.out.println("Mot de passe: " + password.getText());
        System.out.println("Année scolaire: " + annee.getText());
        System.out.println("Académie: " + academie.getText());
        System.out.println("Direction: " + direction.getText());
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