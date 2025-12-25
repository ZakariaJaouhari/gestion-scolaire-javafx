package org.example.controller.formateur;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.*;
import org.example.model.*;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class PlanningFormateurController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomFormateurLabel;
    @FXML private Label versionLabel;

    // Planning
    @FXML private GridPane planningGrid;
    @FXML private Label currentWeekLabel;
    @FXML private Button previousWeekButton;
    @FXML private Button nextWeekButton;
    @FXML private Button todayButton;

    private SeanceDAO seanceDAO;
    private FormateurDAO formateurDAO;
    private GroupeDAO groupeDAO;
    private ModuleDAO moduleDAO;

    private LocalDate currentWeekStart;
    private Formateur currentFormateur;

    // Couleurs pour les cellules
    private static final Color[] CELL_COLORS = {
            Color.web("#E3F2FD"), // Bleu clair
            Color.web("#F3E5F5"), // Violet clair
            Color.web("#E8F5E8"), // Vert clair
            Color.web("#FFF3E0"), // Orange clair
            Color.web("#FCE4EC"), // Rose clair
            Color.web("#F1F8E9"), // Vert pomme
            Color.web("#E0F7FA"), // Cyan clair
            Color.web("#FFF8E1"), // Jaune clair
    };

    @FXML
    public void initialize() {
        seanceDAO = new SeanceDAO();
        formateurDAO = new FormateurDAO();
        groupeDAO = new GroupeDAO();
        moduleDAO = new ModuleDAO();

        // Récupérer le formateur connecté
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            currentFormateur = session.getCurrentFormateur();
        }

        versionLabel.setText("V 0.1.0");
        updateUserInfo(currentFormateur);
        // Initialiser la semaine courante
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Configurer les contrôles
        setupControls();

        // Charger le planning
        loadPlanning();
    }

    private void updateUserInfo(Formateur formateur) {
        nomFormateurLabel.setText(formateur.getNomComplet());

        // Récupérer et afficher le nom de l'école via le directeur
        if (formateur.getDirecteurId() > 0) {
            DirecteurDAO DirecteurDAO = new DirecteurDAO();
            String nomEcole = DirecteurDAO.getNomEcoleByDirecteurId(formateur.getDirecteurId());
            nomEcoleLabel.setText(nomEcole);
        } else {
            nomEcoleLabel.setText("École non spécifiée");
        }
        if (formateur != null) {
            nomFormateurLabel.setText(formateur.getNom() + " " + formateur.getPrenom());
        }


    }

    private void setupControls() {
        // Boutons de navigation
        previousWeekButton.setOnAction(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeekDisplay();
            loadPlanning();
        });

        nextWeekButton.setOnAction(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeekDisplay();
            loadPlanning();
        });

        todayButton.setOnAction(e -> {
            currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            updateWeekDisplay();
            loadPlanning();
        });

        // Initialiser l'affichage de la semaine
        updateWeekDisplay();
    }

    private void updateWeekDisplay() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        currentWeekLabel.setText("Semaine du " + currentWeekStart.format(formatter) + " au " + weekEnd.format(formatter));
    }

    private void loadPlanning() {
        planningGrid.getChildren().clear();
        planningGrid.getColumnConstraints().clear();
        planningGrid.getRowConstraints().clear();

        // Définir les dimensions
        int numDays = 6; // Lundi à Samedi
        int numSlots = 24; // 8h à 19h avec des créneaux de 30 minutes

        // Configurer les colonnes
        for (int col = 0; col <= numDays; col++) {
            ColumnConstraints colConst = new ColumnConstraints();
            if (col == 0) {
                colConst.setPrefWidth(80); // Colonne des heures
            } else {
                colConst.setPrefWidth(200); // Colonnes des jours (plus large pour le formateur)
            }
            planningGrid.getColumnConstraints().add(colConst);
        }

        // Configurer les lignes
        for (int row = 0; row <= numSlots; row++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(30);
            planningGrid.getRowConstraints().add(rowConst);
        }

        // En-tête des jours
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        for (int col = 1; col <= 6; col++) {
            Label dayLabel = new Label(jours[col-1] + "\n" + currentWeekStart.plusDays(col-1).format(DateTimeFormatter.ofPattern("dd/MM")));
            dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-alignment: center;");
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            GridPane.setVgrow(dayLabel, Priority.ALWAYS);
            dayLabel.setPadding(new javafx.geometry.Insets(5));
            planningGrid.add(dayLabel, col, 0);
        }

        // En-tête des heures
        for (int hour = 8; hour <= 19; hour++) {
            int rowStart = (hour - 8) * 2 + 1;

            Label hourLabel = new Label(String.format("%02d:00", hour));
            hourLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center-right;");
            hourLabel.setPadding(new javafx.geometry.Insets(5));
            planningGrid.add(hourLabel, 0, rowStart);

            if (hour < 19) {
                Label halfHourLabel = new Label(String.format("%02d:30", hour));
                halfHourLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 10px; -fx-alignment: center-right; -fx-text-fill: #666;");
                halfHourLabel.setPadding(new javafx.geometry.Insets(5));
                planningGrid.add(halfHourLabel, 0, rowStart + 1);
            }
        }

        // Charger et afficher les séances du formateur
        loadAndDisplaySeances();
    }

    private void loadAndDisplaySeances() {
        if (currentFormateur == null) return;

        List<Seance> seances = new ArrayList<>();

        try {
            // Récupérer les séances du formateur pour la semaine
            seances = seanceDAO.getSeancesByFormateurAndWeek(currentFormateur.getId(), currentWeekStart);

            // Charger les relations
            for (Seance seance : seances) {
                if (seance.getModuleId() > 0) {
                    seance.setModule(moduleDAO.findById(seance.getModuleId()));
                }
                if (seance.getGroupeId() > 0) {
                    seance.setGroupe(groupeDAO.findById(seance.getGroupeId()));
                }
                if (seance.getFormateurId() > 0) {
                    seance.setFormateur(formateurDAO.findById(seance.getFormateurId()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement des séances",
                    "Impossible de charger votre emploi du temps: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        // Trier par heure de début
        seances.sort(Comparator.comparing(Seance::getHeureDebut));

        // Afficher les séances
        for (Seance seance : seances) {
            addSeanceToGrid(seance);
        }
    }

    private void addSeanceToGrid(Seance seance) {
        LocalDate date = seance.getDate();

        // Vérifier si la séance est dans la semaine affichée
        if (date.isBefore(currentWeekStart) || date.isAfter(currentWeekStart.plusDays(5))) {
            return;
        }

        // Calculer la colonne (jour)
        int dayOfWeek = date.getDayOfWeek().getValue();
        if (dayOfWeek == 7) return; // Ignorer dimanche

        // Calculer les créneaux
        LocalTime startTime = seance.getHeureDebut();
        LocalTime endTime = seance.getHeureFin();

        int startSlot = calculateTimeSlot(startTime);
        int endSlot = calculateTimeSlot(endTime);

        // Ajustements
        if (startSlot < 0) startSlot = 0;
        if (endSlot > 24) endSlot = 24;

        int durationSlots = endSlot - startSlot;
        if (durationSlots < 1) durationSlots = 1;

        int startRow = startSlot + 1;

        // Créer la cellule
        BorderPane seanceCell = new BorderPane();
        seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()) +
                "; -fx-border-color: #b0bec5; -fx-border-width: 1px; -fx-border-radius: 3px;");
        seanceCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Contenu
        VBox content = new VBox(3);
        content.setPadding(new javafx.geometry.Insets(8));
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Informations importantes pour le formateur
        Label moduleLabel = new Label(seance.getModule() != null ? seance.getModule().getNom() : "Module");
        moduleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");
        moduleLabel.setWrapText(true);

        Label groupeLabel = new Label();
        if (seance.getGroupe() != null) {
            groupeLabel.setText("Groupe: " + seance.getGroupe().getMatricule() +
                    " - " + seance.getGroupe().getNiveau().getValeur());
        }
        groupeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #34495e;");
        groupeLabel.setWrapText(true);

        // Horaires
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("H'h'mm");
        Label timeLabel = new Label(startTime.format(displayFormatter) + " - " + endTime.format(displayFormatter));
        timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        content.getChildren().addAll(moduleLabel, groupeLabel, timeLabel);
        seanceCell.setCenter(content);

        // Ajouter au grid
        planningGrid.add(seanceCell, dayOfWeek, startRow, 1, durationSlots);

        // Effet au survol
        seanceCell.setOnMouseEntered(e -> {
            seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()).replace("0.8", "1.0") +
                    "; -fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 3px;");
        });

        seanceCell.setOnMouseExited(e -> {
            seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()) +
                    "; -fx-border-color: #b0bec5; -fx-border-width: 1px; -fx-border-radius: 3px;");
        });

        // Info-bulle avec plus de détails
        Tooltip tooltip = new Tooltip();
        tooltip.setText("Module: " + (seance.getModule() != null ? seance.getModule().getNom() : "N/A") + "\n" +
                "Groupe: " + (seance.getGroupe() != null ? seance.getGroupe().getMatricule() : "N/A") + "\n" +
                "Niveau: " + (seance.getGroupe() != null ? seance.getGroupe().getNiveau().getValeur() : "N/A") + "\n" +
                "Salle: " + (seance.getSalle() != null ? seance.getSalle() : "Non spécifiée"));
        Tooltip.install(seanceCell, tooltip);
    }

    private int calculateTimeSlot(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        int slot = (hour - 8) * 2;
        if (minute >= 30) {
            slot += 1;
        }
        return slot;
    }

    private String getColorForModule(int moduleId) {
        if (moduleId <= 0) {
            return "rgba(220, 220, 220, 0.8)";
        }

        int colorIndex = Math.abs(moduleId) % CELL_COLORS.length;
        Color color = CELL_COLORS[colorIndex];
        return String.format("rgba(%d, %d, %d, 0.8)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
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