package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.dao.EtudiantDAO;
import org.example.dao.FormateurDAO;
import org.example.dao.GroupeDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Etudiant;
import org.example.model.Formateur;
import org.example.model.Groupe;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public class Modifier_Module_Controller {

    // HEADER
    @FXML
    private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    @FXML private Button modifierButton;
    @FXML private Button cancelButton;

    @FXML private TextField nomField;
    @FXML private TextField matriculeField;
    @FXML private TextField heuresField;
    @FXML private TextField cofField;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private ComboBox<String> dayComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private ComboBox<String> dayfComboBox;
    @FXML private ComboBox<String> monthfComboBox;
    @FXML private ComboBox<String> yearfComboBox;
    @FXML private ComboBox<Formateur> formateurComboBox;

    private FormateurDAO formateurDAO;

    private org.example.model.Module moduleToEdit;
    private ModuleDAO ModuleDAO = new ModuleDAO();


    @FXML
    public void initialize() {
        ModuleDAO = new ModuleDAO();
        formateurDAO = new FormateurDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            nomDirecteurLabel.setText(session.getNomDirecteur());
        }
        versionLabel.setText("V 0.1.0");

        populateDateComboBoxes();
        loadFormateurs();

    }

    public void setModuleToEdit(org.example.model.Module module) {
        this.moduleToEdit = module;

        nomField.setText(module.getNom());
        matriculeField.setText(module.getMatricule());
        heuresField.setText(module.getHeuresPratique());
        cofField.setText(String.valueOf(module.getCoefficient()));

        if (module.getDateDebut() != null) {
            dayComboBox.setValue(String.valueOf(module.getDateDebut().getDayOfMonth()));
            monthComboBox.setValue(String.valueOf(module.getDateDebut().getMonthValue()));
            yearComboBox.setValue(String.valueOf(module.getDateDebut().getYear()));
        }
        if (module.getDateFin() != null) {
            dayfComboBox.setValue(String.valueOf(module.getDateFin().getDayOfMonth()));
            monthfComboBox.setValue(String.valueOf(module.getDateFin().getMonthValue()));
            yearfComboBox.setValue(String.valueOf(module.getDateFin().getYear()));
        }
        // Sélectionner le formateur actuel
        for (Formateur f : formateurComboBox.getItems()) {
            if (f.getId() == module.getFormateurId()) {
                formateurComboBox.setValue(f);
                break;
            }
        }

    }

    @FXML
    private void saveModule() {
        if (moduleToEdit != null) {
            moduleToEdit.setNom(nomField.getText());
            moduleToEdit.setMatricule(matriculeField.getText());
            moduleToEdit.setHeuresPratique(heuresField.getText());
            moduleToEdit.setCoefficient(Integer.parseInt(cofField.getText()));

            // Convertir ComboBox en LocalDate
            LocalDate dateDebut = getDateFromComboBoxes(
                    dayComboBox, monthComboBox, yearComboBox
            );
            LocalDate dateFin = getDateFromComboBoxes(
                    dayfComboBox, monthfComboBox, yearfComboBox
            );
            // Si l'utilisateur n'a pas modifié → garder l'ancienne date
            if (dateDebut == null) {
                dateDebut = moduleToEdit.getDateDebut();
            }
            if (dateFin == null) {
                dateFin = moduleToEdit.getDateFin();
            }
            moduleToEdit.setDateDebut(dateDebut);
            moduleToEdit.setDateFin(dateFin);

            Formateur selectedFormateur = formateurComboBox.getValue();

            if (selectedFormateur == null) {
                showAlert(
                        "Erreur",
                        "Formateur manquant",
                        "Veuillez sélectionner un formateur.",
                        Alert.AlertType.ERROR
                );
                return;
            }

            // ✅ Mise à jour du groupe
            moduleToEdit.setFormateurId(selectedFormateur.getId());


            ModuleDAO.update(moduleToEdit);

            try {
                StageManager.loadScene("/view/directeur/Gestion_Modules/gestionModules.fxml", "/styles/gestionFormateurs.css", "Modules");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void loadFormateurs() {
        int directeurId = SessionManager.getInstance().getUserId();

        formateurComboBox.getItems().setAll(
                formateurDAO.findByDirecteurId(directeurId)
        );

        // Affichage lisible
        formateurComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Formateur formateur) {
                if (formateur == null) return "";
                return formateur.getNomComplet();
            }

            @Override
            public Formateur fromString(String string) {
                return null;
            }
        });
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
        dayfComboBox.getSelectionModel().select(0); // Jour 01
        monthfComboBox.getSelectionModel().select(0); // Janvier
        yearfComboBox.getSelectionModel().selectFirst(); // Année actuelle


    }

    private String getMonthName(int month) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1];
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
    private void handleEtudiants() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Etudiants/gestionEtudiants.fxml", "/styles/gestionFormateurs.css", "Etudiants");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGroupes() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Groupes/gestionGroupes.fxml", "/styles/gestionGroupes.css", "Groupes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModules() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Modules/gestionModules.fxml", "/styles/gestionFormateurs.css", "Modules");
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
            StageManager.loadScene("/view/directeur/gestionCertificats.fxml", "/styles/gestionCertificats.css", "Certificats");
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
