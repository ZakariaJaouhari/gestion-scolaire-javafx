package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.dao.*;
import org.example.model.*;
import org.example.model.Module;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AddEditSeanceDialogController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> startTimeComboBox;
    @FXML private ComboBox<LocalTime> endTimeComboBox;
    @FXML private ComboBox<Module> moduleComboBox;
    @FXML private ComboBox<Groupe> groupeComboBox;
    @FXML private ComboBox<Formateur> formateurComboBox;
    @FXML private TextField salleTextField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private SeanceDAO seanceDAO;
    private ModuleDAO moduleDAO;
    private GroupeDAO groupeDAO;
    private FormateurDAO formateurDAO;

    private PlanningSemaineController parentController;
    private Seance seanceToEdit;

    @FXML
    public void initialize() {
        seanceDAO = new SeanceDAO();
        moduleDAO = new ModuleDAO();
        groupeDAO = new GroupeDAO();
        formateurDAO = new FormateurDAO();

        setupTimeComboBoxes();
        loadComboBoxData();
        setupEventHandlers();
        setupButtonStyles();
    }

    public void setParentController(PlanningSemaineController parent) {
        this.parentController = parent;
    }

    public void setSeanceToEdit(Seance seance) {
        this.seanceToEdit = seance;
        populateForm();
    }

    private void setupTimeComboBoxes() {
        // Créer les heures de 8h à 18h
        for (int hour = 8; hour <= 18; hour++) {
            startTimeComboBox.getItems().add(LocalTime.of(hour, 0));
            endTimeComboBox.getItems().add(LocalTime.of(hour, 0));
        }

        // Configurer l'affichage des heures
        StringConverter<LocalTime> timeConverter = new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime time) {
                return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
            }

            @Override
            public LocalTime fromString(String string) {
                return string != null ? LocalTime.parse(string, DateTimeFormatter.ofPattern("HH:mm")) : null;
            }
        };

        startTimeComboBox.setConverter(timeConverter);
        endTimeComboBox.setConverter(timeConverter);
    }

    private void loadComboBoxData() {
        int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

        try {
            // Charger les modules
            List<Module> modules = moduleDAO.findByDirecteurId(directeurId);
            moduleComboBox.getItems().setAll(modules);

            // Charger les groupes
            List<Groupe> groupes = groupeDAO.findByDirecteurId(directeurId);
            groupeComboBox.getItems().setAll(groupes);

            // Charger les formateurs
            List<Formateur> formateurs = formateurDAO.findByDirecteurId(directeurId);
            formateurComboBox.getItems().setAll(formateurs);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement impossible",
                    "Impossible de charger les données: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupEventHandlers() {
        saveButton.setOnAction(e -> saveSeance());
        cancelButton.setOnAction(e -> closeDialog());

        // Validation: l'heure de fin doit être après l'heure de début
        startTimeComboBox.setOnAction(e -> validateTimes());
        endTimeComboBox.setOnAction(e -> validateTimes());
    }

    private void populateForm() {
        if (seanceToEdit != null) {
            datePicker.setValue(seanceToEdit.getDate());
            startTimeComboBox.setValue(seanceToEdit.getHeureDebut());
            endTimeComboBox.setValue(seanceToEdit.getHeureFin());
            salleTextField.setText(seanceToEdit.getSalle());

            // Sélectionner les valeurs dans les ComboBox
            for (Module module : moduleComboBox.getItems()) {
                if (module.getId() == seanceToEdit.getModuleId()) {
                    moduleComboBox.setValue(module);
                    break;
                }
            }

            for (Groupe groupe : groupeComboBox.getItems()) {
                if (groupe.getId() == seanceToEdit.getGroupeId()) {
                    groupeComboBox.setValue(groupe);
                    break;
                }
            }

            for (Formateur formateur : formateurComboBox.getItems()) {
                if (formateur.getId() == seanceToEdit.getFormateurId()) {
                    formateurComboBox.setValue(formateur);
                    break;
                }
            }

            saveButton.setText("Modifier");
        }
    }

    private void validateTimes() {
        LocalTime start = startTimeComboBox.getValue();
        LocalTime end = endTimeComboBox.getValue();

        if (start != null && end != null && !end.isAfter(start)) {
            showAlert("Validation", "Heures invalides",
                    "L'heure de fin doit être après l'heure de début.", Alert.AlertType.WARNING);
            endTimeComboBox.setValue(null);
        }
    }

    private void saveSeance() {
        try {
            // Validation
            if (!validateForm()) {
                return;
            }

            int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

            if (seanceToEdit == null) {
                // Créer une nouvelle séance
                Seance newSeance = new Seance(
                        moduleComboBox.getValue().getId(),
                        groupeComboBox.getValue().getId(),
                        formateurComboBox.getValue().getId(),
                        directeurId,
                        datePicker.getValue(),
                        startTimeComboBox.getValue(),
                        endTimeComboBox.getValue(),
                        salleTextField.getText()
                );

                Long newId = seanceDAO.create(newSeance);
                if (newId != null) {
                    showAlert("Succès", "Séance créée",
                            "La séance a été créée avec succès.", Alert.AlertType.INFORMATION);
                    closeDialog();
                } else {
                    showAlert("Erreur", "Création échouée",
                            "Impossible de créer la séance.", Alert.AlertType.ERROR);
                }
            } else {
                // Modifier la séance existante
                seanceToEdit.setModuleId(moduleComboBox.getValue().getId());
                seanceToEdit.setGroupeId(groupeComboBox.getValue().getId());
                seanceToEdit.setFormateurId(formateurComboBox.getValue().getId());
                seanceToEdit.setDate(datePicker.getValue());
                seanceToEdit.setHeureDebut(startTimeComboBox.getValue());
                seanceToEdit.setHeureFin(endTimeComboBox.getValue());
                seanceToEdit.setSalle(salleTextField.getText());

                boolean updated = seanceDAO.update(seanceToEdit);
                if (updated) {
                    showAlert("Succès", "Séance modifiée",
                            "La séance a été modifiée avec succès.", Alert.AlertType.INFORMATION);
                    closeDialog();
                } else {
                    showAlert("Erreur", "Modification échouée",
                            "Impossible de modifier la séance.", Alert.AlertType.ERROR);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Sauvegarde impossible",
                    "Erreur lors de la sauvegarde: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (datePicker.getValue() == null) {
            errors.append("- La date est obligatoire\n");
        }
        if (startTimeComboBox.getValue() == null) {
            errors.append("- L'heure de début est obligatoire\n");
        }
        if (endTimeComboBox.getValue() == null) {
            errors.append("- L'heure de fin est obligatoire\n");
        }
        if (moduleComboBox.getValue() == null) {
            errors.append("- Le module est obligatoire\n");
        }
        if (groupeComboBox.getValue() == null) {
            errors.append("- Le groupe est obligatoire\n");
        }
        if (formateurComboBox.getValue() == null) {
            errors.append("- Le formateur est obligatoire\n");
        }
        if (salleTextField.getText() == null || salleTextField.getText().trim().isEmpty()) {
            errors.append("- La salle est obligatoire\n");
        }

        // Vérifier les conflits d'horaire
        if (datePicker.getValue() != null && startTimeComboBox.getValue() != null &&
                endTimeComboBox.getValue() != null && groupeComboBox.getValue() != null &&
                formateurComboBox.getValue() != null) {

            // Vérifier conflit pour le groupe
            boolean hasConflict = seanceDAO.checkScheduleConflict(
                    seanceToEdit != null ? seanceToEdit.getId() : 0,
                    groupeComboBox.getValue().getId(),
                    formateurComboBox.getValue().getId(),
                    datePicker.getValue(),
                    startTimeComboBox.getValue(),
                    endTimeComboBox.getValue(),
                    salleTextField.getText()
            );

            if (hasConflict) {
                errors.append("- Conflit d'horaire détecté\n");
            }
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Champs invalides", errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void closeDialog() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Planning/PlanningSemaine.fxml", "/styles/gestionFormateurs.css", "Planning");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onHoverButton() {
        saveButton.setStyle(
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
        saveButton.setStyle(
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
        saveButton.setStyle(
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
    // ======================= NAVIGATION MENU =============================
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
            StageManager.loadScene("/view/directeur/Gestion_Formateurs/gestionFormateurs.fxml", "/styles/gestionFormateurs.css", "Formateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStagiaires() {
        try {
            StageManager.loadScene("/view/directeur/gestion_Etudiants/gestionEtudiants.fxml", "/styles/gestionFormateurs.css", "Etudiants");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGroupes() {
        try {
            StageManager.loadScene("/view/directeur/gestion_Groupes/gestionGroupes.fxml", "/styles/gestionGroupes.css", "Groupes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModules() {
        try {
            StageManager.loadScene("/view/directeur/gestion_Modules/gestionModules.fxml", "/styles/gestionFormateurs.css", "Modules");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotes() {
        try {
            StageManager.loadScene("/view/directeur/gestionNotes.fxml", "/styles/gestionNotes.css", "Notes");
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
        SessionManager.getInstance().clearSession();
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

        Stage stage = (Stage) salleTextField.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.showAndWait();
    }
}