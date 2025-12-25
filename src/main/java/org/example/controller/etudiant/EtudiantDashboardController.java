package org.example.controller.etudiant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.DirecteurDAO;
import org.example.dao.EtudiantDAO;
import org.example.dao.GroupeDAO;
import org.example.model.Etudiant;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class EtudiantDashboardController {

    @FXML
    private Label versionLabel;
    @FXML
    private Label welcomeNameLabel;
    @FXML
    private Label nomEcoleLabel;
    @FXML
    private Label nomEtudiantLabel;
    @FXML
    private Label moyenneLabel;
    @FXML
    private Label examensCountLabel;
    @FXML
    private Label modulesCountLabel;
    @FXML
    private ImageView profileImageView;

    @FXML
    private TableView<Note> notesTable;
    @FXML
    private TableColumn<Note, String> moduleColumn;
    @FXML
    private TableColumn<Note, Double> noteColumn;
    @FXML
    private TableColumn<Note, String> dateNoteColumn;
    @FXML
    private TableColumn<Note, Integer> coefficientColumn;

    //@FXML private ListView<String> examensListView;
    @FXML
    private Label aucunExamenLabel;

    @FXML
    private Label monthYearLabel;
    @FXML
    private GridPane calendarGrid;

    private LocalDate currentDate;
    private YearMonth currentYearMonth;

    private EtudiantDAO etudiantDAO;
    private GroupeDAO groupeDAO;
    private DirecteurDAO directeurDAO;
    private ObservableList<Note> notesList;


    // Classe interne pour les notes
    public static class Note {
        private String module;
        private double note;
        private String date;
        private int coefficient;

        public Note(String module, double note, String date, int coefficient) {
            this.module = module;
            this.note = note;
            this.date = date;
            this.coefficient = coefficient;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public double getNote() {
            return note;
        }

        public void setNote(double note) {
            this.note = note;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getCoefficient() {
            return coefficient;
        }

        public void setCoefficient(int coefficient) {
            this.coefficient = coefficient;
        }
    }

    @FXML
    public void initialize() {
        System.out.println("EtudiantDashboardController initialisé");

        currentDate = LocalDate.now();
        currentYearMonth = YearMonth.from(currentDate);

        // Vérifier la session
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Vérifier que c'est un étudiant (vous devrez ajouter cette vérification dans SessionManager)
        // Pour l'instant, on vérifie juste qu'il y a un utilisateur
        Object currentUser = session.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Initialiser les DAO
        etudiantDAO = new EtudiantDAO();
        groupeDAO = new GroupeDAO();
        directeurDAO = new DirecteurDAO();

        // Mettre à jour les labels
        updateUserInfo(currentUser);

        // Initialiser les statistiques
        loadStats();

        // Initialiser la table des notes
        initializeNotesTable();

        // Initialiser le calendrier
        updateCalendar();

        // Initialiser la liste des examens
        //initializeExamensList();

        // Charger les données
        loadNotes();
        //loadExamens();
    }

    private void updateUserInfo(Object currentUser) {
        // Vérifier le type d'utilisateur
        if (currentUser instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) currentUser;

            // Mettre à jour les labels
            welcomeNameLabel.setText(etudiant.getPrenom());
            nomEtudiantLabel.setText(etudiant.getNomComplet());

            // Récupérer le nom de l'école via le directeur
            if (etudiant.getDirecteurId() > 0) {
                DirecteurDAO DirecteurDAO = new DirecteurDAO();
                String nomEcole = DirecteurDAO.getNomEcoleByDirecteurId(etudiant.getDirecteurId());
                nomEcoleLabel.setText(nomEcole);
            } else {
                nomEcoleLabel.setText("École non spécifiée");
            }

        } else {
            // Si ce n'est pas un étudiant, rediriger
            System.err.println("❌ L'utilisateur n'est pas un étudiant");
            redirectToLogin();
        }
    }

    private void loadStats() {
        // Données temporaires - À remplacer par des appels DAO réels
        moyenneLabel.setText("15.75");
        examensCountLabel.setText("3");
        modulesCountLabel.setText("6");
    }

    private void initializeNotesTable() {
        notesList = FXCollections.observableArrayList();
        notesTable.setItems(notesList);

        // Configurer les colonnes
        moduleColumn.setCellValueFactory(new PropertyValueFactory<>("module"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        dateNoteColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));

        // Formatter la colonne note
        noteColumn.setCellFactory(column -> new TableCell<Note, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f/20", item));

                    // Colorier selon la note
                    if (item >= 16) {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                    } else if (item >= 10) {
                        setStyle("-fx-text-fill: #3b82f6;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444;");
                    }
                }
            }
        });
    }

    /*private void initializeExamensList() {
        examensListView.setItems(FXCollections.observableArrayList());
    }*/

    private void loadNotes() {
        // Données d'exemple - À remplacer par des appels DAO réels
        notesList.add(new Note("Programmation Java", 17.5, "2024-03-10", 3));
        notesList.add(new Note("Base de données", 14.0, "2024-03-12", 2));
        notesList.add(new Note("Algorithmique", 12.5, "2024-03-15", 2));
        notesList.add(new Note("Systèmes d'exploitation", 16.0, "2024-03-18", 1));

        // Calculer la moyenne
        double sommeNotes = 0;
        int sommeCoefficients = 0;

        for (Note note : notesList) {
            sommeNotes += note.getNote() * note.getCoefficient();
            sommeCoefficients += note.getCoefficient();
        }

        if (sommeCoefficients > 0) {
            double moyenne = sommeNotes / sommeCoefficients;
            moyenneLabel.setText(String.format("%.2f", moyenne));
        }
    }

    /*private void loadExamens() {
        // Données d'exemple - À remplacer par des appels DAO réels
        ObservableList<String> examens = FXCollections.observableArrayList(
                "📝 Culture et techniques numérique - 2024-03-25 14:30",
                "📝 Bureautique - 2024-04-02 08:30",
                "📝 Programmation Web - 2024-04-10 10:00"
        );

        examensListView.setItems(examens);
        examensCountLabel.setText(String.valueOf(examens.size()));

        // Afficher/masquer le label "aucun examen"
        aucunExamenLabel.setVisible(examens.isEmpty());
    }*/

    // ========== CALENDRIER ==========
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