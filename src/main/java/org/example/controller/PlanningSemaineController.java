package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
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
import java.util.stream.Collectors;

public class PlanningSemaineController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;


    @FXML private GridPane planningGrid;
    @FXML private ComboBox<String> viewTypeComboBox;
    @FXML private ComboBox<String> entityComboBox;
    @FXML private Label currentWeekLabel;
    @FXML private Button previousWeekButton;
    @FXML private Button nextWeekButton;
    @FXML private Button todayButton;
    @FXML private Button addSeanceButton;

    private SeanceDAO seanceDAO;
    private FormateurDAO formateurDAO;
    private GroupeDAO groupeDAO;
    private ModuleDAO moduleDAO;

    private LocalDate currentWeekStart;
    private String currentViewType = "Groupe"; // Par défaut
    private Integer currentEntityId = null;

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

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            nomDirecteurLabel.setText(session.getNomDirecteur());
        }
        versionLabel.setText("V 0.1.0");
        // Initialiser la semaine courante
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Configurer les contrôles
        setupControls();

        // Charger les données initiales
        loadFormateursAndGroupes();
        loadPlanning();
    }

    private void setupControls() {
        // Type de vue
        viewTypeComboBox.getItems().addAll("Groupe", "Formateur");
        viewTypeComboBox.setValue("Groupe");
        viewTypeComboBox.setOnAction(e -> {
            currentViewType = viewTypeComboBox.getValue();
            loadFormateursAndGroupes();
            loadPlanning();
        });

        // Sélection d'entité (groupe/formateur)
        entityComboBox.setPromptText("Sélectionner...");
        entityComboBox.setOnAction(e -> {
            String selected = entityComboBox.getValue();
            if (selected != null) {
                currentEntityId = extractIdFromString(selected);
            } else {
                currentEntityId = null;
            }
            loadPlanning();
        });

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

        // Bouton ajouter séance
        addSeanceButton.setOnAction(e -> openAddSeanceDialog());

        // Initialiser l'affichage de la semaine
        updateWeekDisplay();
    }

    private void loadFormateursAndGroupes() {
        entityComboBox.getItems().clear();

        int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

        try {
            if ("Formateur".equals(currentViewType)) {
                // Charger les formateurs
                List<Formateur> formateurs = formateurDAO.findByDirecteurId(directeurId);
                for (Formateur f : formateurs) {
                    entityComboBox.getItems().add(f.getNom() + " " + f.getPrenom() + " (ID: " + f.getId() + ")");
                }
            } else if ("Groupe".equals(currentViewType)) {
                // Charger les groupes
                List<Groupe> groupes = groupeDAO.findByDirecteurId(directeurId);
                for (Groupe g : groupes) {
                    entityComboBox.getItems().add(g.getMatricule() + " - " + g.getNiveau().getValeur() + " (ID: " + g.getId() + ")");
                }
            }

            if (!entityComboBox.getItems().isEmpty()) {
                entityComboBox.setValue(entityComboBox.getItems().get(0));
                currentEntityId = extractIdFromString(entityComboBox.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement impossible",
                    "Impossible de charger les données: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Integer extractIdFromString(String text) {
        try {
            if (text.contains("(ID: ")) {
                String idStr = text.substring(text.indexOf("(ID: ") + 5, text.indexOf(")"));
                return Integer.parseInt(idStr);
            } else if ("Salle".equals(currentViewType)) {
                // Pour les salles, on retourne l'index dans la liste
                return entityComboBox.getItems().indexOf(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        int numHours = 12; // 8h à 19h

        // Configurer les colonnes (jours)
        for (int col = 0; col <= numDays; col++) {
            ColumnConstraints colConst = new ColumnConstraints();
            if (col == 0) {
                colConst.setPrefWidth(80); // Colonne des heures
            } else {
                colConst.setPrefWidth(150); // Colonnes des jours
            }
            planningGrid.getColumnConstraints().add(colConst);
        }

        // Configurer les lignes (heures)
        for (int row = 0; row <= numHours; row++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(60);
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
            int row = hour - 7;
            Label hourLabel = new Label(String.format("%02d:00", hour));
            hourLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center-right;");
            hourLabel.setPadding(new javafx.geometry.Insets(5));
            planningGrid.add(hourLabel, 0, row);

            // Ligne de séparation
            Region separator = new Region();
            separator.setStyle("-fx-background-color: #e0e0e0;");
            GridPane.setRowSpan(separator, 1);
            planningGrid.add(separator, 0, row, 7, 1);
        }

        // Charger et afficher les séances
        loadAndDisplaySeances();
    }

    private void loadAndDisplaySeances() {
        if (currentEntityId == null) return;

        List<Seance> seances = new ArrayList<>();
        int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

        try {
            if ("Formateur".equals(currentViewType)) {
                seances = seanceDAO.getSeancesByFormateurAndWeek(currentEntityId, currentWeekStart);
            } else if ("Groupe".equals(currentViewType)) {
                seances = seanceDAO.getSeancesByGroupeAndWeek(currentEntityId, currentWeekStart);
            }

            // Charger les relations pour chaque séance
            for (Seance seance : seances) {
                if (seance.getModuleId() > 0) {
                    seance.setModule(moduleDAO.findById(seance.getModuleId()));
                }
                if (seance.getFormateurId() > 0) {
                    seance.setFormateur(formateurDAO.findById(seance.getFormateurId()));
                }
                if (seance.getGroupeId() > 0) {
                    seance.setGroupe(groupeDAO.findById(seance.getGroupeId()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement des séances",
                    "Impossible de charger les séances: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

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
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = lundi, 7 = dimanche
        if (dayOfWeek == 7) return; // On ignore le dimanche

        // Calculer les lignes (heures)
        LocalTime startTime = seance.getHeureDebut();
        LocalTime endTime = seance.getHeureFin();

        int startRow = startTime.getHour() - 7; // 8h -> row 1
        int endRow = endTime.getHour() - 7; // 10h -> row 3

        // Calculer la durée en heures (pour rowSpan)
        int durationHours = (int) java.time.Duration.between(startTime, endTime).toHours();

        // Créer la cellule de séance avec BorderPane pour organiser le contenu
        BorderPane seanceCell = new BorderPane();
        seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()) +
                "; -fx-border-color: #b0bec5; -fx-border-width: 1px; -fx-border-radius: 3px;");
        seanceCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Contenu principal au centre
        VBox content = new VBox(3);
        content.setPadding(new javafx.geometry.Insets(5));
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label moduleLabel = new Label(seance.getModule() != null ? seance.getModule().getNom() : "Module");
        moduleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #2c3e50;");
        moduleLabel.setWrapText(true);

        Label infoLabel = new Label();
        if ("Formateur".equals(currentViewType) && seance.getGroupe() != null) {
            infoLabel.setText("Groupe: " + seance.getGroupe().getMatricule());
        } else if ("Groupe".equals(currentViewType) && seance.getFormateur() != null) {
            infoLabel.setText("Formateur: " + seance.getFormateur().getNom() + " " + seance.getFormateur().getPrenom());
        }
        infoLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #34495e;");
        infoLabel.setWrapText(true);

        // Formater les heures
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Label timeLabel = new Label(startTime.format(timeFormatter) + " - " + endTime.format(timeFormatter));
        timeLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        content.getChildren().addAll(moduleLabel, infoLabel, timeLabel);

        // Conteneur pour les boutons d'action (en haut à droite)
        HBox actionsContainer = new HBox(5);
        actionsContainer.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        actionsContainer.setPadding(new javafx.geometry.Insets(5));
        actionsContainer.setOpacity(0.7); // Semi-transparent par défaut

        // Bouton modifier
        Button editButton = new Button();
        try {
            ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/crayon.png")));
            editIcon.setFitWidth(16);
            editIcon.setFitHeight(16);
            editButton.setGraphic(editIcon);
        } catch (Exception e) {
            editButton.setText("✏️");
            editButton.setStyle("-fx-font-size: 10px;");
        }
        editButton.setStyle("-fx-background-color: transparent; -fx-padding: 2px; -fx-cursor: hand;");
        editButton.setOnAction(e -> openEditSeanceDialog(seance));

        // Bouton supprimer
        Button deleteButton = new Button();
        try {
            ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/supprimer.png")));
            deleteIcon.setFitWidth(16);
            deleteIcon.setFitHeight(16);
            deleteButton.setGraphic(deleteIcon);
        } catch (Exception e) {
            deleteButton.setText("🗑️");
            deleteButton.setStyle("-fx-font-size: 10px;");
        }
        deleteButton.setStyle("-fx-background-color: transparent; -fx-padding: 2px; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> deleteSeance(seance));

        actionsContainer.getChildren().addAll(editButton, deleteButton);

        // Organisation dans le BorderPane
        seanceCell.setTop(actionsContainer);
        seanceCell.setCenter(content);

        // Ajouter au grid
        planningGrid.add(seanceCell, dayOfWeek, startRow, 1, durationHours);

        // Effet au survol
        seanceCell.setOnMouseEntered(e -> {
            seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()).replace("0.8", "1.0") +
                    "; -fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 3px;");
            actionsContainer.setOpacity(1.0); // Rendre complètement visible
        });

        seanceCell.setOnMouseExited(e -> {
            seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()) +
                    "; -fx-border-color: #b0bec5; -fx-border-width: 1px; -fx-border-radius: 3px;");
            actionsContainer.setOpacity(0.7); // Revenir semi-transparent
        });

        // Garder le double-clic pour modifier
        seanceCell.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openEditSeanceDialog(seance);
            }
        });
    }

    private void deleteSeance(Seance seance) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la séance");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette séance ?\n" +
                "Module: " + (seance.getModule() != null ? seance.getModule().getNom() : "N/A") + "\n" +
                "Date: " + seance.getDate() + "\n" +
                "Heure: " + seance.getHeureDebut() + " - " + seance.getHeureFin());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                seanceDAO.delete(seance.getId());
                showAlert("Succès", "Séance supprimée",
                        "La séance a été supprimée avec succès.", Alert.AlertType.INFORMATION);
                loadPlanning(); // Recharger l'affichage
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Échec de suppression",
                        "Impossible de supprimer la séance: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private String getColorForModule(int moduleId) {
        // Si moduleId est 0 ou négatif, utiliser une couleur par défaut
        if (moduleId <= 0) {
            return "rgba(220, 220, 220, 0.8)"; // Gris clair par défaut
        }

        // Générer une couleur basée sur l'ID du module
        int colorIndex = Math.abs(moduleId) % CELL_COLORS.length;
        Color color = CELL_COLORS[colorIndex];
        return String.format("rgba(%d, %d, %d, 0.8)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private void openAddSeanceDialog() {
        try {
            // Charger le dialogue d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/directeur/Gestion_Planning/AddEditSeanceDialog.fxml"));
            Parent root = loader.load();

            AddEditSeanceDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setSeanceToEdit(null); // Pour une nouvelle séance

            // Récupérer la fenêtre principale
            Stage stage = StageManager.getPrimaryStage();
            Scene scene = stage.getScene();

            if (scene == null) {
                // Si pas de scène existante, créer une nouvelle
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                // Sinon remplacer le root de la scène actuelle → Garde la taille
                scene.setRoot(root);
            }

            // Charger le CSS
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/styles/gestionFormateurs.css").toExternalForm()
            );

            // Mettre le titre
            stage.setTitle("Ajouter une Séance");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Ouverture impossible",
                    "Impossible d'ouvrir le dialogue d'ajout.", Alert.AlertType.ERROR);
        }
    }


    private void openEditSeanceDialog(Seance seance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/directeur/Gestion_Planning/AddEditSeanceDialog.fxml"));
            Parent root = loader.load();

            AddEditSeanceDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setSeanceToEdit(seance);

            // Récupérer la fenêtre principale
            Stage stage = StageManager.getPrimaryStage();
            Scene scene = stage.getScene();

            if (scene == null) {
                // Si pas de scène existante, créer une nouvelle
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                // Sinon remplacer le root de la scène actuelle → Garde la taille
                scene.setRoot(root);
            }

            // Charger le CSS
            scene.getStylesheets().clear();
            scene.getStylesheets().add(
                    getClass().getResource("/styles/gestionFormateurs.css").toExternalForm()
            );

            // Mettre le titre
            stage.setTitle("Modifier la Séance");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Ouverture impossible",
                    "Impossible d'ouvrir le dialogue de modification.", Alert.AlertType.ERROR);
        }
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



    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) currentWeekLabel.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.showAndWait();
    }
}