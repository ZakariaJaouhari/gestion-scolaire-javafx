package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.dao.FormateurDAO;
import org.example.model.Formateur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;

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

    @FXML private TableColumn<Formateur, Void> actionsColumn;


    private FormateurDAO formateurDAO;
    private ObservableList<Formateur> formateursList = FXCollections.observableArrayList();


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

        // --- TABLE ----
        nomColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getNom() + " " + cellData.getValue().getPrenom()
                )
        );

        loadFormateursFromDB();
        loadComboBoxes();

        // --- ACTION BUTTONS IN TABLE ---
        actionsColumn.setCellFactory(column -> new TableCell<>() {

            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox container = new HBox(10);

            {
                // BOUTON MODIFIER
                ImageView editIcon = new ImageView("/images/crayon.png");
                editIcon.setFitWidth(20);
                editIcon.setFitHeight(20);
                editBtn.setGraphic(editIcon);
                editBtn.setStyle("-fx-background-color: transparent;");

                editBtn.setOnAction(e -> {
                    Formateur f = getTableView().getItems().get(getIndex());
                    handleEdit(f);
                });

                // BOUTON SUPPRIMER
                ImageView deleteIcon = new ImageView("/images/supprimer.png");
                deleteIcon.setFitWidth(20);
                deleteIcon.setFitHeight(20);
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setStyle("-fx-background-color: transparent;");

                deleteBtn.setOnAction(e -> {
                    Formateur f = getTableView().getItems().get(getIndex());
                    handleDelete(f);
                });

                container.getChildren().addAll(editBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

    }


    private void handleEdit(Formateur f) {
        System.out.println("Modifier : " + f.getNom());
    }


    private void handleDelete(Formateur f) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer formateur");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer " + f.getNom() + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            formateurDAO.delete(f.getId());
            loadFormateursFromDB(); // rafraîchir la table
        }
    }



    // =====================================================================
    // ============= CHARGEMENT DES DONNÉES DEPUIS BD ======================
    // =====================================================================
    private void loadFormateursFromDB() {
        List<Formateur> list = formateurDAO.findByDirecteurId(
                SessionManager.getInstance().getUserId()
        );

        formateursList.setAll(list);
        formateursTable.setItems(formateursList);
    }

    private void loadComboBoxes() {
        sexeComboBox.getItems().setAll("Homme", "Femme");
        situationComboBox.getItems().setAll("Célibataire", "Marié(e)", "Divorcé(e)");
    }

    // =====================================================================
    // ================ BOUTON : FILTRER ===================================
    // =====================================================================
    @FXML
    private void filtrerFormateurs() {
        List<Formateur> filtered = formateursList.stream()
                .filter(f ->
                        (nomField.getText().isEmpty() || f.getNom().toLowerCase().contains(nomField.getText().toLowerCase())) &&
                                (prenomField.getText().isEmpty() || f.getPrenom().toLowerCase().contains(prenomField.getText().toLowerCase())) &&
                                (matriculeField.getText().isEmpty() || f.getMatricule().toLowerCase().contains(matriculeField.getText().toLowerCase())) &&
                                (sexeComboBox.getValue() == null || f.getSexe().getValeur().equals(sexeComboBox.getValue())) &&
                                (situationComboBox.getValue() == null || f.getSituation().getValeur().equals(situationComboBox.getValue()))
                )
                .collect(Collectors.toList());

        formateursTable.setItems(FXCollections.observableArrayList(filtered));
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

        formateursTable.setItems(formateursList);
    }

    // =====================================================================
    // ================ BOUTON : AJOUTER FORMATEUR =========================
    // =====================================================================
    @FXML
    private void ajouterFormateur() {
        try {
            StageManager.loadScene("/view/directeur/ajouterFormateur.fxml",
                    "/styles/ajouterFormateur.css", "Ajouter formateur");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    // =====================================================================
    // ======================= NAVIGATION MENU =============================
    // =====================================================================

    @FXML private void handleHome() {
        try {
            StageManager.loadScene("/view/directeur/dashboard.fxml", "/styles/dashboard.css", "Dashboard");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleFormateurs() {
        try {
            StageManager.loadScene("/view/directeur/gestionFormateurs.fxml", "/styles/gestionFormateurs.css", "Formateurs");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleStagiaires() {
        showAlert("Stagiaires", "Fonctionnalité bientôt disponible.");
    }

    @FXML private void handleGroupes() {
        showAlert("Groupes", "Fonctionnalité bientôt disponible.");
    }

    @FXML private void handleModules() {
        showAlert("Modules", "Fonctionnalité bientôt disponible.");
    }

    @FXML private void handleNotes() {
        showAlert("Notes", "Fonctionnalité bientôt disponible.");
    }

    @FXML private void handleCertificats() {
        showAlert("Planning", "Fonctionnalité bientôt disponible.");
    }

    @FXML private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            StageManager.loadScene("/view/login_register.fxml", "/styles/auth.css", "Connexion");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
