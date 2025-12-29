package org.example.controller.directeur.gestionplannings;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

        // Définir les dimensions - chaque ligne = 30 minutes
        int numDays = 6; // Lundi à Samedi
        int numSlots = 24; // 8h à 19h avec des créneaux de 30 minutes (12h * 2)

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

        // Configurer les lignes (créneaux de 30 minutes)
        for (int row = 0; row <= numSlots; row++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPrefHeight(30); // Hauteur de 30px par créneau
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

        // En-tête des heures (toutes les heures)
        for (int hour = 8; hour <= 19; hour++) {
            // Chaque heure correspond à 2 créneaux
            int rowStart = (hour - 8) * 2 + 1;

            // Afficher l'heure pleine
            Label hourLabel = new Label(String.format("%02d:00", hour));
            hourLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center-right;");
            hourLabel.setPadding(new javafx.geometry.Insets(5));
            planningGrid.add(hourLabel, 0, rowStart);

            // Afficher la demi-heure (si ce n'est pas la dernière heure)
            if (hour < 19) {
                Label halfHourLabel = new Label(String.format("%02d:30", hour));
                halfHourLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 10px; -fx-alignment: center-right; -fx-text-fill: #666;");
                halfHourLabel.setPadding(new javafx.geometry.Insets(5));
                planningGrid.add(halfHourLabel, 0, rowStart + 1);
            }
        }

        // Charger et afficher les séances
        loadAndDisplaySeances();
    }

    private void loadAndDisplaySeances() {
        if (currentEntityId == null) return;
        overlapCounter.clear();

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

        // Réinitialiser le compteur de chevauchements
        overlapCounter.clear();

        // Trier les séances par heure de début pour un meilleur affichage
        seances.sort(Comparator.comparing(Seance::getHeureDebut));

        System.out.println("=== CHARGEMENT SÉANCES ===");
        System.out.println("Nombre de séances: " + seances.size());

        // Afficher les séances
        for (Seance seance : seances) {
            addSeanceToGrid(seance);
        }
    }

    private Map<String, Integer> overlapCounter = new HashMap<>();

    private int getOverlapIndex(int dayOfWeek, int startSlot, int durationSlots) {
        String key = dayOfWeek + "-" + startSlot;

        // Vérifier combien de séances sont déjà dans ce créneau
        int count = overlapCounter.getOrDefault(key, 0);

        // Incrémenter pour la prochaine séance
        overlapCounter.put(key, count + 1);

        return count;
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

        // Calculer les lignes basées sur les minutes précises
        LocalTime startTime = seance.getHeureDebut();
        LocalTime endTime = seance.getHeureFin();

        // Convertir les heures en créneaux de 30 minutes
        int startSlot = calculateTimeSlot(startTime);
        int endSlot = calculateTimeSlot(endTime);

        // S'assurer que les valeurs sont valides
        if (startSlot < 0) startSlot = 0;
        if (endSlot > 24) endSlot = 24;

        int durationSlots = endSlot - startSlot;
        if (durationSlots < 1) {
            durationSlots = 1;
        }

        // Ajuster le row index (ajouter 1 pour la ligne d'en-tête)
        int startRow = startSlot + 1;

        // DEBUG - Supprimez ou gardez pour vérification
        System.out.println("Séance " + seance.getModule().getNom() +
                " (" + startTime + "-" + endTime + ")" +
                " -> ligne " + startRow + " | durée " + durationSlots);

        // Créer la cellule de séance
        BorderPane seanceCell = new BorderPane();
        seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()) +
                "; -fx-border-color: #b0bec5; -fx-border-width: 1px; -fx-border-radius: 3px;");
        seanceCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Contenu principal
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
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("H'h'mm");
        Label timeLabel = new Label(startTime.format(displayFormatter) + " - " + endTime.format(displayFormatter));
        timeLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        content.getChildren().addAll(moduleLabel, infoLabel, timeLabel);

        // CONTENEUR PRINCIPAL avec StackPane pour superposer les boutons
        StackPane mainContainer = new StackPane();
        mainContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Ajouter le contenu
        mainContainer.getChildren().add(content);

        // Créer les boutons
        Button editButton = createIconButton("/images/crayon.png", "✏️", e -> openEditSeanceDialog(seance));
        Button deleteButton = createIconButton("/images/supprimer.png", "🗑️", e -> deleteSeance(seance));

        // Positionner les boutons en haut à droite DANS le StackPane
        HBox buttonsContainer = new HBox(5, editButton, deleteButton);
        buttonsContainer.setAlignment(Pos.TOP_RIGHT);
        buttonsContainer.setPadding(new Insets(5, 5, 0, 0));
        buttonsContainer.setOpacity(0.7);

        // Ajouter les boutons au StackPane (ils seront par-dessus le contenu)
        mainContainer.getChildren().add(buttonsContainer);

        // Positionner les boutons dans le StackPane
        StackPane.setAlignment(buttonsContainer, Pos.TOP_RIGHT);

        // Organisation dans le BorderPane
        seanceCell.setCenter(mainContainer);

        // Ajouter au grid
        planningGrid.add(seanceCell, dayOfWeek, startRow, 1, durationSlots);

        // Effet au survol
        seanceCell.setOnMouseEntered(e -> {
            seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()).replace("0.8", "1.0") +
                    "; -fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 3px;");
            buttonsContainer.setOpacity(1.0);
        });

        seanceCell.setOnMouseExited(e -> {
            seanceCell.setStyle("-fx-background-color: " + getColorForModule(seance.getModuleId()) +
                    "; -fx-border-color: #b0bec5; -fx-border-width: 1px; -fx-border-radius: 3px;");
            buttonsContainer.setOpacity(0.7);
        });

        // Double-clic pour modifier
        seanceCell.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openEditSeanceDialog(seance);
            }
        });
    }

    // Méthode utilitaire pour créer des boutons avec icônes
    private Button createIconButton(String iconPath, String fallbackText, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button();
        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitWidth(16);
            icon.setFitHeight(16);
            button.setGraphic(icon);
        } catch (Exception e) {
            button.setText(fallbackText);
            button.setStyle("-fx-font-size: 10px;");
        }
        button.setStyle("-fx-background-color: transparent; -fx-padding: 2px; -fx-cursor: hand;");
        button.setOnAction(handler);
        return button;
    }


    // Méthode utilitaire pour convertir une heure en créneau de 30 minutes
    private int calculateTimeSlot(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        // Chaque heure = 2 slots
        int slot = (hour - 8) * 2;

        if (minute >= 30) {
            slot += 1;
        }

        return slot;
    }




    // Méthode utilitaire pour convertir un créneau en LocalTime
    private LocalTime slotToTime(int slot) {
        int totalMinutes = slot * 30;
        int hour = 8 + (totalMinutes / 60);
        int minute = totalMinutes % 60;
        return LocalTime.of(hour, minute);
    }

    private void deleteSeance(Seance seance) {
        Stage stage = (Stage) currentWeekLabel.getScene().getWindow(); // ⭐ fenêtre courante

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);                 // 🔑 IMPORTANT
        confirm.initModality(Modality.WINDOW_MODAL);

        confirm.setTitle("Supprimer séance");
        confirm.setHeaderText("Confirmer la suppression");

        // Construire le message détaillé
        StringBuilder content = new StringBuilder();
        content.append("Voulez-vous vraiment supprimer cette séance ?\n\n");

        if (seance.getModule() != null) {
            content.append("Module: ").append(seance.getModule().getNom()).append("\n");
        }

        content.append("Date: ").append(seance.getDate()).append("\n");
        content.append("Heure: ").append(seance.getHeureDebut()).append(" - ").append(seance.getHeureFin()).append("\n");

        if ("Formateur".equals(currentViewType) && seance.getGroupe() != null) {
            content.append("Groupe: ").append(seance.getGroupe().getMatricule()).append("\n");
        } else if ("Groupe".equals(currentViewType) && seance.getFormateur() != null) {
            content.append("Formateur: ").append(seance.getFormateur().getNom())
                    .append(" ").append(seance.getFormateur().getPrenom()).append("\n");
        }

        confirm.setContentText(content.toString());

        // Boutons personnalisés
        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        confirm.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                try {
                    seanceDAO.delete(seance.getId());
                    loadPlanning(); // Recharger l'affichage

                    // Message de succès (aussi modale)
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.initOwner(stage);
                    success.initModality(Modality.WINDOW_MODAL);
                    success.setTitle("Succès");
                    success.setHeaderText("Séance supprimée");
                    success.setContentText("La séance a été supprimée avec succès.");
                    success.showAndWait();

                } catch (Exception e) {
                    e.printStackTrace();

                    // Message d'erreur (modale)
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.initOwner(stage);
                    error.initModality(Modality.WINDOW_MODAL);
                    error.setTitle("Erreur");
                    error.setHeaderText("Échec de suppression");
                    error.setContentText("Impossible de supprimer la séance: " + e.getMessage());
                    error.showAndWait();
                }
            }
        });
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
    private void handleEtudiants() {
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
            StageManager.loadScene("/view/directeur/gestionNotesDirecteur.fxml",
                    "/styles/gestionFormateurs.css", "Gestion des Notes");
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