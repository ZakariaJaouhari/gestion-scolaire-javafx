package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import org.example.util.StageManager;

import java.io.IOException;

public class AuthController {

    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Button registerButton;

    @FXML
    public void initialize() {
        System.out.println("AuthController initialisé!");
    }

    @FXML
    public void login() {
        System.out.println("Tentative de connexion avec:");
        System.out.println("Email: " + loginEmail.getText());
        System.out.println("Mot de passe: " + loginPassword.getText());
    }

    @FXML
    public void switchToRegister() {
        try {
            StageManager.loadScene("/view/signup.fxml", "/styles/sign_up.css", "Inscription - Gestion Scolaire");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page d'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }
}