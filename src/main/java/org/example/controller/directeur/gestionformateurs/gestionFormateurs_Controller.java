package org.example.controller.directeur.gestionformateurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.property.SimpleStringProperty;
import org.example.dao.FormateurDAO;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class gestionFormateurs_Controller {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField matriculeField;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private ComboBox<String> situationComboBox;

    // TABLE
    @FXML private TableView<Formateur> formateursTable;
    @FXML private TableColumn<Formateur, String> nomColumn;
    @FXML private TableColumn<Formateur, String> matriculeColumn;
    @FXML private TableColumn<Formateur, String> sexeColumn;
    @FXML private TableColumn<Formateur, String> dateNaissanceColumn;
    @FXML private TableColumn<Formateur, String> situationColumn;
    @FXML private TableColumn<Formateur, String> dateRecrutementColumn;
    @FXML private TableColumn<Formateur, Void> actionsColumn;

    private FormateurDAO formateurDAO;
    private ObservableList<Formateur> formateursList = FXCollections.observableArrayList();
    private ObservableList<Formateur> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        formateurDAO = new FormateurDAO();

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
        loadFormateursFromDB();

        // --- CONFIGURATION DE LA TABLE ----
        configureTableColumns();
        configureNomColumn();
        configureActionsColumn();

        // --- GESTION DU FOCUS POUR LES LABELS FLOTTANTS ----
        setupFloatingLabels();

        formateursTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

    }

    // =====================================================================
    // ============= CHARGEMENT DES DONNÉES DEPUIS BD ======================
    // =====================================================================
    private void loadFormateursFromDB() {
        List<Formateur> list = formateurDAO.findByDirecteurId(
                SessionManager.getInstance().getUserId()
        );

        formateursList.setAll(list);
        filteredList.setAll(formateursList);
        formateursTable.setItems(filteredList);
    }

    private void loadComboBoxes() {
        sexeComboBox.getItems().setAll("Homme", "Femme");
        situationComboBox.getItems().setAll("Célibataire", "Marié(e)", "Divorcé(e)");

        // Style pour les ComboBox
        sexeComboBox.getStyleClass().add("float-text-field");
        situationComboBox.getStyleClass().add("float-text-field");
    }

    // =====================================================================
    // ============= CONFIGURATION DE LA TABLE =============================
    // =====================================================================
    private void configureTableColumns() {

        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getNom() + " " + cellData.getValue().getPrenom()
                )
        );

        matriculeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricule())
        );

        sexeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSexe().getValeur())
        );

        dateNaissanceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateNaissance().toString())
        );

        situationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSituation().getValeur())
        );

        dateRecrutementColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateRecrutement().toString())
        );

        // 🔹 appliquer le style "other-column"
        applyOtherColumnStyle(matriculeColumn);
        applyOtherColumnStyle(sexeColumn);
        applyOtherColumnStyle(dateNaissanceColumn);
        applyOtherColumnStyle(situationColumn);
        applyOtherColumnStyle(dateRecrutementColumn);
    }

    private <T> void applyOtherColumnStyle(TableColumn<Formateur, T> column) {
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



    private void configureNomColumn() {
        nomColumn.setCellFactory(new Callback<TableColumn<Formateur, String>, TableCell<Formateur, String>>() {
            @Override
            public TableCell<Formateur, String> call(TableColumn<Formateur, String> param) {
                return new TableCell<Formateur, String>() {
                    private final VBox container = new VBox(2);
                    private final Label nomLabel = new Label();
                    private final Label emailLabel = new Label();

                    {
                        container.getStyleClass().add("nom-container");
                        container.setAlignment(Pos.CENTER);
                        nomLabel.getStyleClass().add("nom-text");
                        emailLabel.getStyleClass().add("email-text");
                        container.getChildren().addAll(nomLabel, emailLabel);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                            setText(null);
                            getStyleClass().remove("nom-cell");
                        } else {
                            Formateur formateur = getTableRow().getItem();
                            nomLabel.setText(formateur.getPrenom() + " " + formateur.getNom());
                            emailLabel.setText(formateur.getEmail());

                            setGraphic(container);
                            setText(null);
                            getStyleClass().add("nom-cell");
                            setAlignment(Pos.CENTER_LEFT);
                        }
                    }
                };
            }
        });
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<Formateur, Void>, TableCell<Formateur, Void>>() {
            @Override
            public TableCell<Formateur, Void> call(final TableColumn<Formateur, Void> param) {
                return new TableCell<Formateur, Void>() {
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
                            Formateur formateur = getTableView().getItems().get(getIndex());
                            handleEdit(formateur);
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
                            Formateur formateur = getTableView().getItems().get(getIndex());
                            handleDelete(formateur);
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
        setupFloatingLabelForField(prenomField, "Prenom");
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
    private void handleEdit(Formateur formateur) {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/directeur/Gestion_Formateurs/Modifier_Formateur.fxml")
            );
            Parent root = loader.load();

            // Récupérer le controller de modification
            Modifier_Formateur_Controller controller = loader.getController();
            controller.setFormateurToEdit(formateur);

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
            stage.setTitle("Modifier Formateur");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    private void handleDelete(Formateur formateur) {

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow(); // 🔑 fenêtre courante

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);                 // ✅ OBLIGATOIRE
        confirm.initModality(Modality.WINDOW_MODAL);

        confirm.setTitle("Supprimer formateur");
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setContentText(
                "Voulez-vous vraiment supprimer "
                        + formateur.getPrenom() + " " + formateur.getNom() + " ?"
        );

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo  = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        confirm.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {

                formateurDAO.delete(formateur.getId());
                loadFormateursFromDB();

                showAlert(
                        "Succès",
                        "Formateur supprimé",
                        formateur.getNomComplet() + " a été supprimé avec succès.",
                        Alert.AlertType.INFORMATION
                );
            }
        });
    }


    // =====================================================================
    // ================ BOUTON : FILTRER ===================================
    // =====================================================================
    @FXML
    private void filtrerFormateurs() {
        String nom = nomField.getText().toLowerCase();
        String prenom = prenomField.getText().toLowerCase();
        String matricule = matriculeField.getText().toLowerCase();
        String sexe = sexeComboBox.getValue();
        String situation = situationComboBox.getValue();

        List<Formateur> filtered = formateursList.stream()
                .filter(f ->
                        (nom.isEmpty() || f.getNom().toLowerCase().contains(nom)) &&
                                (prenom.isEmpty() || f.getPrenom().toLowerCase().contains(prenom)) &&
                                (matricule.isEmpty() || f.getMatricule().toLowerCase().contains(matricule)) &&
                                (sexe == null || f.getSexe().getValeur().equals(sexe)) &&
                                (situation == null || f.getSituation().getValeur().equals(situation))
                )
                .collect(Collectors.toList());

        filteredList.setAll(filtered);

        if (filtered.isEmpty() && (!nom.isEmpty() || !prenom.isEmpty() || !matricule.isEmpty() ||
                sexe != null || situation != null)) {
            showAlert("Information", "Aucun résultat",
                    "Aucun formateur ne correspond aux critères de recherche.",
                    Alert.AlertType.INFORMATION);
        }
    }

    // =====================================================================
    // ================ BOUTON : RÉINITIALISER =============================
    // =====================================================================
    @FXML
    private void reinitialiserFiltres() {
        nomField.clear();
        prenomField.clear();
        matriculeField.clear();
        sexeComboBox.setValue(null);
        situationComboBox.setValue(null);

        // Réinitialiser les styles des champs
        nomField.getStyleClass().removeAll("has-text", "focused");
        prenomField.getStyleClass().removeAll("has-text", "focused");
        matriculeField.getStyleClass().removeAll("has-text", "focused");

        filteredList.setAll(formateursList);
    }

    @FXML
    private void rafraichirDonnees() {
        loadFormateursFromDB();
        reinitialiserFiltres();
    }

    // =====================================================================
    // ================ BOUTON : AJOUTER FORMATEUR =========================
    // =====================================================================
    @FXML
    private void ajouterFormateur() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Formateurs/Ajouter_Formateur.fxml", "/styles/gestionFormateurs.css", "Ajouter Formateur");
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

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow(); // n'importe quel champ
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);


    }
}