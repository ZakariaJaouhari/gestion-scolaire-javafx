package org.example.controller.etudiant;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.DirecteurDAO;
import org.example.dao.EtudiantDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Directeur;
import org.example.model.Etudiant;
import org.example.model.Module;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModulesEtudiantController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomEtudiantLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private TextField nomModuleField;
    @FXML private TextField matriculeModuleField;
    @FXML private ComboBox<String> formateurComboBox;

    // TABLE
    @FXML private TableView<Module> modulesTable;
    @FXML private TableColumn<Module, String> nomColumn;
    @FXML private TableColumn<Module, String> matriculeColumn;
    @FXML private TableColumn<Module, String> dateDebutColumn;
    @FXML private TableColumn<Module, String> dateFinColumn;
    @FXML private TableColumn<Module, String> heuresPresentielColumn;
    @FXML private TableColumn<Module, String> formateurColumn;

    private ModuleDAO moduleDAO;
    private EtudiantDAO etudiantDAO;
    private DirecteurDAO directeurDAO;
    private ObservableList<Module> moduleList = FXCollections.observableArrayList();
    private ObservableList<Module> filteredList = FXCollections.observableArrayList();
    private Etudiant etudiantConnecte;

    @FXML
    public void initialize() {
        moduleDAO = new ModuleDAO();
        etudiantDAO = new EtudiantDAO();
        directeurDAO = new DirecteurDAO();

        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            // Récupérer l'étudiant connecté
            etudiantConnecte = session.getCurrentEtudiant();
            if (etudiantConnecte != null) {
                nomEtudiantLabel.setText(etudiantConnecte.getNom() + " " + etudiantConnecte.getPrenom());
                if (etudiantConnecte.getDirecteurId() > 0) {
                    DirecteurDAO DirecteurDAO = new DirecteurDAO();
                    String nomEcole = DirecteurDAO.getNomEcoleByDirecteurId(etudiantConnecte.getDirecteurId());
                    nomEcoleLabel.setText(nomEcole);
                } else {
                    nomEcoleLabel.setText("École non spécifiée");
                }
            }
        }
        versionLabel.setText("V 0.1.0");

        // Configurer les colonnes du tableau
        configureTableColumns();


        // Charger les modules de l'étudiant
        loadModulesFromDB();

        // Initialiser les ComboBox
        loadComboBoxes();
    }

    private void configureTableColumns() {
        // Formateur pour les dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Colonne Nom
        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom())
        );
        nomColumn.setCellFactory(col -> createStyledTableCell());

        // Colonne Matricule
        matriculeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricule())
        );
        matriculeColumn.setCellFactory(col -> createStyledTableCell());

        // Colonne Date Début
        dateDebutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateDebut().format(dateFormatter)
                )
        );
        dateDebutColumn.setCellFactory(col -> createStyledTableCell());

        // Colonne Date Fin
        dateFinColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateFin().format(dateFormatter)
                )
        );
        dateFinColumn.setCellFactory(col -> createStyledTableCell());

        // Colonne Heures Présentiel
        heuresPresentielColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHeuresPratique() + " heures")
        );
        heuresPresentielColumn.setCellFactory(col -> createStyledTableCell());

        // Colonne Formateur
        formateurColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFormateur() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getFormateur().getNom() + " " +
                                cellData.getValue().getFormateur().getPrenom()
                );
            } else {
                return new SimpleStringProperty("Non assigné");
            }
        });
        formateurColumn.setCellFactory(col -> createStyledTableCell());

        // Ajuster la largeur des colonnes
        nomColumn.setPrefWidth(350);
        matriculeColumn.setPrefWidth(200);
        dateDebutColumn.setPrefWidth(200);
        dateFinColumn.setPrefWidth(200);
        heuresPresentielColumn.setPrefWidth(200);
        formateurColumn.setPrefWidth(200);
    }


    private TableCell<Module, String> createStyledTableCell() {
        return new TableCell<Module, String>() {
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
                    -fx-font-size: 15px;
                    -fx-font-weight: normal;
                    -fx-text-fill: #000000;
                    -fx-alignment: CENTER_LEFT;
                    -fx-border-color: transparent;
                """);
                }
            }
        };
    }

    private void loadModulesFromDB() {
        try {
            if (etudiantConnecte == null) {
                System.err.println("Erreur: Étudiant non connecté - etudiantConnecte est null");
                return;
            }

            // DEBUG: Afficher toutes les infos de l'étudiant
            System.out.println("=== DEBUG INFO ===");
            System.out.println("Étudiant ID: " + etudiantConnecte.getId());
            System.out.println("Étudiant Nom: " + etudiantConnecte.getNom());
            System.out.println("Étudiant Groupe: " + (etudiantConnecte.getGroupe() != null ?
                    etudiantConnecte.getGroupe().getMatricule() : "null"));
            System.out.println("Étudiant Groupe ID: " + (etudiantConnecte.getGroupe() != null ?
                    etudiantConnecte.getGroupe().getId() : "null"));
            System.out.println("==================");

            // Vérifier si l'étudiant a un groupe
            if (etudiantConnecte.getGroupe() == null) {
                // Vérifier dans la base de données si l'étudiant a un groupe
                Optional<Etudiant> etudiantFromDB = etudiantDAO.findById(etudiantConnecte.getId());
                if (etudiantFromDB.isPresent() && etudiantFromDB.get().getGroupe() != null) {
                    // Mettre à jour l'étudiant connecté
                    etudiantConnecte.setGroupe(etudiantFromDB.get().getGroupe());
                } else {
                    showAlert("Information", "Aucun groupe assigné",
                            "Vous n'êtes pas encore assigné à un groupe. Veuillez contacter l'administration.",
                            Alert.AlertType.INFORMATION);
                    return;
                }
            }

            // Récupérer les modules du groupe de l'étudiant
            int groupeId = etudiantConnecte.getGroupe().getId();
            System.out.println("Recherche des modules pour le groupe ID: " + groupeId);

            List<Module> modules = moduleDAO.findByGroupeId(groupeId);

            System.out.println("Modules trouvés: " + modules.size());
            for (Module m : modules) {
                System.out.println("  - " + m.getNom() + " (" + m.getMatricule() + ")");
            }

            moduleList.setAll(modules);
            filteredList.setAll(moduleList);
            modulesTable.setItems(filteredList);

            // Rafraîchir le tableau
            modulesTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement des données",
                    "Impossible de charger les modules: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void loadComboBoxes() {
        // Charger les noms des formateurs depuis les modules de l'étudiant
        List<String> formateurs = moduleList.stream()
                .filter(module -> module.getFormateur() != null)
                .map(module -> module.getFormateur().getNom() + " " + module.getFormateur().getPrenom())
                .distinct()
                .collect(Collectors.toList());

        formateurComboBox.getItems().setAll(formateurs);
        formateurComboBox.getStyleClass().add("float-text-field");
    }

    // =====================================================================
    // ================ BOUTON : FILTRER ===================================
    // =====================================================================
    @FXML
    private void filtrerModules() {
        String nom = nomModuleField.getText().toLowerCase();
        String matricule = matriculeModuleField.getText().toLowerCase();
        String formateur = formateurComboBox.getValue();

        List<Module> filtered = moduleList.stream()
                .filter(m ->
                        (nom.isEmpty() || m.getNom().toLowerCase().contains(nom)) &&
                                (matricule.isEmpty() || m.getMatricule().toLowerCase().contains(matricule)) &&
                                (formateur == null ||
                                        (m.getFormateur() != null &&
                                                (m.getFormateur().getNom() + " " + m.getFormateur().getPrenom()).equals(formateur)))
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
        nomModuleField.clear();
        matriculeModuleField.clear();
        formateurComboBox.setValue(null);
        filteredList.setAll(moduleList);
    }

    @FXML
    private void rafraichirDonnees() {
        loadModulesFromDB();
        reinitialiserFiltres();
    }

    // Méthodes de navigation
    @FXML
    private void handleHome() {
        try {
            StageManager.loadScene("/view/etudiant/dashboardE.fxml",
                    "/styles/dashboard.css", "Dashboard Étudiant");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotes() {
        try {
            StageManager.loadScene("/view/etudiant/espaceNotes.fxml", "/styles/gestionFormateurs.css", "Notes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEmploiDuTemps() {
        try {
            StageManager.loadScene("/view/etudiant/planningEtudiant.fxml", "/styles/gestionFormateurs.css", "Planning");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleModules(MouseEvent mouseEvent) {
        try {
            StageManager.loadScene("/view/etudiant/modulesEtudiant.fxml",
                    "/styles/gestionFormateurs.css", "Mes Modules");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfil() {
        try {
            StageManager.loadScene("/view/etudiant/profilEtudiant.fxml",
                    "/styles/gestionFormateurs.css", "Mon Profil");
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

        // Ne pas crash si la scène n'est pas encore affichée
        if (nomEcoleLabel.getScene() != null && nomEcoleLabel.getScene().getWindow() != null) {
            Stage stage = (Stage) nomEcoleLabel.getScene().getWindow();
            alert.initOwner(stage);
            alert.initModality(Modality.WINDOW_MODAL);
        }

        alert.showAndWait();
    }

}