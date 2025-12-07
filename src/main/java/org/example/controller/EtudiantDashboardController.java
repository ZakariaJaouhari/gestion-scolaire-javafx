package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.dao.DirecteurDAO;
import org.example.dao.EtudiantDAO;
import org.example.dao.GroupeDAO;
import org.example.model.Etudiant;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;

public class EtudiantDashboardController {

    @FXML private Label welcomeNameLabel;
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomEtudiantLabel;
    @FXML private Label moyenneLabel;
    @FXML private Label examensCountLabel;
    @FXML private Label modulesCountLabel;
    @FXML private ImageView profileImageView;

    @FXML private TableView<Note> notesTable;
    @FXML private TableColumn<Note, String> moduleColumn;
    @FXML private TableColumn<Note, Double> noteColumn;
    @FXML private TableColumn<Note, String> dateNoteColumn;
    @FXML private TableColumn<Note, Integer> coefficientColumn;

    @FXML private ListView<String> examensListView;
    @FXML private Label aucunExamenLabel;

    private EtudiantDAO etudiantDAO;
    private GroupeDAO groupeDAO;
    private DirecteurDAO directeurDAO;
    private ObservableList<Note> notesList;

    // Classe interne pour les notes
    public static class Note {
        private String module;
        private double note;
        private String date;
        private int coefficient;

        public Note(String module, double note, String date, int coefficient) {
            this.module = module;
            this.note = note;
            this.date = date;
            this.coefficient = coefficient;
        }

        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }

        public double getNote() { return note; }
        public void setNote(double note) { this.note = note; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public int getCoefficient() { return coefficient; }
        public void setCoefficient(int coefficient) { this.coefficient = coefficient; }
    }

    @FXML
    public void initialize() {
        System.out.println("EtudiantDashboardController initialisé");

        // Vérifier la session
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Vérifier que c'est un étudiant (vous devrez ajouter cette vérification dans SessionManager)
        // Pour l'instant, on vérifie juste qu'il y a un utilisateur
        Object currentUser = session.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Initialiser les DAO
        etudiantDAO = new EtudiantDAO();
        groupeDAO = new GroupeDAO();
        directeurDAO = new DirecteurDAO();

        // Mettre à jour les labels
        updateUserInfo(currentUser);

        // Initialiser les statistiques
        loadStats();

        // Initialiser la table des notes
        initializeNotesTable();

        // Initialiser la liste des examens
        initializeExamensList();

        // Charger les données
        loadNotes();
        loadExamens();
    }

    private void updateUserInfo(Object currentUser) {
        // Vérifier le type d'utilisateur
        if (currentUser instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) currentUser;

            // Mettre à jour les labels
            welcomeNameLabel.setText(etudiant.getPrenom());
            nomEtudiantLabel.setText(etudiant.getNomComplet());

            // Récupérer le nom de l'école via le directeur
            if (etudiant.getDirecteurId() > 0) {
                DirecteurDAO DirecteurDAO = new DirecteurDAO();
                String nomEcole = DirecteurDAO.getNomEcoleByDirecteurId(etudiant.getDirecteurId());
                nomEcoleLabel.setText(nomEcole);
            } else {
                nomEcoleLabel.setText("École non spécifiée");
            }

            // Charger la photo de profil si disponible
            if (etudiant.getProfilePicture() != null && !etudiant.getProfilePicture().isEmpty()) {
                try {
                    profileImageView.setImage(new Image("file:" + etudiant.getProfilePicture()));
                } catch (Exception e) {
                    System.err.println("Erreur chargement photo: " + e.getMessage());
                }
            }
        } else {
            // Si ce n'est pas un étudiant, rediriger
            System.err.println("❌ L'utilisateur n'est pas un étudiant");
            redirectToLogin();
        }
    }

    private void loadStats() {
        // Données temporaires - À remplacer par des appels DAO réels
        moyenneLabel.setText("15.75");
        examensCountLabel.setText("3");
        modulesCountLabel.setText("6");
    }

    private void initializeNotesTable() {
        notesList = FXCollections.observableArrayList();
        notesTable.setItems(notesList);

        // Configurer les colonnes
        moduleColumn.setCellValueFactory(new PropertyValueFactory<>("module"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        dateNoteColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));

        // Formatter la colonne note
        noteColumn.setCellFactory(column -> new TableCell<Note, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f/20", item));

                    // Colorier selon la note
                    if (item >= 16) {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                    } else if (item >= 10) {
                        setStyle("-fx-text-fill: #3b82f6;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444;");
                    }
                }
            }
        });
    }

    private void initializeExamensList() {
        examensListView.setItems(FXCollections.observableArrayList());
    }

    private void loadNotes() {
        // Données d'exemple - À remplacer par des appels DAO réels
        notesList.add(new Note("Programmation Java", 17.5, "2024-03-10", 3));
        notesList.add(new Note("Base de données", 14.0, "2024-03-12", 2));
        notesList.add(new Note("Algorithmique", 12.5, "2024-03-15", 2));
        notesList.add(new Note("Systèmes d'exploitation", 16.0, "2024-03-18", 1));

        // Calculer la moyenne
        double sommeNotes = 0;
        int sommeCoefficients = 0;

        for (Note note : notesList) {
            sommeNotes += note.getNote() * note.getCoefficient();
            sommeCoefficients += note.getCoefficient();
        }

        if (sommeCoefficients > 0) {
            double moyenne = sommeNotes / sommeCoefficients;
            moyenneLabel.setText(String.format("%.2f", moyenne));
        }
    }

    private void loadExamens() {
        // Données d'exemple - À remplacer par des appels DAO réels
        ObservableList<String> examens = FXCollections.observableArrayList(
                "📝 Culture et techniques numérique - 2024-03-25 14:30",
                "📝 Bureautique - 2024-04-02 08:30",
                "📝 Programmation Web - 2024-04-10 10:00"
        );

        examensListView.setItems(examens);
        examensCountLabel.setText(String.valueOf(examens.size()));

        // Afficher/masquer le label "aucun examen"
        aucunExamenLabel.setVisible(examens.isEmpty());
    }

    // Méthodes de navigation
    @FXML
    private void handleHome() {
        System.out.println("Accueil étudiant clicked");
    }

    @FXML
    private void handleNotes() {
        System.out.println("Mes Notes clicked");
        // Rediriger vers la page des notes
        // StageManager.loadScene("/view/etudiant/notes.fxml", ...);
    }

    @FXML
    private void handleEmploiDuTemps() {
        System.out.println("Emploi du temps clicked");
        // Rediriger vers la page emploi du temps
        // StageManager.loadScene("/view/etudiant/emploi.fxml", ...);
    }

    @FXML
    private void handleExamens() {
        System.out.println("Mes Examens clicked");
        // Rediriger vers la page des examens
        // StageManager.loadScene("/view/etudiant/examens.fxml", ...);
    }

    @FXML
    private void handleProfil() {
        System.out.println("Mon Profil clicked");
        // Rediriger vers la page profil
        // StageManager.loadScene("/view/etudiant/profil.fxml", ...);
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