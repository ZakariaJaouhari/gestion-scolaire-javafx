package org.example.controller.directeur.gestionformateurs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.FormateurDAO;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.IOException;
import java.time.LocalDate;

public class Modifier_Formateur_Controller {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    @FXML private Button modifierButton;
    @FXML private Button cancelButton;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField matriculeField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private ComboBox<String> situationComboBox;
    @FXML private ComboBox<String> dayComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private ComboBox<String> dayRComboBox;
    @FXML private ComboBox<String> monthRComboBox;
    @FXML private ComboBox<String> yearRComboBox;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;


    private Formateur formateurToEdit;
    private FormateurDAO formateurDAO = new FormateurDAO();


    @FXML
    public void initialize() {
        formateurDAO = new FormateurDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            nomDirecteurLabel.setText(session.getNomDirecteur());
        }
        versionLabel.setText("V 0.1.0");

        populateDateComboBoxes();

    }

    public void setFormateurToEdit(Formateur formateur) {
        this.formateurToEdit = formateur;

        nomField.setText(formateur.getNom());
        prenomField.setText(formateur.getPrenom());
        matriculeField.setText(formateur.getMatricule());
        cinField.setText(formateur.getCin());
        sexeComboBox.setValue(formateur.getSexe().getValeur());
        situationComboBox.setValue(formateur.getSituation().getValeur());

        if (formateur.getDateNaissance() != null) {
            dayComboBox.setValue(String.valueOf(formateur.getDateNaissance().getDayOfMonth()));
            monthComboBox.setValue(String.valueOf(formateur.getDateNaissance().getMonthValue()));
            yearComboBox.setValue(String.valueOf(formateur.getDateNaissance().getYear()));
        }

        if (formateur.getDateRecrutement() != null) {
            dayRComboBox.setValue(String.valueOf(formateur.getDateRecrutement().getDayOfMonth()));
            monthRComboBox.setValue(String.valueOf(formateur.getDateRecrutement().getMonthValue()));
            yearRComboBox.setValue(String.valueOf(formateur.getDateRecrutement().getYear()));
        }

        emailField.setText(formateur.getEmail());
    }

    @FXML
    private void saveFormateur() {
        if (formateurToEdit != null) {
            formateurToEdit.setNom(nomField.getText());
            formateurToEdit.setPrenom(prenomField.getText());
            formateurToEdit.setMatricule(matriculeField.getText());
            formateurToEdit.setCin(cinField.getText());
            formateurToEdit.setSexe(Formateur.Sexe.fromString(sexeComboBox.getValue()));
            formateurToEdit.setSituation(Formateur.Situation.fromString(situationComboBox.getValue()));

            // Convertir ComboBox en LocalDate
            LocalDate dateNaissance = getDateFromComboBoxes(
                    dayComboBox, monthComboBox, yearComboBox
            );

            LocalDate dateRecrutement = getDateFromComboBoxes(
                    dayRComboBox, monthRComboBox, yearRComboBox
            );

            // Si l'utilisateur n'a pas modifié → garder l'ancienne date
            if (dateNaissance == null) {
                dateNaissance = formateurToEdit.getDateNaissance();
            }

            if (dateRecrutement == null) {
                dateRecrutement = formateurToEdit.getDateRecrutement();
            }

            formateurToEdit.setDateNaissance(dateNaissance);
            formateurToEdit.setDateRecrutement(dateRecrutement);

            formateurToEdit.setEmail(emailField.getText());
            String newPassword = passwordField.getText();

            // Si l'utilisateur a saisi un nouveau password
            if (newPassword != null && !newPassword.trim().isEmpty()) {

                // Optionnel : validation longueur minimale
                if (newPassword.length() < 6) {
                    showAlert(
                            "Erreur",
                            "Mot de passe trop court",
                            "Le mot de passe doit contenir au moins 6 caractères.",
                            Alert.AlertType.ERROR
                    );
                    return;
                }

                String hashedPassword = hashPassword(newPassword);
                formateurToEdit.setPassword(hashedPassword);

            }


            formateurDAO.update(formateurToEdit);

            try {
                StageManager.loadScene("/view/directeur/Gestion_Formateurs/gestionFormateurs.fxml", "/styles/gestionFormateurs.css", "Formateurs");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private LocalDate getDateFromComboBoxes(
            ComboBox<String> day,
            ComboBox<String> month,
            ComboBox<String> year
    ) {
        try {
            if (day.getValue() == null || month.getValue() == null || year.getValue() == null) {
                return null;
            }

            int d = Integer.parseInt(day.getValue());

            // EXTRAIRE LE MOIS "01" depuis "01 - Jan"
            String monthValue = month.getValue().substring(0, 2);
            int m = Integer.parseInt(monthValue);

            int y = Integer.parseInt(year.getValue());

            return LocalDate.of(y, m, d);
        } catch (Exception e) {
            return null;
        }
    }




    private void populateDateComboBoxes() {
        // Remplir les jours (01 à 31)
        for (int i = 1; i <= 31; i++) {
            String day = String.format("%02d", i);
            dayComboBox.getItems().add(day);
            dayRComboBox.getItems().add(day);
        }

        // Remplir les mois avec format "MM - NomMois"
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] monthValues = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        for (int i = 0; i < months.length; i++) {
            String monthDisplay = monthValues[i] + " - " + months[i];
            monthComboBox.getItems().add(monthDisplay);
            monthRComboBox.getItems().add(monthDisplay);
        }

        // Remplir les années (de l'année actuelle à 1905)
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= 1905; i--) {
            String year = String.valueOf(i);
            yearComboBox.getItems().add(year);
            yearRComboBox.getItems().add(year);
        }

        // Sélectionner les valeurs par défaut
        // Date de naissance : par défaut 25 ans
        dayComboBox.getSelectionModel().select(0); // Jour 01
        monthComboBox.getSelectionModel().select(0); // Janvier
        yearComboBox.getSelectionModel().selectFirst(); // Année actuelle

        // Date recrutement : date d'aujourd'hui
        LocalDate today = LocalDate.now();
        String dayToday = String.format("%02d", today.getDayOfMonth());
        String monthToday = String.format("%02d", today.getMonthValue());
        String yearToday = String.valueOf(today.getYear());

        // Trouver et sélectionner le jour d'aujourd'hui
        int dayIndex = dayRComboBox.getItems().indexOf(dayToday);
        if (dayIndex >= 0) {
            dayRComboBox.getSelectionModel().select(dayIndex);
        }

        // Trouver et sélectionner le mois d'aujourd'hui
        String monthDisplayToday = monthToday + " - " + getMonthName(today.getMonthValue());
        int monthIndex = monthRComboBox.getItems().indexOf(monthDisplayToday);
        if (monthIndex >= 0) {
            monthRComboBox.getSelectionModel().select(monthIndex);
        }

        // Trouver et sélectionner l'année d'aujourd'hui
        int yearIndex = yearRComboBox.getItems().indexOf(yearToday);
        if (yearIndex >= 0) {
            yearRComboBox.getSelectionModel().select(yearIndex);
        }

    }

    private String getMonthName(int month) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1];
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hashage du mot de passe", e);
        }
    }





    private void setupButtonStyles() {
        // Style pour le bouton Annuler
        cancelButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1a202c;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;"
        );

        // Style initial pour le bouton Ajouter
        modifierButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #10B981;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
    }


    @FXML
    private void onHoverButton() {
        modifierButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #10B981;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 24;" +
                        "-fx-border-color: #10B981;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-translate-y: -1;" +
                        "-fx-scale-x: 1.1;" +
                        "-fx-scale-y: 1.1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
    }

    @FXML
    private void onExitButton() {
        modifierButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #10B981;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
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
