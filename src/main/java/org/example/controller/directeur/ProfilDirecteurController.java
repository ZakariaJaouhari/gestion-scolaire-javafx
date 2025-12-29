package org.example.controller.directeur;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.dao.DirecteurDAO;
import org.example.model.Directeur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.util.Optional;

public class ProfilDirecteurController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // INFORMATIONS PERSONNELLES
    @FXML private Label academieLabel;
    @FXML private Label nomLabel;
    @FXML private Label nomDirecteurInfoLabel;
    @FXML private Label directionLabel;
    @FXML private Label anneeLabel;
    @FXML private Label emailLabel;

    // CHANGEMENT DE MOT DE PASSE
    @FXML private PasswordField ancienPasswordField;
    @FXML private PasswordField nouveauPasswordField;
    @FXML private PasswordField confirmerPasswordField;
    @FXML private Button modifierPasswordButton;

    private DirecteurDAO directeurDAO;
    private Directeur directeurConnecte;

    @FXML
    public void initialize() {
        directeurDAO = new DirecteurDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            // Récupérer le directeur connecté
            directeurConnecte = session.getCurrentDirecteur();
            if (directeurConnecte != null) {
                nomEcoleLabel.setText(directeurConnecte.getNomEcole());
                nomDirecteurLabel.setText(directeurConnecte.getNomDirecteur());

                // Charger les informations complètes du directeur depuis la base
                chargerInformationsDirecteur();
            }
        }
        versionLabel.setText("V 0.1.0");

        // Configurer les listeners pour la validation du mot de passe
        configurerValidationMotDePasse();
    }

    private void chargerInformationsDirecteur() {
        if (directeurConnecte != null) {
            // Recharger le directeur depuis la base pour avoir les informations à jour
            Optional<Directeur> directeurFromDB = directeurDAO.findById(directeurConnecte.getId());

            if (directeurFromDB.isPresent()) {
                Directeur directeur = directeurFromDB.get();

                // Afficher les informations personnelles
                nomLabel.setText(directeur.getNomEcole() != null ? directeur.getNomEcole() : "Non spécifié");
                nomDirecteurInfoLabel.setText(directeur.getNomDirecteur() != null ? directeur.getNomDirecteur() : "Non spécifié");
                academieLabel.setText(directeur.getAcademie() != null ? directeur.getAcademie() : "Non spécifié");
                directionLabel.setText(directeur.getDirection() != null ? directeur.getDirection() : "Non spécifié");
                anneeLabel.setText(directeur.getAnnee() != null ? directeur.getAnnee() : "Non spécifié");
                emailLabel.setText(directeur.getEmail() != null ? directeur.getEmail() : "Non spécifié");
            }
        }
    }

    private void configurerValidationMotDePasse() {
        // Désactiver le bouton initialement
        modifierPasswordButton.setDisable(true);

        // Ajouter des listeners pour valider les champs
        ancienPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validerChampsMotDePasse());
        nouveauPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validerChampsMotDePasse());
        confirmerPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validerChampsMotDePasse());
    }

    private void validerChampsMotDePasse() {
        String ancien = ancienPasswordField.getText();
        String nouveau = nouveauPasswordField.getText();
        String confirmer = confirmerPasswordField.getText();

        boolean ancienValide = !ancien.isEmpty();
        boolean nouveauValide = !nouveau.isEmpty() && nouveau.length() >= 6;
        boolean confirmerValide = !confirmer.isEmpty() && confirmer.equals(nouveau);

        modifierPasswordButton.setDisable(!(ancienValide && nouveauValide && confirmerValide));
    }

    // =====================================================================
    // ================ BOUTON : CHANGER MOT DE PASSE ======================
    // =====================================================================
    @FXML
    private void handleChangerMotDePasse() {
        if (directeurConnecte == null) {
            showAlert("Erreur", "Non connecté",
                    "Vous devez être connecté pour modifier votre mot de passe.",
                    Alert.AlertType.ERROR);
            return;
        }

        String ancienPassword = ancienPasswordField.getText();
        String nouveauPassword = nouveauPasswordField.getText();
        String confirmerPassword = confirmerPasswordField.getText();

        // Validation
        if (ancienPassword.isEmpty() || nouveauPassword.isEmpty() || confirmerPassword.isEmpty()) {
            showAlert("Erreur", "Champs manquants",
                    "Veuillez remplir tous les champs.",
                    Alert.AlertType.ERROR);
            return;
        }

        if (!nouveauPassword.equals(confirmerPassword)) {
            showAlert("Erreur", "Mots de passe différents",
                    "Le nouveau mot de passe et la confirmation ne correspondent pas.",
                    Alert.AlertType.ERROR);
            return;
        }

        if (nouveauPassword.length() < 6) {
            showAlert("Erreur", "Mot de passe trop court",
                    "Le nouveau mot de passe doit contenir au moins 6 caractères.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Vérifier l'ancien mot de passe (avec hash)
        Optional<Directeur> directeurFromDB = directeurDAO.findById(directeurConnecte.getId());
        if (directeurFromDB.isPresent()) {
            Directeur directeur = directeurFromDB.get();

            // 🔒 HASHER l'ancien mot de passe pour comparaison
            String hashedAncienPassword = DigestUtils.sha256Hex(ancienPassword);

            if (!directeur.getPassword().equals(hashedAncienPassword)) {
                showAlert("Erreur", "Mot de passe incorrect",
                        "L'ancien mot de passe est incorrect.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer le changement");
            confirm.setHeaderText("Changement de mot de passe");
            confirm.setContentText("Êtes-vous sûr de vouloir changer votre mot de passe ?");

            ButtonType buttonTypeOui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.NO);
            confirm.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == buttonTypeOui) {
                // 🔒 HASHER le nouveau mot de passe avant de le sauvegarder
                String hashedNouveauPassword = DigestUtils.sha256Hex(nouveauPassword);

                // Mettre à jour le mot de passe (stocké avec hash)
                directeur.setPassword(hashedNouveauPassword);

                if (directeurDAO.update(directeur)) {
                    // Mettre à jour la session (mais garder le hash)
                    directeurConnecte.setPassword(hashedNouveauPassword);

                    showAlert("Succès", "Mot de passe modifié",
                            "Votre mot de passe a été modifié avec succès.",
                            Alert.AlertType.INFORMATION);

                    // Réinitialiser les champs
                    ancienPasswordField.clear();
                    nouveauPasswordField.clear();
                    confirmerPasswordField.clear();
                    modifierPasswordButton.setDisable(true);
                } else {
                    showAlert("Erreur", "Échec de la mise à jour",
                            "Une erreur est survenue lors de la modification du mot de passe.",
                            Alert.AlertType.ERROR);
                }
            }
        }
    }

    // =====================================================================
    // ======================= NAVIGATION ==================================
    // =====================================================================
    @FXML
    private void handleHome() {
        try {
            StageManager.loadScene("/view/directeur/dashboard.fxml", "/styles/dashboard.css", "Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFormateurs() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Formateurs/gestionFormateurs.fxml", "/styles/gestionFormateurs.css", "gesttion des Formateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEtudiants() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Etudiants/gestionEtudiants.fxml", "/styles/gestionFormateurs.css", "gestion des Etudiants");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGroupes() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Groupes/gestionGroupes.fxml", "/styles/gestionFormateurs.css", "gestion des Groupes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModules() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Modules/gestionModules.fxml", "/styles/gestionFormateurs.css", "gestion des Modules");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotes() {
        try {
            StageManager.loadScene("/view/directeur/gestionNotesDirecteur.fxml",
                    "/styles/gestionFormateurs.css", "Gestion des Notes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCertificats() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Planning/PlanningSemaine.fxml", "/styles/gestionFormateurs.css", "Planning");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        System.out.println("Déconnexion demandée");
        SessionManager.getInstance().clearSession();
        redirectToLogin();
    }

    private void redirectToLogin() {
        try {
            StageManager.loadScene("/view/login_register.fxml", "/styles/auth.css", "Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfilDirecteur() {
        try {
            StageManager.loadScene("/view/directeur/profilDirecteur.fxml",
                    "/styles/profil.css", "Mon Profil - Directeur");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible d'ouvrir la page profil.", Alert.AlertType.ERROR);
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

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow(); // n'importe quel champ
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);


    }
}