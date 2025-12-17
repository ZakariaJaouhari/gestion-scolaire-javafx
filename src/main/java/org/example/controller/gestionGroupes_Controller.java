package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.dao.GroupeDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Groupe;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class gestionGroupes_Controller {
    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;


    @FXML private TableColumn<Groupe, Void> actionsColumn;

    // FILTRES
    @FXML private TextField matriculeField;
    @FXML private ComboBox<String> niveauComboBox;

    private GroupeDAO groupeDAO;
    private ModuleDAO moduleDAO;
    private ObservableList<Groupe> groupeList = FXCollections.observableArrayList();
    private ObservableList<Groupe> filteredList = FXCollections.observableArrayList();



    @FXML private TableView<Groupe> groupesTable;
    @FXML private TableColumn<Groupe, String> matriculeColumn;
    @FXML private TableColumn<Groupe, String> niveauColumn;
    @FXML private TableColumn<Groupe, Void> modulesColumn;

    // Classe interne pour les données de module dans le tableau
    public static class ModuleTableData {
        private final SimpleStringProperty nomModule;
        private final SimpleStringProperty matriculeModule;
        private final SimpleStringProperty formateurNom;

        public ModuleTableData(String nomModule, String matriculeModule, String formateurNom) {
            this.nomModule = new SimpleStringProperty(nomModule);
            this.matriculeModule = new SimpleStringProperty(matriculeModule);
            this.formateurNom = new SimpleStringProperty(formateurNom);
        }

        public String getNomModule() { return nomModule.get(); }
        public void setNomModule(String value) { nomModule.set(value); }

        public String getMatriculeModule() { return matriculeModule.get(); }
        public void setMatriculeModule(String value) { matriculeModule.set(value); }

        public String getFormateurNom() { return formateurNom.get(); }
        public void setFormateurNom(String value) { formateurNom.set(value); }
    }



    @FXML
    public void initialize() {
        groupeDAO = new GroupeDAO();
        moduleDAO = new ModuleDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            nomDirecteurLabel.setText(session.getNomDirecteur());
        }
        versionLabel.setText("V 0.1.0");

        // Initialiser les ComboBox
        loadComboBoxes();

        // Configurer les colonnes du tableau
        configureTableColumns();

        // Charger les données
        loadGroupesFromDB();

    }

    private void configureTableColumns() {
        // Colonne Matricule
        matriculeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricule()));

        matriculeColumn.setCellFactory(new Callback<TableColumn<Groupe, String>, TableCell<Groupe, String>>() {
            @Override
            public TableCell<Groupe, String> call(TableColumn<Groupe, String> param) {
                return new TableCell<Groupe, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(10);
                            hbox.setStyle("-fx-alignment: center-left; -fx-padding: 0 0 0 30;");

                            Label labelMatricule = new Label("Matricule:");
                            labelMatricule.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

                            Label valueMatricule = new Label(item);
                            valueMatricule.setStyle("-fx-text-fill: #4b5563;");

                            hbox.getChildren().addAll(labelMatricule, valueMatricule);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });

        // Colonne Niveau
        niveauColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNiveau().getValeur()));

        niveauColumn.setCellFactory(new Callback<TableColumn<Groupe, String>, TableCell<Groupe, String>>() {
            @Override
            public TableCell<Groupe, String> call(TableColumn<Groupe, String> param) {
                return new TableCell<Groupe, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(10);
                            hbox.setStyle("-fx-alignment: center-left; -fx-padding: 0 0 0 50;");

                            Label labelNiveau = new Label("Niveau:");
                            labelNiveau.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

                            Label valueNiveau = new Label(item);
                            valueNiveau.setStyle("-fx-text-fill: #4b5563;");

                            hbox.getChildren().addAll(labelNiveau, valueNiveau);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });

        // Colonne Modules (imbriquée)
        modulesColumn.setCellFactory(new Callback<TableColumn<Groupe, Void>, TableCell<Groupe, Void>>() {
            @Override
            public TableCell<Groupe, Void> call(TableColumn<Groupe, Void> param) {
                return new TableCell<Groupe, Void>() {
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Groupe groupe = getTableView().getItems().get(getIndex());

                            VBox container = new VBox(5);
                            container.setStyle("-fx-padding: 0 0 0 80;");

                            // Vérifier si le groupe a des modules
                            if (groupe.getModules() == null || groupe.getModules().isEmpty()) {
                                Label noModulesLabel = new Label("Aucun module associé");
                                noModulesLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
                                container.getChildren().add(noModulesLabel);
                            } else {
                                // Créer le tableau imbriqué pour les modules
                                TableView<ModuleTableData> modulesTable = createModulesTableView(groupe);
                                container.getChildren().add(modulesTable);
                            }

                            setGraphic(container);
                        }
                    }

                    private TableView<ModuleTableData> createModulesTableView(Groupe groupe) {
                        TableView<ModuleTableData> table = new TableView<>();

                        // Colonne Nom module
                        TableColumn<ModuleTableData, String> nomModuleCol = new TableColumn<>("Nom module");
                        nomModuleCol.setPrefWidth(250);
                        nomModuleCol.setCellValueFactory(cellData ->
                                new SimpleStringProperty(cellData.getValue().getNomModule()));
                        nomModuleCol.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 0 0 1 0;");

                        // Colonne Matricule module
                        TableColumn<ModuleTableData, String> matriculeModuleCol = new TableColumn<>("Matricule module");
                        matriculeModuleCol.setPrefWidth(150);
                        matriculeModuleCol.setCellValueFactory(cellData ->
                                new SimpleStringProperty(cellData.getValue().getMatriculeModule()));
                        matriculeModuleCol.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 0 0 1 0;");

                        // Colonne Formateur
                        TableColumn<ModuleTableData, String> formateurCol = new TableColumn<>("Formateur");
                        formateurCol.setPrefWidth(200);
                        formateurCol.setCellValueFactory(cellData ->
                                new SimpleStringProperty(cellData.getValue().getFormateurNom()));
                        formateurCol.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 0 0 1 0 1;");

                        // Ajouter les colonnes
                        table.getColumns().addAll(nomModuleCol, matriculeModuleCol, formateurCol);

                        // Configurer le style
                        table.setStyle("-fx-background-color: transparent; " +
                                "-fx-table-cell-border-color: #d1d5db; " +
                                "-fx-border-color: #d1d5db; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 3;");

                        // Charger les données
                        ObservableList<ModuleTableData> modulesData = FXCollections.observableArrayList();

                        for (org.example.model.Module module : groupe.getModules()) {
                            String formateurNom = "Non attribué";
                            if (module.getFormateur() != null) {
                                formateurNom = module.getFormateur().getNom() + " " +
                                        module.getFormateur().getPrenom();
                            }

                            modulesData.add(new ModuleTableData(
                                    module.getNom(),
                                    module.getMatricule(),
                                    formateurNom
                            ));
                        }

                        table.setItems(modulesData);

                        // Ajuster la hauteur en fonction du nombre de lignes
                        int rowHeight = 35; // Hauteur estimée par ligne
                        int headerHeight = 30; // Hauteur de l'en-tête
                        table.setPrefHeight(modulesData.size() * rowHeight + headerHeight + 10);

                        return table;
                    }
                };
            }
        });

        // Colonne Actions
        configureActionsColumn();
    }




    // =====================================================================
    // ============= CHARGEMENT DES DONNÉES DEPUIS BD ======================
    // =====================================================================
    private void loadGroupesFromDB() {
        try {
            int directeurId = SessionManager.getInstance().getUserId();
            List<Groupe> groupes = groupeDAO.findByDirecteurId(directeurId);

            // Pour chaque groupe, charger ses modules via ModuleDAO
            ModuleDAO moduleDAO = new ModuleDAO();
            for (Groupe groupe : groupes) {
                List<org.example.model.Module> modules = moduleDAO.findByGroupeId(groupe.getId());
                groupe.setModules(modules);
            }

            groupeList.setAll(groupes);
            filteredList.setAll(groupeList);
            groupesTable.setItems(filteredList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement des données",
                    "Impossible de charger les groupes: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void loadComboBoxes() {
        niveauComboBox.getItems().setAll("1ér année", "2éme année", "3éme année", "4éme année", "5éme année");
        niveauComboBox.getStyleClass().add("float-text-field");
    }


    // =====================================================================
    // ================ BOUTON : FILTRER ===================================
    // =====================================================================
    @FXML
    private void filtrerGroupes() {
        String matricule = matriculeField.getText().toLowerCase();
        String niveau = niveauComboBox.getValue();

        List<Groupe> filtered = groupeList.stream()
                .filter(f ->
                        (matricule.isEmpty() || f.getMatricule().toLowerCase().contains(matricule)) &&
                                (niveau == null || f.getNiveau().getValeur().equals(niveau))
                )
                .collect(Collectors.toList());

        filteredList.setAll(filtered);

        if (filtered.isEmpty() && (!matricule.isEmpty() || niveau != null )) {
            showAlert("Information", "Aucun résultat",
                    "Aucun groupe ne correspond aux critères de recherche.",
                    Alert.AlertType.INFORMATION);
        }
    }

    // =====================================================================
    // ================ BOUTON : RÉINITIALISER =============================
    // =====================================================================
    @FXML
    private void reinitialiserFiltres() {
        matriculeField.clear();
        niveauComboBox.setValue(null);

        // Réinitialiser les styles des champs
        matriculeField.getStyleClass().removeAll("has-text", "focused");

        filteredList.setAll(groupeList);
    }

    @FXML
    private void rafraichirDonnees() {
        loadGroupesFromDB();
        reinitialiserFiltres();
    }





    private void configureActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<Groupe, Void>, TableCell<Groupe, Void>>() {
            @Override
            public TableCell<Groupe, Void> call(final TableColumn<Groupe, Void> param) {
                return new TableCell<Groupe, Void>() {
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
                            Groupe groupe = getTableView().getItems().get(getIndex());
                            handleEdit(groupe);
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
                            Groupe groupe = getTableView().getItems().get(getIndex());
                            handleDelete(groupe);
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




    // =====================================================================
    // ================ GESTION DES ACTIONS ================================
    // =====================================================================
    @FXML
    private void handleEdit(Groupe groupe) {

    }





    private void handleDelete(Groupe groupe) {

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow(); // 🔑 fenêtre courante

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);                 // ✅ OBLIGATOIRE
        confirm.initModality(Modality.WINDOW_MODAL);

        confirm.setTitle("Supprimer groupe");
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setContentText(
                "Voulez-vous vraiment supprimer "
                        + groupe.getMatricule() + " ?"
        );

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo  = new ButtonType("Non", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        confirm.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {

                groupeDAO.delete(groupe.getId());
                loadGroupesFromDB();

                showAlert(
                        "Succès",
                        "Groupe supprimé",
                        groupe.getMatricule() + " a été supprimé avec succès.",
                        Alert.AlertType.INFORMATION
                );
            }
        });
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
            StageManager.loadScene("/view/directeur/Gestion_Formateurs/gestionFormateurs.fxml", "/styles/gestionFormateurs.css", "gesttion des Formateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEtudiants() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Etudiants/gestionEtudiants.fxml", "/styles/gestionFormateurs.css", "Etufiants");
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
