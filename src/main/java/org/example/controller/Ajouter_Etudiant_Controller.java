package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.EtudiantDAO;
import org.example.dao.GroupeDAO;
import org.example.model.Etudiant;
import org.example.model.Etudiant.Sexe;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.model.Groupe;
import org.example.util.SessionManager;
import org.example.util.StageManager;

import java.io.IOException;
import java.time.LocalDate;

public class Ajouter_Etudiant_Controller {

    // HEADER
    @FXML private Label nomEcoleLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label versionLabel;

    // Champs du formulaire
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;

    // ComboBox pour date de naissance
    @FXML private ComboBox<String> dayComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<String> yearComboBox;

    @FXML private ComboBox<String> sexeComboBox;



    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private Button ajouterButton;
    @FXML private Button cancelButton;

    @FXML private ComboBox<Groupe> groupeComboBox;
    private GroupeDAO groupeDAO;

    private EtudiantDAO etudiantDAO;



    @FXML
    public void initialize() {
        etudiantDAO = new EtudiantDAO();
        groupeDAO = new GroupeDAO();


        // --- HEADER ----
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            nomEcoleLabel.setText(session.getNomEcole());
            nomDirecteurLabel.setText(session.getNomDirecteur());
        }
        versionLabel.setText("V 0.1.0");

        // Initialiser les ComboBox de sexe et situation
        sexeComboBox.getItems().setAll("Homme", "Femme");

        // Remplir les ComboBox pour les dates
        populateDateComboBoxes();

        loadGroupes();
        // Configurer les événements
        configureEvents();

        // Configurer les styles des boutons
        setupButtonStyles();
    }

    private void loadGroupes() {
        int directeurId = SessionManager.getInstance()
                .getCurrentDirecteur()
                .getId();

        groupeComboBox.getItems().setAll(
                groupeDAO.findByDirecteurId(directeurId)
        );

        // Afficher uniquement le matricule
        groupeComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Groupe groupe, boolean empty) {
                super.updateItem(groupe, empty);
                setText(empty || groupe == null ? "" : groupe.getMatricule());
            }
        });

        // Affichage de l’élément sélectionné
        groupeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Groupe groupe, boolean empty) {
                super.updateItem(groupe, empty);
                setText(empty || groupe == null ? "" : groupe.getMatricule());
            }
        });
    }



    private void populateDateComboBoxes() {
        // Remplir les jours (01 à 31)
        for (int i = 1; i <= 31; i++) {
            String day = String.format("%02d", i);
            dayComboBox.getItems().add(day);
        }

        // Remplir les mois avec format "MM - NomMois"
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] monthValues = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        for (int i = 0; i < months.length; i++) {
            String monthDisplay = monthValues[i] + " - " + months[i];
            monthComboBox.getItems().add(monthDisplay);
        }

        // Remplir les années (de l'année actuelle à 1905)
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear; i >= 1905; i--) {
            String year = String.valueOf(i);
            yearComboBox.getItems().add(year);
        }

        // Sélectionner les valeurs par défaut
        // Date de naissance : par défaut 25 ans
        dayComboBox.getSelectionModel().select(0); // Jour 01
        monthComboBox.getSelectionModel().select(0); // Janvier
        yearComboBox.getSelectionModel().selectFirst(); // Année actuelle


    }

    private String getMonthName(int month) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1];
    }

    private void configureEvents() {
        // Action des boutons
        ajouterButton.setOnAction(e -> ajouterEtudiant());
        cancelButton.setOnAction(e -> returnToGestionEtudiants());

        // Génération automatique de l'email et du mot de passe
        nomField.setOnKeyReleased(e -> generateEmailAndPassword());
        prenomField.setOnKeyReleased(e -> generateEmail());
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

    private void ajouterEtudiant() {
        try {
            // Validation des champs
            if (!validateFields()) {
                return;
            }

            // Récupération des valeurs
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String cin = cinField.getText().trim();

            // Conversion des dates
            LocalDate dateNaissance = getDateFromComboBoxes(
                    dayComboBox.getValue(),
                    monthComboBox.getValue(),
                    yearComboBox.getValue()
            );


            String sexe = sexeComboBox.getValue();
            String emailPart = emailField.getText().trim();
            String email = emailPart + "@taalim.ma";
            String password = passwordField.getText();

            // Validation mot de passe
            if (password.length() < 6) {
                showAlert("Erreur", "Mot de passe trop court",
                        "Le mot de passe doit contenir au moins 6 caractères.",
                        Alert.AlertType.ERROR);
                return;
            }

            // 🔒 HASH DU MOT DE PASSE
            String hashedPassword = DigestUtils.sha256Hex(password);

            Groupe groupe = groupeComboBox.getValue();
            Groupe groupeSelectionne = groupeComboBox.getValue();

            if (groupeSelectionne == null) {
                showAlert("Validation", "Groupe manquant",
                        "Veuillez sélectionner un groupe.",
                        Alert.AlertType.WARNING);
                return;
            }

            int groupeId = groupeSelectionne.getId();



            // Vérifications supplémentaires
            if (dateNaissance == null ) {
                showAlert("Erreur", "Date invalide",
                        "Veuillez vérifier les dates saisies.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Vérifier la validité de l'email
            if (!isValidEmail(emailPart)) {
                showAlert("Erreur", "Email invalide",
                        "L'email ne doit contenir que des lettres, chiffres, points et tirets.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Récupérer l'id du directeur connecté
            int directeurId = SessionManager.getInstance().getCurrentDirecteur().getId();

            // Créer l'objet Formateur
            Etudiant etudiant = new Etudiant(
                    nom,
                    prenom,
                    dateNaissance,
                    cin,
                    Sexe.fromString(sexe),
                    groupeId,
                    email,
                    hashedPassword,
                    directeurId
            );


            // Sauvegarder dans la base de données
            Long id = etudiantDAO.create(etudiant);

            if (id != null) {

                showAlert(
                        "Succès",
                        "Etudiant ajouté",
                        "L'etudiant a été ajouté avec succès !",
                        Alert.AlertType.INFORMATION
                );

                // 👉 navigation APRES l'alert
                returnToGestionEtudiants();
            } else {
                showAlert("Erreur", "Échec de l'ajout",
                        "Une erreur est survenue lors de l'ajout d'etudiant.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Exception",
                    "Erreur inattendue: " + ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    // Méthode pour valider l'email (partie avant @taalim.ma)
    private boolean isValidEmail(String emailPart) {
        if (emailPart == null || emailPart.isEmpty()) {
            return false;
        }
        // Vérifier que l'email ne contient que des lettres, chiffres, points et tirets
        return emailPart.matches("^[a-zA-Z0-9.\\-]+$");
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }
        if (prenomField.getText().trim().isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
        }
        if (cinField.getText().trim().isEmpty()) {
            errors.append("- Le CIN est obligatoire\n");
        }
        if (sexeComboBox.getValue() == null) {
            errors.append("- Le sexe est obligatoire\n");
        }
        if (groupeComboBox.getValue() == null) {
            errors.append("- Le groupe est obligatoire\n");
        }
        if (emailField.getText().trim().isEmpty()) {
            errors.append("- L'email est obligatoire\n");
        }
        if (passwordField.getText().isEmpty()) {
            errors.append("- Le mot de passe est obligatoire\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Champs manquants",
                    errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private LocalDate getDateFromComboBoxes(String day, String monthStr, String year) {
        try {
            if (day == null || monthStr == null || year == null) {
                return null;
            }

            // Extraire la valeur numérique du mois (ex: "01 - Jan" -> "01")
            String monthValue = monthStr.split(" - ")[0];

            int dayInt = Integer.parseInt(day);
            int monthInt = Integer.parseInt(monthValue);
            int yearInt = Integer.parseInt(year);

            // Valider la date
            if (dayInt < 1 || dayInt > 31 || monthInt < 1 || monthInt > 12 || yearInt < 1905) {
                return null;
            }

            return LocalDate.of(yearInt, monthInt, dayInt);

        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion de la date: " + e.getMessage());
            return null;
        }
    }

    private void clearForm() {
        // Réinitialiser les champs texte
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        emailField.clear();
        passwordField.clear();

        // Réinitialiser les ComboBox
        dayComboBox.getSelectionModel().select(0);
        monthComboBox.getSelectionModel().select(0);
        yearComboBox.getSelectionModel().selectFirst();


        // Réinitialiser les autres ComboBox
        sexeComboBox.getSelectionModel().clearSelection();

        // Remettre le focus sur le premier champ
        nomField.requestFocus();
    }

    // Méthode pour retourner à la page gestionFormateurs
    private void returnToGestionEtudiants() {
        try {
            StageManager.loadScene("/view/directeur/gestionEtudiants.fxml",
                    "/styles/gestionFormateurs.css", "Etudiants");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la gestion des formateurs.", Alert.AlertType.ERROR);
        }
    }

    // Méthode modifiée pour le bouton Annuler
    private void closeWindow() {
        // Cette méthode n'est plus utilisée, utiliser returnToGestionFormateurs() à la place
        returnToGestionEtudiants();
    }

    @FXML
    private void generateEmailAndPassword() {
        generateEmail();
        generatePassword();
    }

    @FXML
    private void generateEmail() {
        String nom = nomField.getText().trim().toLowerCase();
        String prenom = prenomField.getText().trim().toLowerCase();

        if (!nom.isEmpty() && !prenom.isEmpty()) {
            // Nettoyer les accents et caractères spéciaux
            String nomClean = removeAccents(nom);
            String prenomClean = removeAccents(prenom);

            // Générer email au format prenom.nom (sans @taalim.ma)
            String email = prenomClean + "." + nomClean;
            emailField.setText(email);
        }
    }

    // Méthode pour supprimer les accents
    private String removeAccents(String input) {
        if (input == null) return "";

        // Normaliser et remplacer les caractères accentués
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-z0-9.]", "");
    }

    @FXML
    private void generatePassword() {
        String nom = nomField.getText().trim().toLowerCase();

        if (!nom.isEmpty()) {
            // Nettoyer les accents
            String nomClean = removeAccents(nom);
            // Générer mot de passe basé sur le nom
            String password = nomClean + "123";
            passwordField.setText(password);
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
    // ======================= NAVIGATION MENU =============================
    // =====================================================================
    @FXML
    private void handleHome() {
        try {
            StageManager.loadScene("/view/directeur/dashboard.fxml", "/styles/dashboard.css", "Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger le dashboard.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFormateurs() {
        try {
            StageManager.loadScene("/view/directeur/Gestion Formateurs/gestionFormateurs.fxml",
                    "/styles/gestionFormateurs.css", "Formateurs");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la gestion des formateurs.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEtudiants() {
        returnToGestionEtudiants();
    }

    @FXML
    private void handleGroupes() {
        try {
            StageManager.loadScene("/view/directeur/gestionGroupes.fxml",
                    "/styles/gestionGroupes.css", "Groupes");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la gestion des groupes.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleModules() {
        try {
            StageManager.loadScene("/view/directeur/gestionModules.fxml",
                    "/styles/gestionModules.css", "Modules");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la gestion des modules.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleNotes() {
        try {
            StageManager.loadScene("/view/directeur/gestionNotes.fxml",
                    "/styles/gestionNotes.css", "Notes");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la gestion des notes.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCertificats() {
        try {
            StageManager.loadScene("/view/directeur/gestionCertificats.fxml",
                    "/styles/gestionCertificats.css", "Certificats");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Navigation impossible",
                    "Impossible de charger la gestion des certificats.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Déconnexion");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.getInstance().clearSession();
                try {
                    StageManager.loadScene("/view/login_register.fxml",
                            "/styles/auth.css", "Connexion");
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Déconnexion impossible",
                            "Impossible de charger la page de connexion.", Alert.AlertType.ERROR);
                }
            }
        });
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

        Stage stage = (Stage) nomField.getScene().getWindow(); // n'importe quel champ
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.showAndWait();
    }
}