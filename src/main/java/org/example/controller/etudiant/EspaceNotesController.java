package org.example.controller.etudiant;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.DirecteurDAO;
import org.example.dao.NoteDAO;
import org.example.model.Etudiant;
import org.example.model.Groupe;
import org.example.util.SessionManager;
import org.example.util.StageManager;
import java.io.IOException;

public class EspaceNotesController {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomEtudiantLabel;
    @FXML private Label versionLabel;

    // FILTRES
    @FXML private ComboBox<String> anneeScolaireCombo;
    @FXML private ComboBox<String> semestreCombo;
    @FXML private Button chargerButton;

    // TABLEAU DES NOTES
    @FXML private TableView<NoteEtudiant> notesTable;
    @FXML private TableColumn<NoteEtudiant, String> moduleCol;
    @FXML private TableColumn<NoteEtudiant, Double> noteControleCol;
    @FXML private TableColumn<NoteEtudiant, Double> noteExamenCol;
    @FXML private TableColumn<NoteEtudiant, Double> noteModuleCol;
    @FXML private TableColumn<NoteEtudiant, Double> coefficientCol;
    @FXML private TableColumn<NoteEtudiant, Double> noteCieCol;

    // STATISTIQUES
    @FXML private Label moyenneGeneraleLabel;
    @FXML private Label decisionLabel;
    @FXML private Label modulesTotalLabel;
    @FXML private Label modulesCompletsLabel;

    // Données
    private ObservableList<NoteEtudiant> notesList = FXCollections.observableArrayList();
    private NoteDAO noteDAO = new NoteDAO();
    private Etudiant etudiantConnecte;

    // Classe pour représenter une note d'un module
    public static class NoteEtudiant {
        private String module;
        private Double noteControle;
        private Double noteExamen;
        private Double noteModule;
        private Integer coefficient;
        private Double noteCie;

        // Constructeur
        public NoteEtudiant(String module, Integer coefficient) {
            this.module = module;
            this.coefficient = coefficient;
            this.noteControle = null;
            this.noteExamen = null;
            this.noteModule = null;
            this.noteCie = null;
        }

        // Getters et Setters
        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }

        public Double getNoteControle() { return noteControle; }
        public void setNoteControle(Double noteControle) {
            this.noteControle = noteControle;
            calculerNoteModule();
        }

        public Double getNoteExamen() { return noteExamen; }
        public void setNoteExamen(Double noteExamen) {
            this.noteExamen = noteExamen;
            calculerNoteModule();
        }

        public Double getNoteModule() { return noteModule; }
        public void setNoteModule(Double noteModule) { this.noteModule = noteModule; }

        public Integer getCoefficient() { return coefficient; }
        public void setCoefficient(Integer coefficient) { this.coefficient = coefficient; }

        public Double getNoteCie() { return noteCie; }
        public void setNoteCie(Double noteCie) { this.noteCie = noteCie; }

        // Calculer la note du module (40% contrôle + 60% examen)
        private void calculerNoteModule() {
            if (noteControle != null && noteExamen != null) {
                this.noteModule = (noteControle * 0.4) + (noteExamen * 0.6);
                if (coefficient != null) {
                    this.noteCie = noteModule * coefficient;
                }
            }
        }

        // Vérifier si les deux notes sont présentes
        public boolean isComplet() {
            return noteControle != null && noteExamen != null;
        }
    }

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            etudiantConnecte = session.getCurrentEtudiant();
            if (etudiantConnecte != null) {
                nomEtudiantLabel.setText(etudiantConnecte.getNomComplet());
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

        // Configurer le tableau
        configurerTableau();

        // Initialiser les filtres
        initialiserFiltres();

        // Charger les données initiales
        chargerNotes();
    }

    private void initialiserFiltres() {
        // Année scolaire
        int anneeCourante = java.time.LocalDate.now().getYear();
        anneeScolaireCombo.getItems().addAll(
                (anneeCourante-1) + "-" + anneeCourante,
                anneeCourante + "-" + (anneeCourante+1)
        );
        anneeScolaireCombo.setValue(anneeCourante + "-" + (anneeCourante+1));

        // Semestre
        semestreCombo.getItems().addAll("Semestre 1", "Semestre 2", "Semestre 3", "Semestre 4");
        semestreCombo.setValue("Semestre 1");

        // Événement
        chargerButton.setOnAction(e -> chargerNotes());
    }

    private void configurerTableau() {
        // Configurer les colonnes
        moduleCol.setCellValueFactory(new PropertyValueFactory<>("module"));
        coefficientCol.setCellValueFactory(new PropertyValueFactory<>("coefficient"));

        // Colonne Note Contrôle
        noteControleCol.setCellValueFactory(new PropertyValueFactory<>("noteControle"));
        noteControleCol.setCellFactory(col -> new TableCell<NoteEtudiant, Double>() {
            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText("--");
                    setStyle("");
                } else {
                    setText(String.format("%.2f", note));
                    setStyle(getColorStyle(note));
                }
            }
        });

        // Colonne Note Examen
        noteExamenCol.setCellValueFactory(new PropertyValueFactory<>("noteExamen"));
        noteExamenCol.setCellFactory(col -> new TableCell<NoteEtudiant, Double>() {
            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText("--");
                    setStyle("");
                } else {
                    setText(String.format("%.2f", note));
                    setStyle(getColorStyle(note));
                }
            }
        });

        // Colonne Note Module
        noteModuleCol.setCellValueFactory(new PropertyValueFactory<>("noteModule"));
        noteModuleCol.setCellFactory(col -> new TableCell<NoteEtudiant, Double>() {
            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText("--");
                    setStyle("");
                } else {
                    setText(String.format("%.2f", note));
                    setStyle(getColorStyle(note));
                }
            }
        });

        // Colonne Note CIE (Coefficient * Note)
        noteCieCol.setCellValueFactory(new PropertyValueFactory<>("noteCie"));
        noteCieCol.setCellFactory(col -> new TableCell<NoteEtudiant, Double>() {
            @Override
            protected void updateItem(Double note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText("--");
                    setStyle("");
                } else {
                    setText(String.format("%.2f", note));
                    setStyle(getColorStyle(note / getTableView().getItems().get(getIndex()).getCoefficient()));
                }
            }
        });

        notesTable.setItems(notesList);
    }

    private String getColorStyle(double note) {
        if (note < 10) {
            return "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
        } else if (note < 12) {
            return "-fx-text-fill: #f59e0b; -fx-font-weight: bold;";
        } else {
            return "-fx-text-fill: #10b981; -fx-font-weight: bold;";
        }
    }

    private void chargerNotes() {
        notesList.clear();

        if (etudiantConnecte == null) {
            showAlert("Erreur", "Non connecté",
                    "Veuillez vous connecter en tant qu'étudiant.", Alert.AlertType.ERROR);
            return;
        }

        String anneeScolaire = anneeScolaireCombo.getValue();
        String semestre = semestreCombo.getValue();

        if (anneeScolaire == null || semestre == null) {
            showAlert("Erreur", "Paramètres manquants",
                    "Veuillez sélectionner l'année scolaire et le semestre.", Alert.AlertType.ERROR);
            return;
        }

        int annee = Integer.parseInt(anneeScolaire.split("-")[0]);

        // Récupérer le bulletin de l'étudiant
        NoteDAO.BulletinResultat bulletin = noteDAO.calculerBulletinEtudiant(
                etudiantConnecte.getId(), annee, semestre
        );

        if (bulletin == null || bulletin.getModuleNotes() == null) {
            showAlert("Information", "Aucune note",
                    "Aucune note disponible pour cette période.", Alert.AlertType.INFORMATION);
            return;
        }

        // Remplir le tableau avec les notes
        for (NoteDAO.ModuleNote moduleNote : bulletin.getModuleNotes()) {
            NoteEtudiant noteEtudiant = new NoteEtudiant(
                    moduleNote.getModuleNom(),
                    moduleNote.getCoefficient()
            );

            noteEtudiant.setNoteControle(moduleNote.getNoteControle());
            noteEtudiant.setNoteExamen(moduleNote.getNoteExamen());
            noteEtudiant.setNoteModule(moduleNote.getNoteModule());
            noteEtudiant.setNoteCie(moduleNote.getNoteCie());

            notesList.add(noteEtudiant);
        }

        // Mettre à jour les statistiques
        updateStatistics(bulletin);
    }

    private void updateStatistics(NoteDAO.BulletinResultat bulletin) {
        // Calculer le nombre de modules complets
        long modulesComplets = notesList.stream()
                .filter(NoteEtudiant::isComplet)
                .count();

        // Mettre à jour les labels
        modulesTotalLabel.setText(String.valueOf(bulletin.getNombreModules()));
        modulesCompletsLabel.setText(String.valueOf(modulesComplets));
        moyenneGeneraleLabel.setText(String.format("%.2f", bulletin.getMoyenneGenerale()));
        decisionLabel.setText(bulletin.getDecision());

        // Colorer la décision
        if ("Admis".equals(bulletin.getDecision())) {
            decisionLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else {
            decisionLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 16px;");
        }

        // Colorer la moyenne
        if (bulletin.getMoyenneGenerale() >= 10) {
            moyenneGeneraleLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else {
            moyenneGeneraleLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 24px;");
        }
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

        Stage stage = (Stage) nomEcoleLabel.getScene().getWindow();
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.showAndWait();
    }
}