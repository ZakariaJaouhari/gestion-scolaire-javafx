package org.example.controller.directeur.gestionetudiants;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.dao.NoteDAO;
import org.example.model.Note;
import org.example.service.BulletinPDFService;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;












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
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import org.example.dao.EtudiantDAO;
import org.example.dao.GroupeDAO;
import org.example.dao.NoteDAO;
import org.example.model.Directeur;
import org.example.model.Etudiant;
import org.example.model.Groupe;
import org.example.dao.GroupeDAO;
import org.example.model.Note;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
                    private final Button downloadButton = new Button(); // Nouveau bouton

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

                        // Bouton télécharger bulletin
                        try {
                            ImageView downloadIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/resultat.png")));
                            downloadIcon.setFitWidth(20);
                            downloadIcon.setFitHeight(20);
                            downloadButton.setGraphic(downloadIcon);
                        } catch (Exception e) {
                            downloadButton.setText("📥");
                        }
                        downloadButton.getStyleClass().add("action-button");
                        downloadButton.setTooltip(new Tooltip("Télécharger le bulletin"));
                        downloadButton.setOnAction(event -> {
                            Etudiant etudiant = getTableView().getItems().get(getIndex());
                            telechargerBulletin(etudiant);
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

                        container.getChildren().addAll(editButton, downloadButton, deleteButton);
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

    private void telechargerBulletin(Etudiant etudiant) {
        // Créer une fenêtre de dialogue pour sélectionner le semestre
        ChoiceDialog<String> semestreDialog = new ChoiceDialog<>("Semestre 1", "Semestre 1", "Semestre 2");
        semestreDialog.setTitle("Sélection du semestre");
        semestreDialog.setHeaderText("Téléchargement du bulletin de " + etudiant.getNomComplet());
        semestreDialog.setContentText("Choisissez le semestre :");

        // Afficher la boîte de dialogue et attendre la réponse
        Optional<String> semestreResult = semestreDialog.showAndWait();
        if (!semestreResult.isPresent()) {
            return; // L'utilisateur a annulé
        }

        String semestre = semestreResult.get();

        // Créer une boîte de dialogue pour l'année scolaire
        TextInputDialog anneeDialog = new TextInputDialog(String.valueOf(LocalDate.now().getYear()));
        anneeDialog.setTitle("Année scolaire");
        anneeDialog.setHeaderText("Entrez l'année scolaire");
        anneeDialog.setContentText("Année de début (ex: 2024 pour 2024-2025) :");

        Optional<String> anneeResult = anneeDialog.showAndWait();
        if (!anneeResult.isPresent()) {
            return;
        }

        try {
            int anneeScolaire = Integer.parseInt(anneeResult.get());

            // Vérifier si l'étudiant a un groupe
            if (etudiant.getGroupeId() <= 0 && etudiant.getGroupe() == null) {
                showAlert("Erreur", "Étudiant sans groupe",
                        "Cet étudiant n'est pas assigné à un groupe.", Alert.AlertType.WARNING);
                return;
            }

            // CORRECTION: Récupérer l'ID du directeur connecté
            Directeur directeur = SessionManager.getInstance().getCurrentDirecteur();
            int directeurId = directeur.getId();

            // Créer une tâche asynchrone pour la génération du PDF
            Task<File> generationTask = new Task<>() {
                @Override
                protected File call() throws Exception {
                    updateMessage("Génération du bulletin en cours...");

                    // CORRECTION: Passer l'ID du directeur au constructeur
                    BulletinPDFService pdfService = new BulletinPDFService(directeurId);
                    return pdfService.generateBulletinPDF(
                            etudiant.getId(),
                            anneeScolaire,
                            semestreResult.get()
                    );
                }
            };

            // Créer une boîte de dialogue de progression
            Dialog<ButtonType> progressDialog = new Dialog<>();
            progressDialog.setTitle("Génération du bulletin");
            progressDialog.setHeaderText("Veuillez patienter...");

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setProgress(-1);

            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);
            content.setPadding(new Insets(20));
            content.getChildren().addAll(progressIndicator, new Label("Génération du bulletin en cours..."));

            progressDialog.getDialogPane().setContent(content);
            progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            // Configurer la fermeture de la boîte de dialogue
            progressDialog.setOnCloseRequest(event -> {
                if (generationTask.isRunning()) {
                    generationTask.cancel();
                }
            });

            // Gérer la réussite de la tâche
            generationTask.setOnSucceeded(event -> {
                progressDialog.close();

                try {
                    File pdfFile = generationTask.getValue();

                    // Demander où enregistrer le fichier
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Enregistrer le bulletin");
                    fileChooser.setInitialFileName(String.format("Bulletin_%s_%s_%s_%d.pdf",
                            etudiant.getNom(),
                            etudiant.getPrenom(),
                            semestre.replace(" ", "_"),
                            System.currentTimeMillis()));
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

                    File savedFile = fileChooser.showSaveDialog(null);
                    if (savedFile != null) {
                        // Copier le fichier généré vers l'emplacement choisi
                        Files.copy(pdfFile.toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        // Demander si l'utilisateur veut ouvrir le fichier
                        Alert openAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        openAlert.setTitle("Bulletin généré");
                        openAlert.setHeaderText("Le bulletin a été généré avec succès !");
                        openAlert.setContentText("Voulez-vous ouvrir le fichier ?");

                        if (openAlert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(savedFile);
                            }
                        }

                        // Supprimer le fichier temporaire
                        pdfFile.delete();

                        showAlert("Succès", "Téléchargement réussi",
                                "Le bulletin a été enregistré avec succès.", Alert.AlertType.INFORMATION);
                    } else {
                        // L'utilisateur a annulé l'enregistrement
                        pdfFile.delete();
                    }

                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de l'enregistrement",
                            "Impossible d'enregistrer le fichier : " + e.getMessage(),
                            Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            });

            // Gérer l'échec de la tâche
            generationTask.setOnFailed(event -> {
                progressDialog.close();
                Throwable exception = generationTask.getException();
                String errorMessage = "Impossible de générer le bulletin.";

                if (exception != null) {
                    if (exception.getMessage() != null && exception.getMessage().contains("Étudiant non trouvé")) {
                        errorMessage = "Étudiant non trouvé dans la base de données.";
                    } else if (exception.getMessage() != null && exception.getMessage().contains("Aucune note")) {
                        errorMessage = "Aucune note disponible pour cet étudiant pour le semestre sélectionné.";
                    } else {
                        errorMessage = "Erreur : " + exception.getMessage();
                    }
                }

                showAlert("Erreur", "Génération échouée", errorMessage, Alert.AlertType.ERROR);

                if (exception != null) {
                    exception.printStackTrace();
                }
            });

            // Démarrer la tâche et afficher la boîte de dialogue
            new Thread(generationTask).start();
            progressDialog.showAndWait();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Année invalide",
                    "Veuillez entrer une année valide (ex: 2024).", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur inattendue",
                    "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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
    @FXML
    private void handleEdit(Etudiant etudiant) {
        try {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/directeur/Gestion_Etudiants/Modifier_Etudiant.fxml")
            );
            Parent root = loader.load();

            // Récupérer le controller de modification
            Modifier_Etudiant_Controller controller = loader.getController();
            controller.setEtudiantToEdit(etudiant);

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
            stage.setTitle("Modifier Etudiant");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
            StageManager.loadScene("/view/directeur/Gestion_Etudiants/Ajouter_Etudiant.fxml", "/styles/gestionFormateurs.css", "Ajouter Etudiant");
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

