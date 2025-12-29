package org.example.controller.directeur.gestiongroupes;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.dao.GroupeDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Groupe;
import org.example.model.Module;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Ajouter_Groupe_Controller {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // Champs du formulaire
    @FXML private TextField matriculeField;
    @FXML private ComboBox<String> niveauComboBox;
    @FXML private ListView<Module> modulesListView;

    @FXML private Button ajouterButton;
    @FXML private Button cancelButton;

    private GroupeDAO groupeDAO;
    private ModuleDAO moduleDAO;
    private ObservableList<Module> allModulesList = FXCollections.observableArrayList();

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

        // Initialiser le ComboBox de niveau
        niveauComboBox.getItems().setAll("1ér année", "2éme année", "3éme année", "4éme année", "5éme année");

        // Charger les modules disponibles
        chargerModules();

        // Configurer la ListView pour la sélection multiple
        configurerListView();

        // Initialiser les styles de largeur
        initialiserStylesListView();

        // Configurer les événements
        configureEvents();

        // Configurer les styles des boutons
        setupButtonStyles();
    }

    private void chargerModules() {
        try {
            int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

            // Charger tous les modules du directeur
            List<Module> modules = moduleDAO.findByDirecteurId(directeurId);

            allModulesList.setAll(modules);
            modulesListView.setItems(allModulesList);

            if (modules.isEmpty()) {
                showAlert("Information", "Aucun module disponible",
                        "Aucun module n'est disponible pour l'instant. " +
                                "Veuillez d'abord créer des modules.", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Chargement des modules",
                    "Impossible de charger les modules: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurerListView() {
        // Activer la sélection multiple
        modulesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Style global pour la ListView
        modulesListView.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #1a202c;" +
                        "-fx-border-width: 0 0 1 0;" +
                        "-fx-padding: 8 0;" +
                        "-fx-background-insets: 0;" +
                        // Styles pour la sélection
                        "-fx-selection-bar: #e5e7eb;" +
                        "-fx-selection-bar-non-focused: #e5e7eb;" +
                        "-fx-selection-bar-border: transparent;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );

        modulesListView.setFocusTraversable(false);

        // Personnaliser l'affichage des modules avec style amélioré
        modulesListView.setCellFactory(new Callback<ListView<Module>, ListCell<Module>>() {
            @Override
            public ListCell<Module> call(ListView<Module> param) {
                return new ListCell<Module>() {
                    private final HBox hbox = new HBox(10);
                    private final Label matriculeLabel = new Label();
                    private final Label nomLabel = new Label();
                    private final Label formateurLabel = new Label();

                    {
                        // Configuration initiale des labels
                        hbox.setMaxWidth(820);
                        hbox.setPrefWidth(820);

                        matriculeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
                        nomLabel.setStyle("-fx-text-fill: #6b7280;");
                        formateurLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");

                        hbox.getChildren().addAll(matriculeLabel, nomLabel, formateurLabel);

                        // Style de base pour la cellule
                        setStyle("-fx-background-color: transparent; -fx-padding: 8 12;");
                        setGraphic(hbox);
                        setPrefWidth(800);
                    }

                    @Override
                    protected void updateItem(Module module, boolean empty) {
                        super.updateItem(module, empty);

                        if (empty || module == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("-fx-background-color: transparent; -fx-padding: 8 12;");
                        } else {
                            // Mettre à jour les textes des labels
                            matriculeLabel.setText(module.getMatricule());
                            nomLabel.setText("- " + module.getNom());

                            // Afficher ou cacher le formateur
                            if (module.getFormateur() != null) {
                                formateurLabel.setText("(" +
                                        module.getFormateur().getNom() + " " +
                                        module.getFormateur().getPrenom() + ")");
                                formateurLabel.setVisible(true);
                                formateurLabel.setManaged(true);
                            } else {
                                formateurLabel.setText("");
                                formateurLabel.setVisible(false);
                                formateurLabel.setManaged(false);
                            }

                            setGraphic(hbox);

                            // Vérifier si cette cellule est sélectionnée
                            if (isSelected()) {
                                setStyle(
                                        "-fx-background-color: #e5e7eb;" +
                                                "-fx-background-radius: 4;" +
                                                "-fx-padding: 8 12;" +
                                                "-fx-border-color: #d1d5db;" +
                                                "-fx-border-width: 1;" +
                                                "-fx-border-radius: 4;"
                                );
                            } else {
                                setStyle("-fx-background-color: transparent; -fx-padding: 8 12;");
                            }
                        }
                    }

                    @Override
                    public void updateSelected(boolean selected) {
                        super.updateSelected(selected);

                        // Mettre à jour le style lorsque la sélection change
                        if (selected) {
                            setStyle(
                                    "-fx-background-color: #e5e7eb;" +
                                            "-fx-background-radius: 4;" +
                                            "-fx-padding: 8 12;" +
                                            "-fx-border-color: #d1d5db;" +
                                            "-fx-border-width: 1;" +
                                            "-fx-border-radius: 4;"
                            );
                        } else {
                            setStyle("-fx-background-color: transparent; -fx-padding: 8 12;");
                        }
                    }
                };
            }
        });

        // Ajouter un écouteur pour mettre à jour les styles lors de la sélection
        modulesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Rafraîchir toutes les cellules
            modulesListView.refresh();
        });
    }
    private void initialiserStylesListView() {
        // Appliquer le style à la ListView après son initialisation
        Platform.runLater(() -> {
            // Forcer la largeur à 800px
            modulesListView.setPrefWidth(820);
            modulesListView.setMaxWidth(820);

            // Appliquer les styles CSS
            modulesListView.setStyle(
                    modulesListView.getStyle() +
                            " -fx-pref-width: 820px;" +
                            " -fx-max-width: 820px;" +
                            " -fx-min-width: 820px;"
            );
        });
    }



    private void configureEvents() {
        // Action des boutons
        ajouterButton.setOnAction(e -> ajouterGroupe());
        cancelButton.setOnAction(e -> returnToGestionGroupes());
    }

    private void setupButtonStyles() {
        // Style pour le bouton Annuler
        cancelButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1a202c;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-cursor: hand;"
        );

        // Style initial pour le bouton Ajouter
        ajouterButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #10B981;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
    }

    private void ajouterGroupe() {
        try {
            // Validation des champs
            if (!validateFields()) {
                return;
            }

            // Récupération des valeurs
            String matricule = matriculeField.getText().trim();
            String niveau = niveauComboBox.getValue();

            // Récupérer les modules sélectionnés
            List<Module> modulesSelectionnes = new ArrayList<>(
                    modulesListView.getSelectionModel().getSelectedItems()
            );

            // Vérifier si le matricule existe déjà
            if (groupeDAO.matriculeExists(matricule)) {
                showAlert("Erreur", "Matricule déjà utilisé",
                        "Ce matricule de groupe existe déjà.", Alert.AlertType.ERROR);
                return;
            }

            // Récupérer l'id du directeur connecté
            int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

            // Créer l'objet Groupe
            Groupe groupe = new Groupe(
                    matricule,
                    Groupe.Niveau.fromString(niveau),
                    directeurId
            );

            // Sauvegarder le groupe dans la base de données
            Long groupeId = groupeDAO.create(groupe);

            if (groupeId != null) {
                // Associer les modules sélectionnés au groupe
                boolean associationsReussies = true;
                for (Module module : modulesSelectionnes) {
                    if (!moduleDAO.addModuleToGroupe(groupeId.intValue(), module.getId())) {
                        associationsReussies = false;
                    }
                }

                if (associationsReussies || modulesSelectionnes.isEmpty()) {
                    StringBuilder message = new StringBuilder();
                    message.append("Le groupe a été ajouté avec succès !\n\n");
                    message.append("Matricule: ").append(matricule).append("\n");
                    message.append("Niveau: ").append(niveau).append("\n");

                    if (!modulesSelectionnes.isEmpty()) {
                        message.append("\nModules associés:\n");
                        for (Module module : modulesSelectionnes) {
                            message.append("- ").append(module.getMatricule())
                                    .append(" - ").append(module.getNom()).append("\n");
                        }
                    } else {
                        message.append("\nAucun module associé.");
                    }

                    showAlert("Succès", "Groupe ajouté",
                            message.toString(), Alert.AlertType.INFORMATION);

                    // Retourner à la page GestionGroupes
                    returnToGestionGroupes();
                } else {
                    // Supprimer le groupe si les associations ont échoué
                    groupeDAO.delete(groupeId.intValue());
                    showAlert("Erreur", "Échec des associations",
                            "Le groupe a été créé mais les associations avec les modules ont échoué.",
                            Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Erreur", "Échec de l'ajout",
                        "Une erreur est survenue lors de l'ajout du groupe.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Exception",
                    "Erreur inattendue: " + ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (matriculeField.getText().trim().isEmpty()) {
            errors.append("- Le matricule est obligatoire\n");
        }
        if (niveauComboBox.getValue() == null) {
            errors.append("- Le niveau est obligatoire\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Champs manquants",
                    errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void clearForm() {
        // Réinitialiser les champs texte
        matriculeField.clear();

        // Réinitialiser les ComboBox
        niveauComboBox.getSelectionModel().clearSelection();

        // Désélectionner tous les modules
        modulesListView.getSelectionModel().clearSelection();

        // Remettre le focus sur le premier champ
        matriculeField.requestFocus();
    }

    // Méthode pour retourner à la page gestionGroupes
    private void returnToGestionGroupes() {
        try {
            StageManager.loadScene("/view/directeur/Gestion_Groupes/gestionGroupes.fxml",
                    "/styles/gestionFormateurs.css", "Groupes");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la gestion des groupes.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onHoverButton() {
        ajouterButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #10B981;" +
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 24;" +
                        "-fx-border-color: #10B981;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-translate-y: -1;" +
                        "-fx-scale-x: 1.1;" +
                        "-fx-scale-y: 1.1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
    }

    @FXML
    private void onExitButton() {
        ajouterButton.setStyle(
                "-fx-font-family: 'sans-serif';" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #10B981;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
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


    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) matriculeField.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.showAndWait();
    }
}