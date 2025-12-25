package org.example.controller.formateur;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.DirecteurDAO;
import org.example.dao.EtudiantDAO;
import org.example.dao.GroupeDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Etudiant;
import org.example.model.Formateur;
import org.example.model.Groupe;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class GroupesFormateurController {
    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomFormateurLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private TextField matriculeField;
    @FXML private ComboBox<String> niveauComboBox;

    private GroupeDAO groupeDAO;
    private EtudiantDAO etudiantDAO;
    private ObservableList<Groupe> groupeList = FXCollections.observableArrayList();
    private ObservableList<Groupe> filteredList = FXCollections.observableArrayList();

    @FXML private TableView<Groupe> groupesTable;
    @FXML private TableColumn<Groupe, String> matriculeColumn;
    @FXML private TableColumn<Groupe, String> niveauColumn;
    @FXML private TableColumn<Groupe, String> nombreEtudiantsColumn;
    @FXML private TableColumn<Groupe, Void> etudiantsColumn;



    // Classe interne pour les données des étudiants dans le tableau
    public static class EtudiantTableData {
        private final SimpleStringProperty nomComplet;
        private final SimpleStringProperty sexe;
        private final SimpleStringProperty dateNaissance;
        private final SimpleStringProperty email;

        public EtudiantTableData(String nomComplet, String sexe, String dateNaissance, String email) {
            this.nomComplet = new SimpleStringProperty(nomComplet);
            this.sexe = new SimpleStringProperty(sexe);
            this.dateNaissance = new SimpleStringProperty(dateNaissance);
            this.email = new SimpleStringProperty(email);
        }

        public String getNomComplet() { return nomComplet.get(); }
        public void setNomComplet(String value) { nomComplet.set(value); }

        public String getSexe() { return sexe.get(); }
        public void setSexe(String value) { sexe.set(value); }

        public String getDateNaissance() { return dateNaissance.get(); }
        public void setDateNaissance(String value) { dateNaissance.set(value); }

        public String getEmail() { return email.get(); }
        public void setEmail(String value) { email.set(value); }
    }

    @FXML
    public void initialize() {
        groupeDAO = new GroupeDAO();
        etudiantDAO = new EtudiantDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();

        versionLabel.setText("V 0.1.0");

        Formateur formateur = session.getCurrentFormateur();
        updateUserInfo(formateur);

        // Initialiser les ComboBox
        loadComboBoxes();

        // Configurer les colonnes du tableau
        configureTableColumns();

        // Charger les données
        loadGroupesFromDB();
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

        // Colonne Nombre d'étudiants
        nombreEtudiantsColumn.setCellValueFactory(cellData -> {
            Groupe groupe = cellData.getValue();
            if (groupe.getEtudiants() != null) {
                return new SimpleStringProperty(String.valueOf(groupe.getEtudiants().size()));
            } else {
                return new SimpleStringProperty("0");
            }
        });
        nombreEtudiantsColumn.setCellFactory(col -> new TableCell<Groupe, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item + " étudiant(s)");
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

        // Colonne Étudiants (imbriquée)
        etudiantsColumn.setCellFactory(column -> new TableCell<Groupe, Void>() {
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

                    // Vérifier si le groupe a des étudiants
                    if (groupe.getEtudiants() == null || groupe.getEtudiants().isEmpty()) {
                        Label noEtudiantsLabel = new Label("Aucun étudiant dans ce groupe");
                        noEtudiantsLabel.setStyle("""
                        -fx-text-fill: #9ca3af;
                        -fx-font-style: italic;
                        -fx-font-size: 14px;
                        -fx-padding: 15px 8px;
                    """);
                        container.getChildren().add(noEtudiantsLabel);
                    } else {
                        // Créer le tableau imbriqué pour les étudiants
                        TableView<EtudiantTableData> etudiantsTable = createEtudiantsTableView(groupe);
                        container.getChildren().add(etudiantsTable);
                    }

                    setGraphic(container);
                }
            }

            private TableView<EtudiantTableData> createEtudiantsTableView(Groupe groupe) {
                TableView<EtudiantTableData> table = new TableView<>();

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
                table.getStyleClass().add("etudiants-subtable");

                // Formateur pour la date
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // Colonne Nom Complet
                TableColumn<EtudiantTableData, String> nomCompletCol = new TableColumn<>("Nom Complet");
                nomCompletCol.setPrefWidth(250);
                nomCompletCol.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getNomComplet()));
                nomCompletCol.setCellFactory(col -> new TableCell<EtudiantTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setStyle("""
                            -fx-padding: 12px 8px;
                            -fx-font-size: 15px;
                            -fx-font-weight: normal;
                            -fx-text-fill: #000000;
                            -fx-background-color: transparent;
                            -fx-border-color: transparent;
                            -fx-alignment: CENTER_LEFT;
                        """);
                        }
                    }
                });

                // Colonne Sexe
                TableColumn<EtudiantTableData, String> sexeCol = new TableColumn<>("Sexe");
                sexeCol.setPrefWidth(80);
                sexeCol.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getSexe()));
                sexeCol.setCellFactory(col -> new TableCell<EtudiantTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
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

                // Colonne Date de Naissance
                TableColumn<EtudiantTableData, String> dateNaissanceCol = new TableColumn<>("Date Naissance");
                dateNaissanceCol.setPrefWidth(170);
                dateNaissanceCol.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDateNaissance()));
                dateNaissanceCol.setCellFactory(col -> new TableCell<EtudiantTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
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

                // Colonne Email
                TableColumn<EtudiantTableData, String> emailCol = new TableColumn<>("Email");
                emailCol.setPrefWidth(350);
                emailCol.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getEmail()));
                emailCol.setCellFactory(col -> new TableCell<EtudiantTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setStyle("""
                            -fx-padding: 12px 8px;
                            -fx-font-size: 15px;
                            -fx-font-weight: normal;
                            -fx-text-fill: #000000;
                            -fx-background-color: transparent;
                            -fx-border-color: transparent;
                            -fx-alignment: CENTER_LEFT;
                        """);
                        }
                    }
                });

                // Ajouter les colonnes
                table.getColumns().addAll(nomCompletCol, sexeCol, dateNaissanceCol, emailCol);

                // Supprimer le style d'en-tête du tableau imbriqué pour plus de propreté
                for (TableColumn<EtudiantTableData, ?> column : table.getColumns()) {
                    column.setStyle("-fx-font-size: 15px; -fx-font-weight: normal;");
                    column.setReorderable(false);
                    column.setResizable(true);
                }

                // Charger les données des étudiants
                ObservableList<EtudiantTableData> etudiantsData = FXCollections.observableArrayList();

                if (groupe.getEtudiants() != null) {
                    for (Etudiant etudiant : groupe.getEtudiants()) {  // Maintenant getEtudiants() retourne List<Etudiant>
                        String nomComplet = etudiant.getNom() + " " + etudiant.getPrenom();
                        String sexe = etudiant.getSexe() != null ? String.valueOf(etudiant.getSexe()) : "Non spécifié";
                        String dateNaissance = etudiant.getDateNaissance() != null ?
                                etudiant.getDateNaissance().format(dateFormatter) : "Non spécifiée";
                        String email = etudiant.getEmail() != null ? etudiant.getEmail() : "Non spécifié";

                        etudiantsData.add(new EtudiantTableData(
                                nomComplet,
                                sexe,
                                dateNaissance,
                                email
                        ));
                    }
                }

                table.setItems(etudiantsData);

                // Ajuster la hauteur pour afficher toutes les lignes
                int rowHeight = 40;
                int headerHeight = 35;
                table.setPrefHeight(etudiantsData.size() * rowHeight + headerHeight);

                // Pas de sélection de ligne
                table.setSelectionModel(null);

                return table;
            }
        });

        groupesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // =====================================================================
    // ============= CHARGEMENT DES DONNÉES DEPUIS BD ======================
    // =====================================================================
    private void loadGroupesFromDB() {
        try {
            // Récupérer le formateur connecté
            SessionManager session = SessionManager.getInstance();
            Formateur formateur = session.getCurrentFormateur();

            if (formateur == null) {
                showAlert("Erreur", "Non connecté",
                        "Vous devez être connecté en tant que formateur.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Récupérer les groupes du formateur (via les modules)
            List<Groupe> groupes = groupeDAO.findByFormateurId(formateur.getId());

            // Pour chaque groupe, charger les étudiants
            for (Groupe groupe : groupes) {
                List<Etudiant> etudiants = etudiantDAO.findByGroupeId(groupe.getId());
                groupe.setEtudiants(etudiants);

                // DEBUG
                System.out.println("Groupe: " + groupe.getMatricule() +
                        " | Étudiants: " + (etudiants != null ? etudiants.size() : 0));
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
                .filter(g ->
                        (matricule.isEmpty() || g.getMatricule().toLowerCase().contains(matricule)) &&
                                (niveau == null || g.getNiveau().getValeur().equals(niveau))
                )
                .collect(Collectors.toList());

        filteredList.setAll(filtered);

        if (filtered.isEmpty() && (!matricule.isEmpty() || niveau != null)) {
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
        filteredList.setAll(groupeList);
    }

    @FXML
    private void rafraichirDonnees() {
        loadGroupesFromDB();
        reinitialiserFiltres();
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

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }
}