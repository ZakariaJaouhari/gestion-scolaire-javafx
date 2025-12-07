package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.dao.DirecteurDAO;
import org.example.dao.FormateurDAO;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class FormateurDashboardController {

    @FXML private Label versionLabel;
    @FXML private VBox cardModules;
    @FXML private VBox cardGroupes;
    @FXML private VBox cardEtudiants;

    @FXML private Label welcomeNameLabel;
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomFormateurLabel;
    @FXML private Label modulesCountLabel;
    @FXML private Label stagiairesCountLabel;
    @FXML private Label groupesCountLabel;
    @FXML private ImageView profileImageView;

    //@FXML private TableView<Examen> examensTable;
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
   // private ObservableList<Examen> examensList;

    /* Classe interne pour les examens
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
    }*/

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
        //initializeExamensTable();

        // Initialiser le calendrier
        currentYearMonth = YearMonth.now();
        updateCalendar();

        // Initialiser les combobox
       // initializeComboBoxes();

        // Charger les examens
        // loadExamens();
    }

    private void updateUserInfo(Formateur formateur) {
        welcomeNameLabel.setText(formateur.getNomComplet());
        nomFormateurLabel.setText(formateur.getNomComplet());

        // Récupérer et afficher le nom de l'école via le directeur
        if (formateur.getDirecteurId() > 0) {
            DirecteurDAO DirecteurDAO = new DirecteurDAO();
            String nomEcole = DirecteurDAO.getNomEcoleByDirecteurId(formateur.getDirecteurId());
            nomEcoleLabel.setText(nomEcole);
        } else {
            nomEcoleLabel.setText("École non spécifiée");
        }


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

    /*private void initializeExamensTable() {
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
    }*/

    private void updateCalendar() {
        try {
            // Mettre à jour le label du mois/année
            String month = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
            int year = currentYearMonth.getYear();
            monthYearLabel.setText(month.substring(0, 1).toUpperCase() + month.substring(1) + " " + year);

            // Nettoyer le grid
            calendarGrid.getChildren().clear();

            // Ajouter les jours de la semaine
            String[] joursSemaine = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
            for (int i = 0; i < 7; i++) {
                Label dayLabel = new Label(joursSemaine[i]);
                dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                dayLabel.setTextFill(Color.web("#6b7280"));
                dayLabel.setStyle("-fx-padding: 10; -fx-alignment: center;");
                calendarGrid.add(dayLabel, i, 0);
            }

            // Premier jour du mois
            LocalDate firstDay = currentYearMonth.atDay(1);
            int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // Dimanche = 0

            // Nombre de jours dans le mois
            int daysInMonth = currentYearMonth.lengthOfMonth();

            // Remplir les jours
            int row = 1;
            int col = firstDayOfWeek;

            for (int day = 1; day <= daysInMonth; day++) {
                StackPane dayCell = createDayCell(day);
                calendarGrid.add(dayCell, col, row);

                col++;
                if (col > 6) {
                    col = 0;
                    row++;
                }
            }

            System.out.println("✅ Calendrier mis à jour: " + month + " " + year);

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour calendrier: " + e.getMessage());
        }
    }

    private StackPane createDayCell(int day) {
        StackPane cell = new StackPane();
        cell.setPrefSize(50, 50);

        // Vérifier si c'est aujourd'hui
        boolean isToday = currentYearMonth.equals(YearMonth.from(LocalDate.now())) &&
                day == LocalDate.now().getDayOfMonth();

        // Cercle de fond pour aujourd'hui
        if (isToday) {
            Circle circle = new Circle(20);
            circle.setFill(Color.web("#059669"));
            cell.getChildren().add(circle);
        }

        // Label du jour
        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        dayLabel.setTextFill(isToday ? Color.WHITE : Color.BLACK);

        cell.getChildren().add(dayLabel);

        // Effet hover
        cell.setOnMouseEntered(e -> {
            if (!isToday) {
                cell.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 25;");
            }
        });

        cell.setOnMouseExited(e -> {
            if (!isToday) {
                cell.setStyle("");
            }
        });

        return cell;
    }

    private void initializeStyles() {
        // Style version label
        versionLabel.setText("V 0.1.0");

        // Appliquer les ombres aux cartes
        String shadowStyle = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);";

        if (cardModules != null) {
            cardModules.setStyle(cardModules.getStyle() + shadowStyle);
        }
        if (cardEtudiants != null) {
            cardEtudiants.setStyle(cardEtudiants.getStyle() + shadowStyle);
        }
        if (cardGroupes != null) {
            cardGroupes.setStyle(cardGroupes.getStyle() + shadowStyle);
        }
    }

    // ========== NAVIGATION CALENDRIER ==========
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

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}