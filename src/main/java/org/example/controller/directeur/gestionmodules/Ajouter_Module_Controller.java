package org.example.controller.directeur.gestionmodules;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.FormateurDAO;
import org.example.dao.ModuleDAO;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;

public class Ajouter_Module_Controller {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // Champs du formulaire
    @FXML private TextField nomField;
    @FXML private TextField matriculeField;
    @FXML private TextField heuresField;
    @FXML private TextField cofField;

    // ComboBox pour date
    @FXML private ComboBox<String> dayComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private ComboBox<String> dayfComboBox;
    @FXML private ComboBox<String> monthfComboBox;
    @FXML private ComboBox<String> yearfComboBox;


    @FXML private Button ajouterButton;
    @FXML private Button cancelButton;

    @FXML private ComboBox<Formateur> formateurComboBox;
    private FormateurDAO formateurDAO;

    private ModuleDAO moduleDAO;



    @FXML
    public void initialize() {
        moduleDAO = new ModuleDAO();
        formateurDAO = new FormateurDAO();


        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            nomDirecteurLabel.setText(session.getNomDirecteur());
        }
        versionLabel.setText("V 0.1.0");


        // Remplir les ComboBox pour les dates
        populateDateComboBoxes();

        loadFormateurs();
        // Configurer les événements
        configureEvents();

        // Configurer les styles des boutons
        setupButtonStyles();
    }

    private void loadFormateurs() {
        int directeurId = SessionManager.getInstance()
                .getCurrentDirecteur()
                .getId();

        formateurComboBox.getItems().setAll(
                formateurDAO.findByDirecteurId(directeurId)
        );

        // Afficher uniquement le matricule
        formateurComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Formateur formateur, boolean empty) {
                super.updateItem(formateur, empty);
                setText(empty || formateur == null ? "" : formateur.getNomComplet());
            }
        });

        // Affichage de l’élément sélectionné
        formateurComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Formateur formateur, boolean empty) {
                super.updateItem(formateur, empty);
                setText(empty || formateur == null ? "" : formateur.getNomComplet());
            }
        });
    }



    private void populateDateComboBoxes() {
        // Remplir les jours (01 à 31)
        for (int i = 1; i <= 31; i++) {
            String day = String.format("%02d", i);
            dayComboBox.getItems().add(day);
            dayfComboBox.getItems().add(day);
        }

        // Remplir les mois avec format "MM - NomMois"
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] monthValues = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        for (int i = 0; i < months.length; i++) {
            String monthDisplay = monthValues[i] + " - " + months[i];
            monthComboBox.getItems().add(monthDisplay);
            monthfComboBox.getItems().add(monthDisplay);
        }

        // Remplir les années (de l'année actuelle à 1905)
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= 1905; i--) {
            String year = String.valueOf(i);
            yearComboBox.getItems().add(year);
            yearfComboBox.getItems().add(year);
        }

        // Sélectionner les valeurs par défaut
        dayComboBox.getSelectionModel().select(0); // Jour 01
        monthComboBox.getSelectionModel().select(0); // Janvier
        yearComboBox.getSelectionModel().selectFirst(); // Année actuelle

        // Date recrutement : date d'aujourd'hui
        LocalDate today = LocalDate.now();
        String dayToday = String.format("%02d", today.getDayOfMonth());
        String monthToday = String.format("%02d", today.getMonthValue());
        String yearToday = String.valueOf(today.getYear());

        // Trouver et sélectionner le jour d'aujourd'hui
        int dayIndex = dayfComboBox.getItems().indexOf(dayToday);
        if (dayIndex >= 0) {
            dayfComboBox.getSelectionModel().select(dayIndex);
        }

        // Trouver et sélectionner le mois d'aujourd'hui
        String monthDisplayToday = monthToday + " - " + getMonthName(today.getMonthValue());
        int monthIndex = monthfComboBox.getItems().indexOf(monthDisplayToday);
        if (monthIndex >= 0) {
            monthfComboBox.getSelectionModel().select(monthIndex);
        }

        // Trouver et sélectionner l'année d'aujourd'hui
        int yearIndex = yearfComboBox.getItems().indexOf(yearToday);
        if (yearIndex >= 0) {
            yearfComboBox.getSelectionModel().select(yearIndex);
        }
    }

    private String getMonthName(int month) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1];
    }

    private void configureEvents() {
        // Action des boutons
        ajouterButton.setOnAction(e -> ajouterModule());
        cancelButton.setOnAction(e -> handleModules());

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
        ajouterButton.setStyle(
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

    private void ajouterModule() {
        try {
            // Validation des champs
            if (!validateFields()) {
                return;
            }

            // Récupération des valeurs
            String nom = nomField.getText().trim();
            String matricule = matriculeField.getText().trim();
            String heures_P = heuresField.getText().trim();
            Integer coefficient = Integer.valueOf(cofField.getText());

            // Conversion des dates
            LocalDate date_D = getDateFromComboBoxes(
                    dayComboBox.getValue(),
                    monthComboBox.getValue(),
                    yearComboBox.getValue()
            );
            LocalDate date_F = getDateFromComboBoxes(
                    dayfComboBox.getValue(),
                    monthfComboBox.getValue(),
                    yearfComboBox.getValue()
            );


            Formateur formateur = formateurComboBox.getValue();
            Formateur formateurSelectionne = formateurComboBox.getValue();

            if (formateurSelectionne == null) {
                showAlert("Validation", "Formateur manquant",
                        "Veuillez sélectionner un formateur.",
                        Alert.AlertType.WARNING);
                return;
            }

            int formateurId = formateurSelectionne.getId();



            // Vérifications supplémentaires
            if (date_D == null ) {
                showAlert("Erreur", "Date invalide",
                        "Veuillez vérifier les dates saisies.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Récupérer l'id du directeur connecté
            int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

            // Créer l'objet Formateur
            org.example.model.Module module = new org.example.model.Module(
                    nom,
                    matricule,
                    date_D,
                    date_F,
                    heures_P,
                    coefficient,
                    formateurId,
                    directeurId
            );


            // Sauvegarder dans la base de données
            Long id = moduleDAO.create(module);

            if (id != null) {

                showAlert(
                        "Succès",
                        "Module ajouté",
                        "Le module a été ajouté avec succès !",
                        Alert.AlertType.INFORMATION
                );

                // 👉 navigation APRES l'alert
                handleModules();
            } else {
                showAlert("Erreur", "Échec de l'ajout",
                        "Une erreur est survenue lors de l'ajout de module.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Exception",
                    "Erreur inattendue: " + ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }
        if (matriculeField.getText().trim().isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
        }
        if (heuresField.getText().trim().isEmpty()) {
            errors.append("- Le heures est obligatoire\n");
        }
        if (cofField.getText().trim().isEmpty()) {
            errors.append("- Coefficient est obligatoire\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Champs manquants",
                    errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private LocalDate getDateFromComboBoxes(String day, String monthStr, String year) {
        try {
            if (day == null || monthStr == null || year == null) {
                return null;
            }

            // Extraire la valeur numérique du mois (ex: "01 - Jan" -> "01")
            String monthValue = monthStr.split(" - ")[0];

            int dayInt = Integer.parseInt(day);
            int monthInt = Integer.parseInt(monthValue);
            int yearInt = Integer.parseInt(year);

            // Valider la date
            if (dayInt < 1 || dayInt > 31 || monthInt < 1 || monthInt > 12 || yearInt < 1905) {
                return null;
            }

            return LocalDate.of(yearInt, monthInt, dayInt);

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion de la date: " + e.getMessage());
            return null;
        }
    }

    private void clearForm() {
        // Réinitialiser les champs texte
        nomField.clear();
        matriculeField.clear();
        heuresField.clear();
        cofField.clear();

        // Réinitialiser les ComboBox
        dayComboBox.getSelectionModel().select(0);
        monthComboBox.getSelectionModel().select(0);
        yearComboBox.getSelectionModel().selectFirst();
        dayfComboBox.getSelectionModel().select(0);
        monthfComboBox.getSelectionModel().select(0);
        yearfComboBox.getSelectionModel().selectFirst();


        // Réinitialiser les autres ComboBox
        formateurComboBox.getSelectionModel().clearSelection();

        // Remettre le focus sur le premier champ
        nomField.requestFocus();
    }



    @FXML
    private void onHoverButton() {
        ajouterButton.setStyle(
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
        ajouterButton.setStyle(
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
        showAlert("Notes", "Gestion des notes",
                "Cette fonctionnalité sera disponible prochainement.",
                Alert.AlertType.INFORMATION);
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

        Stage stage = (Stage) nomField.getScene().getWindow(); // n'importe quel champ
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.showAndWait();
    }
}