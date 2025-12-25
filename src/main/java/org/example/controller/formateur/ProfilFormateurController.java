package org.example.controller.formateur;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.dao.FormateurDAO;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProfilFormateurController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomFormateurLabel;
    @FXML private Label versionLabel;

    // INFORMATIONS PERSONNELLES
    @FXML private Label cinLabel;
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label matriculeLabel;
    @FXML private Label dateNaissanceLabel;
    @FXML private Label sexeLabel;
    @FXML private Label situationLabel;
    @FXML private Label emailLabel;
    @FXML private Label dateRecrutementLabel;
    @FXML private Label ageLabel;
    @FXML private Label ancienneteLabel;

    // CHANGEMENT DE MOT DE PASSE
    @FXML private PasswordField ancienPasswordField;
    @FXML private PasswordField nouveauPasswordField;
    @FXML private PasswordField confirmerPasswordField;
    @FXML private Button modifierPasswordButton;

    private FormateurDAO formateurDAO;
    private Formateur formateurConnecte;

    @FXML
    public void initialize() {
        formateurDAO = new FormateurDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());

            // Récupérer le formateur connecté
            formateurConnecte = session.getCurrentFormateur();
            if (formateurConnecte != null) {
                nomFormateurLabel.setText(formateurConnecte.getNom() + " " + formateurConnecte.getPrenom());

                // Charger les informations complètes du formateur depuis la base
                chargerInformationsFormateur();
            }
        }
        versionLabel.setText("V 0.1.0");

        // Configurer les listeners pour la validation du mot de passe
        configurerValidationMotDePasse();
    }

    private void chargerInformationsFormateur() {
        if (formateurConnecte != null) {
            // Recharger le formateur depuis la base pour avoir les informations à jour
            Optional<Formateur> formateurFromDB = Optional.ofNullable(formateurDAO.findById(formateurConnecte.getId()));

            if (formateurFromDB.isPresent()) {
                Formateur formateur = formateurFromDB.get();

                // Afficher les informations personnelles
                cinLabel.setText(formateur.getCin() != null ? formateur.getCin() : "Non spécifié");
                nomLabel.setText(formateur.getNom() != null ? formateur.getNom() : "Non spécifié");
                prenomLabel.setText(formateur.getPrenom() != null ? formateur.getPrenom() : "Non spécifié");
                matriculeLabel.setText(formateur.getMatricule() != null ? formateur.getMatricule() : "Non spécifié");

                if (formateur.getDateNaissance() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    dateNaissanceLabel.setText(formateur.getDateNaissance().format(formatter));

                    // Calculer l'âge
                    int age = java.time.Period.between(formateur.getDateNaissance(), java.time.LocalDate.now()).getYears();
                    ageLabel.setText(age + " ans");
                } else {
                    dateNaissanceLabel.setText("Non spécifiée");
                    ageLabel.setText("Non spécifié");
                }

                sexeLabel.setText(formateur.getSexe() != null ? formateur.getSexe().getValeur() : "Non spécifié");
                situationLabel.setText(formateur.getSituation() != null ? formateur.getSituation().getValeur() : "Non spécifié");
                emailLabel.setText(formateur.getEmail() != null ? formateur.getEmail() : "Non spécifié");

                if (formateur.getDateRecrutement() != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    dateRecrutementLabel.setText(formateur.getDateRecrutement().format(formatter));

                    // Calculer l'ancienneté
                    int anciennete = java.time.Period.between(formateur.getDateRecrutement(), java.time.LocalDate.now()).getYears();
                    ancienneteLabel.setText(anciennete + " ans");
                } else {
                    dateRecrutementLabel.setText("Non spécifiée");
                    ancienneteLabel.setText("Non spécifié");
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
        if (formateurConnecte == null) {
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
        Optional<Formateur> formateurFromDB = Optional.ofNullable(formateurDAO.findById(formateurConnecte.getId()));
        if (formateurFromDB.isPresent()) {
            Formateur formateur = formateurFromDB.get();

            // 🔒 HASHER l'ancien mot de passe pour comparaison
            String hashedAncienPassword = DigestUtils.sha256Hex(ancienPassword);

            if (!formateur.getPassword().equals(hashedAncienPassword)) {
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
                formateur.setPassword(hashedNouveauPassword);

                if (formateurDAO.update(formateur)) {
                    // Mettre à jour la session (mais garder le hash)
                    formateurConnecte.setPassword(hashedNouveauPassword);

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

    // Méthodes de navigation
    @FXML
    private void handleHome() {
        try {
            StageManager.loadScene("/view/formateur/dashboardF.fxml", "/styles/dashboardF.css", "Planning");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        try {
            StageManager.loadScene("/view/formateur/profilFormateur.fxml", "/styles/gestionFormateurs.css", "Profil");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGroupes() {
        try {
            StageManager.loadScene("/view/formateur/groupesFormateur.fxml", "/styles/gestionFormateurs.css", "Groupes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModules() {
        try {
            StageManager.loadScene("/view/formateur/modulesFormateur.fxml", "/styles/gestionFormateurs.css", "Modules");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotes() {
        try {
            StageManager.loadScene("/view/formateur/saisieNotes.fxml", "/styles/gestionFormateurs.css", "Notes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEmploiDuTemps() {
        try {
            StageManager.loadScene("/view/formateur/planningFormateur.fxml", "/styles/gestionFormateurs.css", "Planning");
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