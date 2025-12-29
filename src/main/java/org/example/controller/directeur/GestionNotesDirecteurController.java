package org.example.controller.directeur;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.*;
import org.example.model.*;
import org.example.model.Module;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class GestionNotesDirecteurController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private ComboBox<String> anneeScolaireCombo;
    @FXML private ComboBox<String> semestreCombo;
    @FXML private ComboBox<String> typeEvaluationCombo;
    @FXML private ComboBox<Groupe> groupeCombo;
    @FXML private ComboBox<Module> moduleCombo;
    @FXML private Button filtrerButton;
    @FXML private Button sauvegarderButton;

    // TABLEAU DES NOTES
    @FXML private TableView<EtudiantNote> notesTable;
    @FXML private TableColumn<EtudiantNote, String> matriculeCol;
    @FXML private TableColumn<EtudiantNote, String> nomCol;
    @FXML private TableColumn<EtudiantNote, String> prenomCol;
    @FXML private TableColumn<EtudiantNote, Double> noteCol;
    @FXML private TableColumn<EtudiantNote, CheckBox> absentCol;

    // Données
    private ObservableList<EtudiantNote> etudiantsList = FXCollections.observableArrayList();
    private NoteDAO noteDAO = new NoteDAO();
    private EtudiantDAO etudiantDAO = new EtudiantDAO();
    private ModuleDAO moduleDAO = new ModuleDAO();
    private GroupeDAO groupeDAO = new GroupeDAO();
    private Directeur directeurConnecte;

    // Classe pour représenter un étudiant avec sa note (exactement comme celle du formateur)
    public static class EtudiantNote {
        private Etudiant etudiant;
        private javafx.beans.property.ObjectProperty<Double> note;
        private CheckBox absentCheckBox;
        private Integer noteExistanteId;
        private Runnable refreshCallback;

        public EtudiantNote(Etudiant etudiant) {
            this.etudiant = etudiant;
            this.note = new javafx.beans.property.SimpleObjectProperty<>(null);
            this.absentCheckBox = new CheckBox();
            this.noteExistanteId = null;
            this.refreshCallback = null;

            // Écouteur qui rafraîchit automatiquement le tableau quand la note change
            this.note.addListener((obs, oldVal, newVal) -> {
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            // Configurez l'écouteur du CheckBox
            this.absentCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    setNote(0.0);
                    this.absentCheckBox.setText("Absent");
                } else {
                    this.absentCheckBox.setText("");
                }
                // Rafraîchir l'affichage si un callback est défini
                if (this.refreshCallback != null) {
                    this.refreshCallback.run();
                }
            });
        }

        // Getters et Setters
        public Etudiant getEtudiant() { return etudiant; }

        public Double getNote() { return note.get(); }

        public void setNote(Double note) {
            this.note.set(note);
            if (note != null && note == 0.0 && this.absentCheckBox.isSelected()) {
                // Si note = 0 et absent est coché, on garde
            } else if (note != null && note > 0) {
                this.absentCheckBox.setSelected(false);
                this.absentCheckBox.setText("");
            }
            refreshNote();
        }

        public void refreshNote() {
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        }

        public javafx.beans.property.ObjectProperty<Double> noteProperty() {
            return note;
        }

        public CheckBox getAbsentCheckBox() { return absentCheckBox; }

        public boolean isAbsent() { return absentCheckBox.isSelected(); }

        public void setAbsent(boolean absent) {
            this.absentCheckBox.setSelected(absent);
            if (absent) {
                setNote(0.0);
                this.absentCheckBox.setText("Absent");
            } else {
                this.absentCheckBox.setText("");
            }
        }

        public Integer getNoteExistanteId() { return noteExistanteId; }

        public void setNoteExistanteId(Integer noteExistanteId) {
            this.noteExistanteId = noteExistanteId;
        }

        public void setRefreshCallback(Runnable callback) {
            this.refreshCallback = callback;
        }

        public String getMatricule() { return etudiant.getCin(); }
        public String getNom() { return etudiant.getNom(); }
        public String getPrenom() { return etudiant.getPrenom(); }
    }

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            directeurConnecte = session.getCurrentDirecteur();
            if (directeurConnecte != null) {
                nomDirecteurLabel.setText(directeurConnecte.getNomDirecteur());
            }
        }
        versionLabel.setText("V 1.0.0");

        // Écouteur pour mettre à jour les modules quand le groupe change
        groupeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chargerModulesPourGroupe(newVal);
            }
        });

        // Configurer le tableau
        configurerTableau();

        // Initialiser les filtres
        initialiserFiltres();

        // Charger les données initiales
        chargerGroupes();

        // Écouteur pour rafraîchir automatiquement le tableau
        etudiantsList.addListener((javafx.collections.ListChangeListener<EtudiantNote>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasUpdated() || change.wasRemoved()) {
                    notesTable.refresh();
                }
            }
        });
    }

    private void initialiserFiltres() {
        // Année scolaire
        int anneeCourante = LocalDate.now().getYear();
        anneeScolaireCombo.getItems().addAll(
                anneeCourante + "-" + (anneeCourante + 1)
        );
        anneeScolaireCombo.setValue(anneeCourante + "-" + (anneeCourante + 1));

        // Semestre
        semestreCombo.getItems().addAll("Semestre 1", "Semestre 2");
        semestreCombo.setValue("Semestre 1");

        // Type d'évaluation
        typeEvaluationCombo.getItems().addAll(
                "Contrôle continu",
                "Examen"
        );
        typeEvaluationCombo.setValue("Contrôle continu");

        // Événements
        filtrerButton.setOnAction(e -> chargerEtudiants());
        sauvegarderButton.setOnAction(e -> sauvegarderNotes());

        // Désactiver sauvegarder au début
        sauvegarderButton.setDisable(true);
    }

    private void chargerModulesPourGroupe(Groupe groupe) {
        if (groupe != null) {
            List<Module> modules = moduleDAO.findByGroupeId(groupe.getId());
            moduleCombo.getItems().clear();
            moduleCombo.getItems().addAll(modules);

            // Récupérer tous les formateurs
            FormateurDAO formateurDAO = new FormateurDAO();
            Map<Integer, String> formateursMap = new HashMap<>();
            for (Module module : modules) {
                Formateur formateur = formateurDAO.findById(module.getFormateurId());
                if (formateur != null) {
                    formateursMap.put(module.getId(), formateur.getNomComplet());
                }
            }

            // Configurer l'affichage avec formateur
            moduleCombo.setCellFactory(param -> new ListCell<Module>() {
                @Override
                protected void updateItem(Module module, boolean empty) {
                    super.updateItem(module, empty);
                    if (empty || module == null) {
                        setText(null);
                    } else {
                        String formateurInfo = formateursMap.get(module.getId());
                        if (formateurInfo != null) {
                            setText(module.getNom() + " - " + formateurInfo);
                        } else {
                            setText(module.getNom());
                        }
                    }
                }
            });

            moduleCombo.setButtonCell(new ListCell<Module>() {
                @Override
                protected void updateItem(Module module, boolean empty) {
                    super.updateItem(module, empty);
                    if (empty || module == null) {
                        setText(null);
                    } else {
                        String formateurInfo = formateursMap.get(module.getId());
                        if (formateurInfo != null) {
                            setText(module.getNom() + " - " + formateurInfo);
                        } else {
                            setText(module.getNom());
                        }
                    }
                }
            });

            if (!modules.isEmpty()) {
                moduleCombo.setValue(modules.get(0));
            } else {
                moduleCombo.setValue(null);
            }
        }
    }

    private void chargerGroupes() {
        // CHANGEMENT : Le directeur voit TOUS les groupes
        List<Groupe> groupes = groupeDAO.findAll();
        groupeCombo.getItems().clear();
        groupeCombo.getItems().addAll(groupes);

        // Configurer l'affichage pour montrer le matricule
        groupeCombo.setCellFactory(param -> new ListCell<Groupe>() {
            @Override
            protected void updateItem(Groupe groupe, boolean empty) {
                super.updateItem(groupe, empty);
                if (empty || groupe == null) {
                    setText(null);
                } else {
                    setText(groupe.getMatricule() != null ? groupe.getMatricule() : "Groupe " + groupe.getId());
                }
            }
        });

        groupeCombo.setButtonCell(new ListCell<Groupe>() {
            @Override
            protected void updateItem(Groupe groupe, boolean empty) {
                super.updateItem(groupe, empty);
                if (empty || groupe == null) {
                    setText(null);
                } else {
                    setText(groupe.getMatricule() != null ? groupe.getMatricule() : "Groupe " + groupe.getId());
                }
            }
        });

        if (!groupes.isEmpty()) {
            Groupe premierGroupe = groupes.get(0);
            groupeCombo.setValue(premierGroupe);
            // Charger les modules pour le premier groupe
            chargerModulesPourGroupe(premierGroupe);
        }
    }

    private void configurerTableau() {
        matriculeCol.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        // Binding direct sur la propriété
        noteCol.setCellValueFactory(cellData -> {
            ObjectProperty<Double> noteProp = cellData.getValue().noteProperty();
            return new javafx.beans.property.SimpleObjectProperty<>(noteProp.get());
        });

        noteCol.setCellFactory(col -> new TableCell<>() {
            private final TextField textField = new TextField();

            {
                textField.setPromptText("0 - 20");
                textField.setFocusTraversable(false);

                // Validation saisie
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                        textField.setText(oldVal);
                    }
                });

                // Commit quand on quitte le champ
                textField.focusedProperty().addListener((obs, oldVal, focused) -> {
                    if (!focused && getTableRow() != null) {
                        EtudiantNote en = getTableRow().getItem();
                        if (en == null) return;

                        String txt = textField.getText();
                        if (txt == null || txt.isEmpty()) {
                            en.setNote(null);
                        } else {
                            try {
                                double v = Double.parseDouble(txt);
                                if (v >= 0 && v <= 20) {
                                    en.setNote(v);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                EtudiantNote en = getTableRow().getItem();

                if (en.isAbsent()) {
                    textField.setDisable(true);
                    textField.setText("0.00");
                    textField.setStyle("-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;");
                } else {
                    textField.setDisable(false);

                    // Affichez la note (elle peut venir de la base de données)
                    if (note != null) {
                        textField.setText(String.valueOf(note));

                        if (note < 10) {
                            textField.setStyle("-fx-text-fill:#ef4444; -fx-border-color:#ef4444;");
                        } else if (note < 12) {
                            textField.setStyle("-fx-text-fill:#f59e0b; -fx-border-color:#f59e0b;");
                        } else {
                            textField.setStyle("-fx-text-fill:#10b981; -fx-border-color:#10b981;");
                        }
                    } else {
                        textField.setText("");
                        textField.setStyle("");
                    }
                }

                setGraphic(textField);
            }
        });

        // Colonne Absent
        absentCol.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getAbsentCheckBox())
        );

        absentCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(CheckBox box, boolean empty) {
                super.updateItem(box, empty);
                if (empty || box == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        notesTable.setItems(etudiantsList);
    }

    private void chargerEtudiants() {
        etudiantsList.clear();
        sauvegarderButton.setDisable(true);

        Groupe groupe = groupeCombo.getValue();
        Module module = moduleCombo.getValue();

        if (groupe == null || module == null) {
            showAlert("Erreur", "Paramètres manquants",
                    "Veuillez sélectionner un groupe et un module.", Alert.AlertType.ERROR);
            return;
        }

        List<Etudiant> etudiants = etudiantDAO.findByGroupeId(groupe.getId());

        String anneeScolaire = anneeScolaireCombo.getValue();
        String semestre = semestreCombo.getValue();
        String typeEvaluation = typeEvaluationCombo.getValue();

        int annee = Integer.parseInt(anneeScolaire.split("-")[0]);

        for (Etudiant e : etudiants) {
            EtudiantNote en = new EtudiantNote(e);

            // Ajoutez un callback pour rafraîchir la ligne
            en.setRefreshCallback(() -> {
                Platform.runLater(() -> {
                    int index = etudiantsList.indexOf(en);
                    if (index >= 0) {
                        notesTable.getItems().set(index, en);
                    }
                });
            });

            Optional<Note> opt = noteDAO.findExistingNote(
                    e.getId(), module.getId(), annee, semestre, typeEvaluation
            );

            if (opt.isPresent()) {
                Note n = opt.get();
                en.setNote(n.getNote());
                en.setNoteExistanteId(n.getId());

                if (n.getNote() == 0.0 && "Absent".equals(n.getAppreciation())) {
                    en.setAbsent(true);
                }
            }

            etudiantsList.add(en);
        }

        sauvegarderButton.setDisable(false);
        notesTable.refresh();
    }

    private void sauvegarderNotes() {
        if (directeurConnecte == null || etudiantsList.isEmpty()) {
            showAlert("Erreur", "Aucune donnée",
                    "Veuillez d'abord charger les étudiants.", Alert.AlertType.ERROR);
            return;
        }

        Groupe groupe = groupeCombo.getValue();
        Module module = moduleCombo.getValue();
        String anneeScolaire = anneeScolaireCombo.getValue();
        String semestre = semestreCombo.getValue();
        String typeEvaluation = typeEvaluationCombo.getValue();

        if (groupe == null || module == null || anneeScolaire == null) {
            showAlert("Erreur", "Paramètres manquants",
                    "Veuillez remplir tous les paramètres.", Alert.AlertType.ERROR);
            return;
        }

        int annee = Integer.parseInt(anneeScolaire.split("-")[0]);
        int succes = 0;
        int erreurs = 0;

        // CHANGEMENT : Le directeur peut avoir besoin de récupérer le formateur du module
        // On doit trouver le formateur qui enseigne ce module à ce groupe
        FormateurDAO formateurDAO = new FormateurDAO();
        Formateur formateur = formateurDAO.findByModuleAndGroupe(module.getId(), groupe.getId());

        if (formateur == null) {
            showAlert("Erreur", "Formateur introuvable",
                    "Aucun formateur n'enseigne ce module à ce groupe.", Alert.AlertType.ERROR);
            return;
        }

        for (EtudiantNote etudiantNote : etudiantsList) {
            if (etudiantNote.getNote() == null) {
                // Si pas de note, passer au suivant
                continue;
            }

            Note note = new Note();
            note.setEtudiantId(etudiantNote.getEtudiant().getId());
            note.setModuleId(module.getId());
            note.setFormateurId(formateur.getId()); // Utiliser l'ID du formateur qui enseigne le module
            note.setNote(etudiantNote.getNote());
            note.setAppreciation(etudiantNote.isAbsent() ? "Absent" : "");
            note.setAnneeScolaire(annee);
            note.setSemestre(semestre);
            note.setTypeEvaluation(typeEvaluation);

            // Si note existe déjà, mettre à jour
            if (etudiantNote.getNoteExistanteId() != null) {
                note.setId(etudiantNote.getNoteExistanteId());
                if (noteDAO.update(note)) {
                    succes++;
                } else {
                    erreurs++;
                }
            } else {
                // Sinon créer
                Long newId = noteDAO.create(note);
                if (newId != null) {
                    etudiantNote.setNoteExistanteId(newId.intValue());
                    succes++;
                } else {
                    erreurs++;
                }
            }
        }

        showAlert("Succès", "Sauvegarde terminée",
                String.format("%d notes sauvegardées, %d erreurs.", succes, erreurs),
                Alert.AlertType.INFORMATION);

        notesTable.refresh();
    }

    // ========== NAVIGATION MENU ==========
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
            StageManager.loadScene("/view/directeur/Gestion_Planning/PlanningSemaine.fxml", "/styles/gestionFormateurs.css", "gestion des Modules");
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

        try {
            Stage stage = (Stage) nomEcoleLabel.getScene().getWindow();
            alert.initOwner(stage);
            alert.initModality(Modality.WINDOW_MODAL);
        } catch (Exception e) {
            System.err.println("Warning: Could not set alert owner: " + e.getMessage());
        }

        alert.showAndWait();
    }
}