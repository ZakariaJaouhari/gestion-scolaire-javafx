package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.dao.FormateurDAO;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class FormateurDashboardController {

    @FXML private Label welcomeNameLabel;
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomFormateurLabel;
    @FXML private Label modulesCountLabel;
    @FXML private Label stagiairesCountLabel;
    @FXML private Label groupesCountLabel;
    @FXML private ImageView profileImageView;

    @FXML private TableView<Examen> examensTable;
    @FXML private VBox addExamForm;
    @FXML private Button addExamButton;
    @FXML private ComboBox<String> groupeComboBox;
    @FXML private ComboBox<String> moduleComboBox;
    @FXML private DatePicker examDatePicker;
    @FXML private ComboBox<String> heureComboBox;
    @FXML private TextField dureeTextField;

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;

    private YearMonth currentYearMonth;
    private FormateurDAO formateurDAO;
    private ObservableList<Examen> examensList;

    // Classe interne pour les examens
    public static class Examen {
        private String groupe;
        private String module;
        private String date;
        private String heure;

        public Examen(String groupe, String module, String date, String heure) {
            this.groupe = groupe;
            this.module = module;
            this.date = date;
            this.heure = heure;
        }

        public String getGroupe() { return groupe; }
        public String getModule() { return module; }
        public String getDate() { return date; }
        public String getHeure() { return heure; }
    }

    @FXML
    public void initialize() {
        System.out.println("FormateurDashboardController initialisé");

        // Vérifier la session
        SessionManager session = SessionManager.getInstance();
        if (!session.isFormateur()) {
            redirectToLogin();
            return;
        }

        // Récupérer le formateur connecté
        Formateur formateur = session.getCurrentFormateur();
        if (formateur == null) {
            redirectToLogin();
            return;
        }

        // Initialiser les données
        formateurDAO = new FormateurDAO();

        // Mettre à jour les labels
        updateUserInfo(formateur);

        // Initialiser les statistiques
        loadStats();

        // Initialiser la table des examens
        initializeExamensTable();

        // Initialiser le calendrier
        currentYearMonth = YearMonth.now();
        updateCalendar();

        // Initialiser les combobox
        initializeComboBoxes();

        // Charger les examens
        loadExamens();
    }

    private void updateUserInfo(Formateur formateur) {
        welcomeNameLabel.setText(formateur.getNomComplet());
        nomFormateurLabel.setText(formateur.getNomComplet());

        // Pour l'école, vous devrez récupérer via le directeur
        // nomEcoleLabel.setText(formateur.getDirecteur().getNomEcole());
        nomEcoleLabel.setText("École de Formation");

        // Charger la photo de profil si disponible
        if (formateur.getProfilePicture() != null && !formateur.getProfilePicture().isEmpty()) {
            try {
                profileImageView.setImage(new Image("file:" + formateur.getProfilePicture()));
            } catch (Exception e) {
                System.err.println("Erreur chargement photo: " + e.getMessage());
            }
        }
    }

    private void loadStats() {
        // Charger les statistiques depuis la base de données
        // modulesCountLabel.setText(String.valueOf(formateurDAO.countModules()));
        // stagiairesCountLabel.setText(formateurDAO.countStagiaires() + "/" + formateurDAO.totalStagiaires());
        // groupesCountLabel.setText(String.valueOf(formateurDAO.countGroupes()));

        // Données temporaires
        modulesCountLabel.setText("4");
        stagiairesCountLabel.setText("2/9");
        groupesCountLabel.setText("4");
    }

    private void initializeExamensTable() {
        examensList = FXCollections.observableArrayList();
        examensTable.setItems(examensList);

        // Ajouter des données d'exemple
        examensList.add(new Examen("Dev102", "Culture et techniques numérique", "2024-03-12", "13:30"));
        examensList.add(new Examen("GE101", "Bureautique", "2024-04-02", "08:30"));
    }

    private void initializeComboBoxes() {
        // Initialiser les groupes (données d'exemple)
        ObservableList<String> groupes = FXCollections.observableArrayList(
                "Dev102", "GE101", "Dev103", "GE102"
        );
        groupeComboBox.setItems(groupes);

        // Initialiser les heures dans le contrôleur
        ObservableList<String> heures = FXCollections.observableArrayList(
                "08:30", "09:30", "10:30", "11:30",
                "13:30", "14:30", "15:30", "16:30",
                "17:30", "18:30"
        );
        heureComboBox.setItems(heures);
        heureComboBox.setValue("08:30");

        // Configurer les événements
        groupeComboBox.setOnAction(e -> handleGroupeSelection());
    }

    @FXML
    private void handleGroupeSelection() {
        // Quand un groupe est sélectionné, charger ses modules
        String selectedGroupe = groupeComboBox.getValue();
        if (selectedGroupe != null) {
            ObservableList<String> modules = FXCollections.observableArrayList();

            // Logique pour charger les modules selon le groupe
            switch (selectedGroupe) {
                case "Dev102":
                    modules.addAll("Culture et techniques numérique", "Programmation Java", "Base de données");
                    break;
                case "GE101":
                    modules.addAll("Bureautique", "Communication", "Gestion de projet");
                    break;
                case "Dev103":
                    modules.addAll("Algorithmique", "Web développement", "Systèmes d'exploitation");
                    break;
                case "GE102":
                    modules.addAll("Comptabilité", "Marketing", "Management");
                    break;
            }

            moduleComboBox.setItems(modules);
            if (!modules.isEmpty()) {
                moduleComboBox.setValue(modules.get(0));
            }
        }
    }

    @FXML
    private void showAddExamForm() {
        addExamForm.setVisible(true);
        addExamButton.setVisible(false);
        examDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void addExam() {
        String groupe = groupeComboBox.getValue();
        String module = moduleComboBox.getValue();
        LocalDate date = examDatePicker.getValue();
        String heure = heureComboBox.getValue();
        String duree = dureeTextField.getText();

        // Validation
        if (groupe == null || module == null || date == null || heure == null) {
            showAlert("Erreur", "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        // Ajouter l'examen à la table
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        examensList.add(new Examen(groupe, module, dateStr, heure));

        // Réinitialiser le formulaire
        cancelAddExam();

        showAlert("Succès", "Examen ajouté", "L'examen a été ajouté avec succès.");
    }

    @FXML
    private void cancelAddExam() {
        addExamForm.setVisible(false);
        addExamButton.setVisible(true);
        groupeComboBox.setValue(null);
        moduleComboBox.setValue(null);
        examDatePicker.setValue(null);
        heureComboBox.setValue("08:30");
        dureeTextField.clear();
    }

    private void loadExamens() {
        // Charger les examens depuis la base de données
        // List<Examen> examens = formateurDAO.getExamens();
        // examensList.addAll(examens);
    }

    private void updateCalendar() {
        // Mettre à jour le label du mois/année
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(currentYearMonth.format(formatter));

        // Nettoyer le calendrier
        calendarGrid.getChildren().clear();

        // Obtenir le premier jour du mois
        LocalDate firstDay = currentYearMonth.atDay(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 0 pour dimanche

        // Remplir le calendrier
        int daysInMonth = currentYearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        int row = 0;
        int col = firstDayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setStyle("-fx-font-size: 16; -fx-alignment: center; -fx-padding: 15;");

            // Mettre en évidence le jour actuel
            if (currentYearMonth.equals(YearMonth.from(today)) && day == today.getDayOfMonth()) {
                dayLabel.setStyle(dayLabel.getStyle() +
                        "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
            } else {
                dayLabel.setStyle(dayLabel.getStyle() +
                        "-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1;");
            }

            // Effet hover
            dayLabel.setOnMouseEntered(e -> {
                if (!dayLabel.getStyle().contains("#059669")) {
                    dayLabel.setStyle(dayLabel.getStyle().replace("white", "#f3f4f6"));
                }
            });

            dayLabel.setOnMouseExited(e -> {
                if (!dayLabel.getStyle().contains("#059669")) {
                    dayLabel.setStyle(dayLabel.getStyle().replace("#f3f4f6", "white"));
                }
            });

            calendarGrid.add(dayLabel, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    // Méthodes de navigation
    @FXML
    private void handleHome() {
        // Reste sur la même page
        System.out.println("Home clicked");
    }

    @FXML
    private void handleProfile() {
        System.out.println("Profile clicked");
        // Rediriger vers la page profil
        // StageManager.loadScene("/view/formateur/profile.fxml", ...);
    }

    @FXML
    private void handleGroupes() {
        System.out.println("Groupes clicked");
        // Rediriger vers la page groupes
        // StageManager.loadScene("/view/formateur/groupes.fxml", ...);
    }

    @FXML
    private void handleModules() {
        System.out.println("Modules clicked");
        // Rediriger vers la page modules
        // StageManager.loadScene("/view/formateur/modules.fxml", ...);
    }

    @FXML
    private void handleNotes() {
        System.out.println("Notes clicked");
        // Rediriger vers la page notes
        // StageManager.loadScene("/view/formateur/notes.fxml", ...);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            StageManager.loadScene("/view/login.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redirectToLogin() {
        try {
            StageManager.loadScene("/view/login.fxml", "/styles/auth.css", "Connexion - Gestion Scolaire");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}