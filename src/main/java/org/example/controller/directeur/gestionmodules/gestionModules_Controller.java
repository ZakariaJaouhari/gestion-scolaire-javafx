package org.example.controller.directeur.gestionmodules;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import org.example.dao.FormateurDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Directeur;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public class gestionModules_Controller {


    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private TextField nomField;
    @FXML private TextField matriculeField;
    @FXML private ComboBox<String> formateurComboBox;

    // TABLE
    @FXML private TableView<org.example.model.Module> modulesTable;
    @FXML private TableColumn<org.example.model.Module, String> nomColumn;
    @FXML private TableColumn<org.example.model.Module, String> dateDebutColumn;
    @FXML private TableColumn<org.example.model.Module, String> dateFinColumn;
    @FXML private TableColumn<org.example.model.Module, String> matriculeColumn;
    @FXML private TableColumn<org.example.model.Module, String> heuresColumn;
    @FXML private TableColumn<org.example.model.Module, Integer> cofColumn;
    @FXML private TableColumn<org.example.model.Module, String> formateurColumn;
    @FXML private TableColumn<org.example.model.Module, Void> actionsColumn;

    private final FormateurDAO formateurDAO = new FormateurDAO();

    private ModuleDAO moduleDAO;
    private ObservableList<org.example.model.Module> moduleList = FXCollections.observableArrayList();
    private ObservableList<org.example.model.Module> filteredList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        moduleDAO = new ModuleDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            nomDirecteurLabel.setText(session.getNomDirecteur());
        }
        versionLabel.setText("V 0.1.0");

        // --- INITIALISATION DES COMBOBOXES ----
        loadComboBoxes();

        // --- CHARGEMENT DES DONNÉES ----
        loadModulesFromDB();

        // --- CONFIGURATION DE LA TABLE ----
        configureTableColumns();
        configureActionsColumn();

        // --- GESTION DU FOCUS POUR LES LABELS FLOTTANTS ----
        setupFloatingLabels();

        modulesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

    }


    // =====================================================================
    // ============= CHARGEMENT DES DONNÉES DEPUIS BD ======================
    // =====================================================================
    private void loadModulesFromDB() {
        List<org.example.model.Module> list = moduleDAO.findByDirecteurId(
                SessionManager.getInstance().getUserId()
        );

        moduleList.setAll(list);
        filteredList.setAll(moduleList);
        modulesTable.setItems(filteredList);
    }

    private void loadComboBoxes() {

        // 🔹
        Directeur directeur = SessionManager.getInstance().getCurrentDirecteur();
        int directeurId = directeur.getId();


        List<Formateur> formateurs = formateurDAO.findByDirecteurId((int) directeurId);

        List<String> noms = formateurs.stream()
                .map(Formateur::getNomComplet)
                .toList();

        formateurComboBox.getItems().clear();
        formateurComboBox.getItems().addAll(noms);

        // 🔹 Style
        formateurComboBox.getStyleClass().add("float-text-field");
    }



    // =====================================================================
    // ============= CONFIGURATION DE LA TABLE =============================
    // =====================================================================
    private void configureTableColumns() {

        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getNom()
                )
        );
        matriculeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricule())
        );
        dateDebutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateDebut().toString())
        );
        dateFinColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateFin().toString())
        );
        heuresColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHeuresPratique())
        );
        cofColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCoefficient()).asObject()
        );
        formateurColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormateur().getNomComplet())
        );


        // 🔹 appliquer le style "other-column"
        applyOtherColumnStyle(nomColumn);
        applyOtherColumnStyle(matriculeColumn);
        applyOtherColumnStyle(dateDebutColumn);
        applyOtherColumnStyle(dateFinColumn);
        applyOtherColumnStyle(heuresColumn);
        applyOtherColumnStyle(cofColumn);
        applyOtherColumnStyle(formateurColumn);
    }


    private <T> void applyOtherColumnStyle(TableColumn<org.example.model.Module, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
                if (!empty) {
                    getStyleClass().add("other-column");
                }
            }
        });
    }



    private void configureActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<org.example.model.Module, Void>, TableCell<org.example.model.Module, Void>>() {
            @Override
            public TableCell<org.example.model.Module, Void> call(final TableColumn<org.example.model.Module, Void> param) {
                return new TableCell<org.example.model.Module, Void>() {
                    private final HBox container = new HBox(12);
                    private final Button editButton = new Button();
                    private final Button deleteButton = new Button();

                    {
                        container.getStyleClass().add("actions-container");

                        // Bouton modifier
                        try {
                            ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/crayon.png")));
                            editIcon.setFitWidth(20);
                            editIcon.setFitHeight(20);
                            editButton.setGraphic(editIcon);
                        } catch (Exception e) {
                            editButton.setText("✏️");
                        }
                        editButton.getStyleClass().add("action-button");
                        editButton.setOnAction(event -> {
                            org.example.model.Module module = getTableView().getItems().get(getIndex());
                            handleEdit(module);
                        });

                        // Bouton supprimer
                        try {
                            ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/supprimer.png")));
                            deleteIcon.setFitWidth(20);
                            deleteIcon.setFitHeight(20);
                            deleteButton.setGraphic(deleteIcon);
                        } catch (Exception e) {
                            deleteButton.setText("🗑️");
                        }
                        deleteButton.getStyleClass().add("action-button");
                        deleteButton.setOnAction(event -> {
                            org.example.model.Module module = getTableView().getItems().get(getIndex());
                            handleDelete(module);
                        });

                        container.getChildren().addAll(editButton, deleteButton);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                            getStyleClass().remove("actions-cell");
                        } else {
                            setGraphic(container);
                            getStyleClass().setAll("table-cell", "actions-cell");
                        }
                    }

                };
            }
        });
    }


    private void setupFloatingLabels() {
        // Gestion des labels flottants pour les champs de filtrage
        setupFloatingLabelForField(nomField, "Nom");
        setupFloatingLabelForField(matriculeField, "Matricule");
    }

    private void setupFloatingLabelForField(TextField field, String labelText) {
        // Cette logique serait mieux gérée dans le CSS avec des classes
        // Ici, on ajoute juste les classes CSS appropriées
        field.getStyleClass().add("float-text-field");
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                field.getStyleClass().add("has-text");
            } else {
                field.getStyleClass().remove("has-text");
            }
        });

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal || !field.getText().isEmpty()) {
                field.getStyleClass().add("focused");
            } else {
                field.getStyleClass().remove("focused");
            }
        });
    }



    // =====================================================================
    // ================ GESTION DES ACTIONS ================================
    // =====================================================================
    @FXML
    private void handleEdit(org.example.model.Module module) {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/directeur/Gestion_Modules/Modifier_Module.fxml")
            );
            Parent root = loader.load();

            // Récupérer le controller de modification
            Modifier_Module_Controller controller = loader.getController();
            controller.setModuleToEdit(module);

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
            stage.setTitle("Modifier Module");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(org.example.model.Module module) {

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow(); // ⭐ fenêtre courante

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);                 // 🔑 IMPORTANT
        confirm.initModality(Modality.WINDOW_MODAL);

        confirm.setTitle("Supprimer module");
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setContentText(
                "Voulez-vous vraiment supprimer "
                        +  module.getNom() + " ?"
        );

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo  = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        confirm.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {

                moduleDAO.delete(module.getId());
                loadModulesFromDB(); // rafraîchir la table

                showAlert(
                        "Succès",
                        "Module supprimé",
                        module.getNom() + " a été supprimé avec succès.",
                        Alert.AlertType.INFORMATION
                );
            }
        });
    }




    // =====================================================================
    // ================ BOUTON : FILTRER ===================================
    // =====================================================================
    @FXML
    private void filtrerModules() {
        String nom = nomField.getText().toLowerCase();
        String matricule = matriculeField.getText().toLowerCase();
        String formateur = formateurComboBox.getValue();

        List<org.example.model.Module> filtered = moduleList.stream()
                .filter(f ->
                        (nom.isEmpty() || f.getNom().toLowerCase().contains(nom)) &&
                                (matricule.isEmpty() || f.getMatricule().toLowerCase().contains(matricule)) &&
                                (formateur == null || f.getFormateur().getMatricule().equals(formateur))
                )
                .collect(Collectors.toList());

        filteredList.setAll(filtered);

        if (filtered.isEmpty() && (!nom.isEmpty() || !matricule.isEmpty() || formateur != null)) {
            showAlert("Information", "Aucun résultat",
                    "Aucun module ne correspond aux critères de recherche.",
                    Alert.AlertType.INFORMATION);
        }
    }

    // =====================================================================
    // ================ BOUTON : RÉINITIALISER =============================
    // =====================================================================
    @FXML
    private void reinitialiserFiltres() {
        nomField.clear();
        matriculeField.clear();
        formateurComboBox.setValue(null);

        // Réinitialiser les styles des champs
        nomField.getStyleClass().removeAll("has-text", "focused");
        matriculeField.getStyleClass().removeAll("has-text", "focused");

        filteredList.setAll(moduleList);
    }

    @FXML
    private void rafraichirDonnees() {
        loadModulesFromDB();
        reinitialiserFiltres();
    }


    // =====================================================================
    // ================ BOUTON : AJOUTER MODULE =========================
    // =====================================================================
    @FXML
    private void ajouterModule() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Modules/Ajouter_Module.fxml", "/styles/gestionFormateurs.css", "Ajouter Module");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

