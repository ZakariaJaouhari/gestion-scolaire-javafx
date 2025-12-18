package org.example.controller;

import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.dao.GroupeDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Formateur;
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
                new SimpleStringProperty(cellData.getValue().getMatricule())
        );
        matriculeColumn.setCellFactory(col -> new TableCell<Groupe, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    // Style comme sur la capture d'écran
                    setStyle("""
                    -fx-padding: 15px 8px;
                    -fx-font-size: 16px;
                    -fx-font-weight: normal;
                    -fx-text-fill: #000000;
                    -fx-alignment: CENTER_LEFT;
                    -fx-border-color: transparent;
                """);
                }
            }
        });

        // Colonne Niveau
        niveauColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNiveau().getValeur())
        );
        niveauColumn.setCellFactory(col -> new TableCell<Groupe, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    // Style comme sur la capture d'écran
                    setStyle("""
                    -fx-padding: 15px 8px;
                    -fx-font-size: 16px;
                    -fx-font-weight: normal;
                    -fx-text-fill: #000000;
                    -fx-alignment: CENTER_LEFT;
                    -fx-border-color: transparent;
                """);
                }
            }
        });

        // Colonne Modules (imbriquée)
        modulesColumn.setCellFactory(column -> new TableCell<Groupe, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                // Nettoyage
                setGraphic(null);
                setText(null);

                if (!empty && getTableRow() != null && getTableRow().getItem() != null) {
                    Groupe groupe = getTableRow().getItem();

                    VBox container = new VBox(0);
                    container.setStyle("-fx-padding: 0px;");

                    // Vérifier si le groupe a des modules
                    if (groupe.getModules() == null || groupe.getModules().isEmpty()) {
                        Label noModulesLabel = new Label("Aucun module associé");
                        noModulesLabel.setStyle("""
                        -fx-text-fill: #9ca3af;
                        -fx-font-style: italic;
                        -fx-font-size: 14px;
                        -fx-padding: 15px 8px;
                    """);
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

                // Style minimal pour le tableau imbriqué
                table.setStyle("""
                -fx-background-color: transparent;
                -fx-border-color: transparent;
                -fx-padding: 0;
                -fx-table-cell-border-color: transparent;
                -fx-table-header-border-color: transparent;
            """);

                // Enlever toutes les classes de style pour éviter les conflits
                table.getStyleClass().clear();
                table.getStyleClass().add("modules-subtable");
                modulesColumn.getStyleClass().add("modules-column");

                // Colonne Nom module
                TableColumn<ModuleTableData, String> nomModuleCol = new TableColumn<>("Nom module");
                nomModuleCol.setPrefWidth(250);
                nomModuleCol.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getNomModule()));
                nomModuleCol.setCellFactory(col -> new TableCell<ModuleTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            // Style comme sur la capture d'écran - texte noir simple
                            setStyle("""
                            -fx-padding: 12px 8px;
                            -fx-font-size: 15px;
                            -fx-font-weight: normal;
                            -fx-text-fill: #000000;
                            -fx-background-color: transparent;
                            -fx-border-color: transparent;
                            -fx-alignment: CENTER;
                        """);
                        }
                    }
                });

                // Colonne Matricule module
                TableColumn<ModuleTableData, String> matriculeModuleCol = new TableColumn<>("Matricule module");
                matriculeModuleCol.setPrefWidth(150);
                matriculeModuleCol.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getMatriculeModule()));
                matriculeModuleCol.setCellFactory(col -> new TableCell<ModuleTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            // Style comme sur la capture d'écran
                            setStyle("""
                            -fx-padding: 12px 8px;
                            -fx-font-size: 15px;
                            -fx-font-weight: normal;
                            -fx-text-fill: #000000;
                            -fx-background-color: transparent;
                            -fx-border-color: transparent;
                            -fx-alignment: CENTER;
                        """);
                        }
                    }
                });

                // Colonne Formateur
                TableColumn<ModuleTableData, String> formateurCol = new TableColumn<>("Formateur");
                formateurCol.setPrefWidth(200);
                formateurCol.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getFormateurNom()));
                formateurCol.setCellFactory(col -> new TableCell<ModuleTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            // Style comme sur la capture d'écran
                            setStyle("""
                            -fx-padding: 12px 8px;
                            -fx-font-size: 15px;
                            -fx-font-weight: normal;
                            -fx-text-fill: #000000;
                            -fx-background-color: transparent;
                            -fx-border-color: transparent;
                            -fx-alignment: CENTER;
                        """);
                        }
                    }
                });

                // Ajouter les colonnes
                table.getColumns().addAll(nomModuleCol, matriculeModuleCol, formateurCol);

                // Supprimer le style d'en-tête du tableau imbriqué pour plus de propreté
                for (TableColumn<ModuleTableData, ?> column : table.getColumns()) {
                    column.setStyle("-fx-font-size: 15px; -fx-font-weight: normal;");
                    column.setReorderable(false);
                    column.setResizable(true);
                }

                // Charger les données
                ObservableList<ModuleTableData> modulesData = FXCollections.observableArrayList();

                if (groupe.getModules() != null) {
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
                }

                table.setItems(modulesData);

                // Ajuster la hauteur pour afficher toutes les lignes
                int rowHeight = 40; // Hauteur de ligne plus grande comme sur la capture
                int headerHeight = 35;
                table.setPrefHeight(modulesData.size() * rowHeight + headerHeight);

                // Pas de sélection de ligne
                table.setSelectionModel(null);

                return table;
            }
        });

        // Colonne Actions
        configureActionsColumn();
        groupesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Méthode pour appliquer le style "other-column" comme dans le tableau des formateurs
    private <T> void applyOtherColumnStyle(TableColumn<Groupe, T> column) {
        column.setCellFactory(col -> new TableCell<Groupe, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-padding: 12px 8px; -fx-font-weight: normal; -fx-text-fill: #374151;");
                    getStyleClass().add("other-column");
                }
            }
        });
    }




    // =====================================================================
    // ============= CHARGEMENT DES DONNÉES DEPUIS BD ======================
    // =====================================================================
    private void loadGroupesFromDB() {
        try {
            int directeurId = SessionManager.getInstance().getUserId();
            List<Groupe> groupes = groupeDAO.findByDirecteurId(directeurId);

            ModuleDAO moduleDAO = new ModuleDAO();
            for (Groupe groupe : groupes) {
                List<org.example.model.Module> modules = moduleDAO.findByGroupeId(groupe.getId());
                groupe.setModules(modules);

                // DEBUG
                System.out.println("Groupe: " + groupe.getMatricule() +
                        " | Modules: " + (modules != null ? modules.size() : 0));
            }

            groupeList.setAll(groupes);
            filteredList.setAll(groupeList);
            groupesTable.setItems(filteredList);

            // Rafraîchir le tableau
            groupesTable.refresh();

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
                            editIcon.setFitWidth(18);
                            editIcon.setFitHeight(18);
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
                            deleteIcon.setFitWidth(18);
                            deleteIcon.setFitHeight(18);
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
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/directeur/Gestion_Groupes/Modifier_Groupe.fxml")
            );
            Parent root = loader.load();

            // Récupérer le controller de modification
            Modifier_Groupe_Controller controller = loader.getController();
            controller.initData(groupe);

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
            stage.setTitle("Modifier Groupe");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private void ajouterGroupe() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Groupes/Ajouter_Groupe.fxml", "/styles/gestionFormateurs.css", "Ajouter Groupe");
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
