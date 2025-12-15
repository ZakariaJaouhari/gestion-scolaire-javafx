package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import org.example.dao.EtudiantDAO;
import org.example.dao.GroupeDAO;
import org.example.model.Directeur;
import org.example.model.Etudiant;
import org.example.model.Groupe;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public class gestionEtudiant_Controller {


    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> sexeComboBox;
    @FXML private ComboBox<String> groupeComboBox;

    // TABLE
    @FXML private TableView<Etudiant> etudiantsTable;
    @FXML private TableColumn<Etudiant, String> nomColumn;
    @FXML private TableColumn<Etudiant, String> sexeColumn;
    @FXML private TableColumn<Etudiant, String> dateNaissanceColumn;
    @FXML private TableColumn<Etudiant, String> cinColumn;
    @FXML private TableColumn<Etudiant, String> groupeColumn;
    @FXML private TableColumn<Etudiant, Void> actionsColumn;

    private final GroupeDAO groupeDAO = new GroupeDAO();

    private EtudiantDAO etudiantDAO;
    private ObservableList<Etudiant> etudiantList = FXCollections.observableArrayList();
    private ObservableList<Etudiant> filteredList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        etudiantDAO = new EtudiantDAO();

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
        loadEtudiantsFromDB();

        // --- CONFIGURATION DE LA TABLE ----
        configureTableColumns();
        configureNomColumn();
        configureActionsColumn();

        // --- GESTION DU FOCUS POUR LES LABELS FLOTTANTS ----
        setupFloatingLabels();

        etudiantsTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

    }


    // =====================================================================
    // ============= CHARGEMENT DES DONNÉES DEPUIS BD ======================
    // =====================================================================
    private void loadEtudiantsFromDB() {
        List<Etudiant> list = etudiantDAO.findByDirecteurId(
                SessionManager.getInstance().getUserId()
        );

        etudiantList.setAll(list);
        filteredList.setAll(etudiantList);
        etudiantsTable.setItems(filteredList);
    }

    private void loadComboBoxes() {

        // 🔹 Sexe
        sexeComboBox.getItems().setAll("Homme", "Femme");

        // 🔹 Groupes (matricules)
        Directeur directeur = SessionManager.getInstance().getCurrentDirecteur();
        int directeurId = directeur.getId();


        List<Groupe> groupes = groupeDAO.findByDirecteurId((int) directeurId);

        List<String> matricules = groupes.stream()
                .map(Groupe::getMatricule)
                .toList();

        groupeComboBox.getItems().clear();
        groupeComboBox.getItems().addAll(matricules);

        // 🔹 Style
        sexeComboBox.getStyleClass().add("float-text-field");
        groupeComboBox.getStyleClass().add("float-text-field");
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

        sexeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSexe().getValeur())
        );

        dateNaissanceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateNaissance().toString())
        );

        cinColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCin())
        );

        groupeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGroupe().getMatricule())
        );


        // 🔹 appliquer le style "other-column"
        applyOtherColumnStyle(sexeColumn);
        applyOtherColumnStyle(dateNaissanceColumn);
        applyOtherColumnStyle(cinColumn);
        applyOtherColumnStyle(groupeColumn);
    }


    private <T> void applyOtherColumnStyle(TableColumn<Etudiant, T> column) {
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
        nomColumn.setCellFactory(new Callback<TableColumn<Etudiant, String>, TableCell<Etudiant, String>>() {
            @Override
            public TableCell<Etudiant, String> call(TableColumn<Etudiant, String> param) {
                return new TableCell<Etudiant, String>() {
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
                            Etudiant etudiant = getTableRow().getItem();
                            nomLabel.setText(etudiant.getPrenom() + " " + etudiant.getNom());
                            emailLabel.setText(etudiant.getEmail());

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
        actionsColumn.setCellFactory(new Callback<TableColumn<Etudiant, Void>, TableCell<Etudiant, Void>>() {
            @Override
            public TableCell<Etudiant, Void> call(final TableColumn<Etudiant, Void> param) {
                return new TableCell<Etudiant, Void>() {
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
                            Etudiant etudiant = getTableView().getItems().get(getIndex());
                            handleEdit(etudiant);
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
                            Etudiant etudiant = getTableView().getItems().get(getIndex());
                            handleDelete(etudiant);
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
        setupFloatingLabelForField(cinField, "CIN");
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
    private void handleEdit(Etudiant etudiant) {

    }

    private void handleDelete(Etudiant etudiant) {

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow(); // ⭐ fenêtre courante

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);                 // 🔑 IMPORTANT
        confirm.initModality(Modality.WINDOW_MODAL);

        confirm.setTitle("Supprimer étudiant");
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setContentText(
                "Voulez-vous vraiment supprimer "
                        + etudiant.getPrenom() + " " + etudiant.getNom() + " ?"
        );

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo  = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        confirm.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {

                etudiantDAO.delete(etudiant.getId());
                loadEtudiantsFromDB(); // rafraîchir la table

                showAlert(
                        "Succès",
                        "Étudiant supprimé",
                        etudiant.getNomComplet() + " a été supprimé avec succès.",
                        Alert.AlertType.INFORMATION
                );
            }
        });
    }




    // =====================================================================
    // ================ BOUTON : FILTRER ===================================
    // =====================================================================
    @FXML
    private void filtrerEtudiants() {
        String nom = nomField.getText().toLowerCase();
        String prenom = prenomField.getText().toLowerCase();
        String cin = cinField.getText().toLowerCase();
        String sexe = sexeComboBox.getValue();
        String groupe = groupeComboBox.getValue();

        List<Etudiant> filtered = etudiantList.stream()
                .filter(f ->
                        (nom.isEmpty() || f.getNom().toLowerCase().contains(nom)) &&
                                (prenom.isEmpty() || f.getPrenom().toLowerCase().contains(prenom)) &&
                                (cin.isEmpty() || f.getCin().toLowerCase().contains(cin)) &&
                                (sexe == null || f.getSexe().getValeur().equals(sexe)) &&
                                (groupe == null || f.getGroupe().getMatricule().equals(groupe))
                )
                .collect(Collectors.toList());

        filteredList.setAll(filtered);

        if (filtered.isEmpty() && (!nom.isEmpty() || !prenom.isEmpty() || !cin.isEmpty() ||
                sexe != null || groupe != null)) {
            showAlert("Information", "Aucun résultat",
                    "Aucun etudiant ne correspond aux critères de recherche.",
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
        cinField.clear();
        sexeComboBox.setValue(null);
        groupeComboBox.setValue(null);

        // Réinitialiser les styles des champs
        nomField.getStyleClass().removeAll("has-text", "focused");
        prenomField.getStyleClass().removeAll("has-text", "focused");
        cinField.getStyleClass().removeAll("has-text", "focused");

        filteredList.setAll(etudiantList);
    }

    @FXML
    private void rafraichirDonnees() {
        loadEtudiantsFromDB();
        reinitialiserFiltres();
    }


    // =====================================================================
    // ================ BOUTON : AJOUTER ETUDIANT =========================
    // =====================================================================
    @FXML
    private void ajouterEtudiant() {
        try {
            StageManager.loadScene("/view/directeur/Ajouter_Etudiant.fxml", "/styles/gestionFormateurs.css", "Ajouter Etudiant");
        } catch (IOException e) {
            e.printStackTrace();
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
            StageManager.loadScene("/view/directeur/Gestion Formateurs/gestionFormateurs.fxml", "/styles/gestionFormateurs.css", "Formateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStagiaires() {
        // On est déjà sur cette page, on rafraîchit juste les données
        rafraichirDonnees();
    }

    @FXML
    private void handleGroupes() {
        try {
            StageManager.loadScene("/view/directeur/gestionGroupes.fxml", "/styles/gestionGroupes.css", "Groupes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModules() {
        try {
            StageManager.loadScene("/view/directeur/gestionModules.fxml", "/styles/gestionModules.css", "Modules");
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

