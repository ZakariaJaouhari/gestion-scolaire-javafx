package org.example.controller.formateur;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.DirecteurDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Formateur;
import org.example.model.Module;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ModulesFormateurController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomFormateurLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private TextField nomField;
    @FXML private TextField matriculeField;

    // TABLE
    @FXML private TableView<Module> modulesTable;
    @FXML private TableColumn<Module, String> nomColumn;
    @FXML private TableColumn<Module, String> matriculeColumn;
    @FXML private TableColumn<Module, String> dateDebutColumn;
    @FXML private TableColumn<Module, String> dateFinColumn;
    @FXML private TableColumn<Module, String> heuresColumn;

    private ModuleDAO moduleDAO;
    private ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private ObservableList<Module> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        moduleDAO = new ModuleDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();

        versionLabel.setText("V 0.1.0");
        Formateur formateur = session.getCurrentFormateur();
        updateUserInfo(formateur);
        // Configurer les colonnes du tableau
        configureTableColumns();

        // Charger les données
        loadModulesFromDB();

        // Ajuster la largeur des colonnes
        ajusterLargeurColonnes();
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
        // Formateur pour les dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Colonne Nom - alignement à gauche
        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom())
        );
        nomColumn.setCellFactory(col -> createStyledTableCell(true));

        // Colonne Matricule - alignement au centre
        matriculeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricule())
        );
        matriculeColumn.setCellFactory(col -> createStyledTableCell(false));

        // Colonne Date Début - alignement au centre
        dateDebutColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateDebut() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getDateDebut().format(dateFormatter)
                );
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        dateDebutColumn.setCellFactory(col -> createStyledTableCell(false));

        // Colonne Date Fin - alignement au centre
        dateFinColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateFin() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getDateFin().format(dateFormatter)
                );
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        dateFinColumn.setCellFactory(col -> createStyledTableCell(false));

        // Colonne Heures - alignement au centre avec "heures"
        heuresColumn.setCellValueFactory(cellData -> {
            String heures = cellData.getValue().getHeuresPratique();
            return new SimpleStringProperty(heures != null ? heures : "0");
        });
        heuresColumn.setCellFactory(col -> new TableCell<Module, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Si c'est un nombre, ajouter "heures"
                    if (item.matches("\\d+")) {
                        setText(item + " heures");
                    } else {
                        setText(item);
                    }
                    setStyle("""
                    -fx-padding: 12px 8px;
                    -fx-font-size: 15px;
                    -fx-font-weight: normal;
                    -fx-text-fill: #000000;
                    -fx-alignment: CENTER-LEFT;
                    -fx-border-color: transparent;
                    -fx-background-color: transparent;
                """);
                }
            }
        });

        // Améliorer l'apparence du tableau
        modulesTable.setStyle("""
            -fx-table-cell-border-color: transparent;
            -fx-background-color: transparent;
        """);

        modulesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // Méthode utilitaire pour créer des cellules stylisées
    private <T> TableCell<Module, T> createStyledTableCell(boolean alignLeft) {
        return new TableCell<Module, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    String alignment = "CENTER-LEFT";
                    setStyle(String.format("""
                    -fx-padding: 12px 8px;
                    -fx-font-size: 15px;
                    -fx-font-weight: normal;
                    -fx-text-fill: #000000;
                    -fx-alignment: %s;
                    -fx-border-color: transparent;
                    -fx-background-color: transparent;
                """, alignment));
                }
            }
        };
    }

    private void ajusterLargeurColonnes() {
        // Ajuster les largeurs pour éviter le décalage
        nomColumn.setPrefWidth(250);
        matriculeColumn.setPrefWidth(125);
        dateDebutColumn.setPrefWidth(125);
        dateFinColumn.setPrefWidth(125);
        heuresColumn.setPrefWidth(125);

        // Centrer le texte des en-têtes
        String styleCenter = "-fx-alignment: center-left;";

        nomColumn.setStyle(styleCenter);
        matriculeColumn.setStyle(styleCenter);
        dateDebutColumn.setStyle(styleCenter);
        dateFinColumn.setStyle(styleCenter);
        heuresColumn.setStyle(styleCenter);
    }

    private void loadModulesFromDB() {
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

            // Récupérer les modules du formateur
            List<Module> modules = moduleDAO.findByFormateurId(formateur.getId());

            moduleList.setAll(modules);
            filteredList.setAll(moduleList);
            modulesTable.setItems(filteredList);

            // Rafraîchir le tableau
            modulesTable.refresh();

            System.out.println("Modules chargés: " + modules.size());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement des données",
                    "Impossible de charger les modules: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    // ... reste des méthodes identiques (filtrerModules, reinitialiserFiltres, etc.)

    @FXML
    private void filtrerModules() {
        String nom = nomField.getText().toLowerCase();
        String matricule = matriculeField.getText().toLowerCase();

        List<Module> filtered = moduleList.stream()
                .filter(m ->
                        (nom.isEmpty() || m.getNom().toLowerCase().contains(nom)) &&
                                (matricule.isEmpty() || m.getMatricule().toLowerCase().contains(matricule))
                )
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        modulesTable.refresh();

        if (filtered.isEmpty() && (!nom.isEmpty() || !matricule.isEmpty())) {
            showAlert("Information", "Aucun résultat",
                    "Aucun module ne correspond aux critères de recherche.",
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void reinitialiserFiltres() {
        nomField.clear();
        matriculeField.clear();
        filteredList.setAll(moduleList);
        modulesTable.refresh();
    }

    @FXML
    private void rafraichirDonnees() {
        loadModulesFromDB();
        reinitialiserFiltres();
    }

    // ... méthodes de navigation identiques

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
}