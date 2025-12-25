package org.example.controller.etudiant;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.dao.EtudiantDAO;
import org.example.model.Etudiant;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProfilEtudiantController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomEtudiantLabel;
    @FXML private Label versionLabel;

    // INFORMATIONS PERSONNELLES
    @FXML private Label cinLabel;
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label dateNaissanceLabel;
    @FXML private Label sexeLabel;
    @FXML private Label emailLabel;
    @FXML private Label groupeLabel;
    @FXML private Label ageLabel;

    // CHANGEMENT DE MOT DE PASSE
    @FXML private PasswordField ancienPasswordField;
    @FXML private PasswordField nouveauPasswordField;
    @FXML private PasswordField confirmerPasswordField;
    @FXML private Button modifierPasswordButton;

    // PHOTO DE PROFIL
    @FXML private ImageView photoProfilImageView;
    @FXML private Button changerPhotoButton;

    private EtudiantDAO etudiantDAO;
    private Etudiant etudiantConnecte;

    @FXML
    public void initialize() {
        etudiantDAO = new EtudiantDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());

            // Récupérer l'étudiant connecté
            etudiantConnecte = session.getCurrentEtudiant();
            if (etudiantConnecte != null) {
                nomEtudiantLabel.setText(etudiantConnecte.getNom() + " " + etudiantConnecte.getPrenom());

                // Charger les informations complètes de l'étudiant depuis la base
                chargerInformationsEtudiant();
            }
        }
        versionLabel.setText("V 0.1.0");

        // Configurer les listeners pour la validation du mot de passe
        configurerValidationMotDePasse();
    }

    private void chargerInformationsEtudiant() {
        if (etudiantConnecte != null) {
            // Recharger l'étudiant depuis la base pour avoir les informations à jour
            Optional<Etudiant> etudiantFromDB = etudiantDAO.findById(etudiantConnecte.getId());

            if (etudiantFromDB.isPresent()) {
                Etudiant etudiant = etudiantFromDB.get();

                // Afficher les informations personnelles
                cinLabel.setText(etudiant.getCin() != null ? etudiant.getCin() : "Non spécifié");
                nomLabel.setText(etudiant.getNom() != null ? etudiant.getNom() : "Non spécifié");
                prenomLabel.setText(etudiant.getPrenom() != null ? etudiant.getPrenom() : "Non spécifié");

                if (etudiant.getDateNaissance() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    dateNaissanceLabel.setText(etudiant.getDateNaissance().format(formatter));

                    // Calculer l'âge
                    int age = java.time.Period.between(etudiant.getDateNaissance(), java.time.LocalDate.now()).getYears();
                    ageLabel.setText(age + " ans");
                } else {
                    dateNaissanceLabel.setText("Non spécifiée");
                    ageLabel.setText("Non spécifié");
                }

                sexeLabel.setText(etudiant.getSexe() != null ? etudiant.getSexe().getValeur() : "Non spécifié");
                emailLabel.setText(etudiant.getEmail() != null ? etudiant.getEmail() : "Non spécifié");

                // Afficher le groupe
                if (etudiant.getGroupe() != null) {
                    groupeLabel.setText(etudiant.getGroupe().getMatricule() + " (" + etudiant.getGroupe().getNiveau().getValeur() + ")");
                } else if (etudiant.getGroupeId() > 0) {
                    groupeLabel.setText("Groupe ID: " + etudiant.getGroupeId());
                } else {
                    groupeLabel.setText("Non assigné");
                }
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
        if (etudiantConnecte == null) {
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
        Optional<Etudiant> etudiantFromDB = etudiantDAO.findById(etudiantConnecte.getId());
        if (etudiantFromDB.isPresent()) {
            Etudiant etudiant = etudiantFromDB.get();

            // 🔒 HASHER l'ancien mot de passe pour comparaison
            String hashedAncienPassword = DigestUtils.sha256Hex(ancienPassword);

            if (!etudiant.getPassword().equals(hashedAncienPassword)) {
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
                etudiant.setPassword(hashedNouveauPassword);

                if (etudiantDAO.update(etudiant)) {
                    // Mettre à jour la session (mais garder le hash)
                    etudiantConnecte.setPassword(hashedNouveauPassword);

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

    @FXML
    private void handleChangerPhoto() {
        // Implémentation simplifiée pour le moment
        showAlert("Information", "Fonctionnalité en développement",
                "La fonctionnalité de changement de photo sera disponible prochainement.",
                Alert.AlertType.INFORMATION);
    }

    // Méthodes de navigation
    @FXML
    private void handleHome() {
        try {
            StageManager.loadScene("/view/etudiant/dashboardE.fxml",
                    "/styles/dashboard.css", "Dashboard Étudiant");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotes() {
        System.out.println("Mes Notes clicked");
        // Rediriger vers la page des notes
        // StageManager.loadScene("/view/etudiant/notes.fxml", ...);
    }

    @FXML
    private void handleEmploiDuTemps() {
        try {
            StageManager.loadScene("/view/etudiant/planningEtudiant.fxml", "/styles/gestionFormateurs.css", "Planning");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleModules(MouseEvent mouseEvent) {
        try {
            StageManager.loadScene("/view/etudiant/modulesEtudiant.fxml",
                    "/styles/gestionFormateurs.css", "Mes Modules");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfil() {
        try {
            StageManager.loadScene("/view/etudiant/profilEtudiant.fxml",
                    "/styles/profil.css", "Mon Profil");
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

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }
}