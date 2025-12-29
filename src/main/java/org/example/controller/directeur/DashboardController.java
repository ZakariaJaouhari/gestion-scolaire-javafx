package org.example.controller.directeur;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import java.util.Map;

import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.EtudiantDAO;
import org.example.dao.FormateurDAO;
import org.example.dao.GroupeDAO;
import org.example.dao.ModuleDAO;
import org.example.model.Directeur;
import org.example.util.SessionManager;
import org.example.util.StageManager;

public class DashboardController {

    // Labels header
    @FXML private Label welcomeNameLabel;
    @FXML private Label nomDirecteurLabel;
    @FXML private Label nomEcoleLabel;
    @FXML private Label versionLabel;

    // Cartes statistiques
    @FXML private Label nombreFormateursLabel;
    @FXML private Label nombreStagiairesLabel;
    @FXML private Label nombreGroupesLabel;
    @FXML private Label nombreTotalGroupesLabel;

    // Statistiques
    @FXML private Label totalHommesLabel;
    @FXML private Label totalFemmesLabel;

    // Conteneurs
    @FXML private VBox cardFormateurs;
    @FXML private VBox cardStagiaires;
    @FXML private VBox cardGroupes;

    // Graphique
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Calendrier
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;

    // Données du calendrier
    private LocalDate currentDate;
    private YearMonth currentYearMonth;

    @FXML
    private void initialize() {
        System.out.println("📊 DashboardController initialisé - Design Premium");

        try {
            // Initialiser la date actuelle
            currentDate = LocalDate.now();
            currentYearMonth = YearMonth.from(currentDate);

            // Initialiser les données de session
            initializeSessionData();

            // Initialiser les statistiques
            initializeStatistics();

            // Initialiser le graphique
            initializeChart();

            // Initialiser le calendrier
            updateCalendar();

            // Initialiser les styles
            initializeStyles();

            System.out.println("✅ Dashboard premium initialisé avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeSessionData() {
        SessionManager session = SessionManager.getInstance();

        if (session.isLoggedIn()) {
            System.out.println("✅ Session active pour: " + session.getNomDirecteur());

            // Récupérer les données du directeur connecté
            nomDirecteurLabel.setText(session.getNomDirecteur());
            nomEcoleLabel.setText(session.getNomEcole());

            // Message de bienvenue avec le prénom
            String nomComplet = session.getNomDirecteur();
            String[] parties = nomComplet.split(" ");
            String prenom = parties.length > 0 ? parties[0] : nomComplet;
            welcomeNameLabel.setText(prenom);

        } else {
            System.out.println("❌ Pas de session active - redirection vers login");
            redirectToLogin();
        }
    }

    private void initializeStatistics() {
        SessionManager session = SessionManager.getInstance();
        Directeur directeur = session.getCurrentDirecteur();
        int directeurId = directeur.getId();

        EtudiantDAO etudiantDAO = new EtudiantDAO();
        FormateurDAO formateurDAO = new FormateurDAO();
        GroupeDAO groupeDAO = new GroupeDAO();

        // Récupérer les statistiques
        int nbEtudiants = etudiantDAO.countByDirecteurId(directeurId);
        int nbFormateurs = formateurDAO.countByDirecteurId(directeurId);
        int nbGroupes = groupeDAO.countByDirecteurId(directeurId);
        int nbHommes = etudiantDAO.countHommesByDirecteurId(directeurId);
        int nbFemmes = etudiantDAO.countFemmesByDirecteurId(directeurId);

        // Mettre à jour les labels
        nombreStagiairesLabel.setText(String.valueOf(nbEtudiants));
        nombreFormateursLabel.setText(String.valueOf(nbFormateurs));
        nombreGroupesLabel.setText(String.valueOf(nbGroupes));
        nombreTotalGroupesLabel.setText(String.valueOf(nbGroupes)); // Même valeur
        totalHommesLabel.setText(String.valueOf(nbHommes));
        totalFemmesLabel.setText(String.valueOf(nbFemmes));
    }

    @SuppressWarnings("unchecked")
    private void initializeChart() {
        try {
            // Configuration du graphique
            barChart.setTitle("");
            barChart.setLegendVisible(false);
            barChart.setAnimated(true);

            // Afficher les labels de l'axe X (pour les matricules)
            xAxis.setTickLabelsVisible(true);
            xAxis.setTickMarkVisible(true);
            xAxis.setOpacity(1);
            xAxis.setTickLabelFont(javafx.scene.text.Font.font(10)); // Taille de police

            // Cacher les labels de l'axe Y
            yAxis.setTickLabelsVisible(false);
            yAxis.setTickMarkVisible(false);
            yAxis.setOpacity(0);

            // Récupérer les données réelles depuis la base
            SessionManager session = SessionManager.getInstance();
            Directeur directeur = session.getCurrentDirecteur();

            if (directeur == null) {
                System.err.println("❌ Directeur non connecté");
                return;
            }

            // Récupérer les statistiques des étudiants par groupe
            EtudiantDAO etudiantDAO = new EtudiantDAO();
            Map<String, Object> statsEtudiants = etudiantDAO.getStatistiquesParGroupe(directeur.getId());

            // Récupérer les statistiques des modules par groupe
            ModuleDAO moduleDAO = new ModuleDAO();
            Map<String, Object> statsModules = moduleDAO.getStatistiquesModulesParGroupe(directeur.getId());

            // Vérifier si on a des données
            List<String> groupes = (List<String>) statsEtudiants.get("groupes");
            List<Integer> hommes = (List<Integer>) statsEtudiants.get("hommes");
            List<Integer> femmes = (List<Integer>) statsEtudiants.get("femmes");
            List<Integer> modules = (List<Integer>) statsModules.get("modules");
            List<Integer> formateurs = (List<Integer>) statsModules.get("formateurs");

            if (groupes == null || groupes.isEmpty()) {
                System.out.println("⚠️ Aucun groupe trouvé pour ce directeur");
                // Utiliser des données d'exemple temporaires
                String[] groupesExemple = {"Aucun groupe"};
                int[] hommesExemple = {0};
                int[] femmesExemple = {0};
                int[] modulesExemple = {0};
                int[] formateursExemple = {0};

                createChartSeriesWithMatricules(groupesExemple, hommesExemple, femmesExemple, modulesExemple, formateursExemple);
                return;
            }

            // Convertir les listes en tableaux pour le graphique
            String[] groupesArray = groupes.toArray(new String[0]);
            int[] hommesArray = hommes.stream().mapToInt(i -> i).toArray();
            int[] femmesArray = femmes.stream().mapToInt(i -> i).toArray();
            int[] modulesArray = modules.stream().mapToInt(i -> i).toArray();
            int[] formateursArray = formateurs.stream().mapToInt(i -> i).toArray();

            // Créer les séries avec les données réelles
            createChartSeriesWithMatricules(groupesArray, hommesArray, femmesArray, modulesArray, formateursArray);

            System.out.println("✅ Graphique initialisé avec données réelles");
            System.out.println("   Groupes: " + groupes.size());
            System.out.println("   Total hommes: " + hommes.stream().mapToInt(i -> i).sum());
            System.out.println("   Total femmes: " + femmes.stream().mapToInt(i -> i).sum());

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation graphique: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createChartSeriesWithMatricules(String[] matricules, int[] hommes, int[] femmes, int[] modules, int[] formateurs) {
        // Créer les séries
        XYChart.Series<String, Number> seriesHommes = new XYChart.Series<>();
        seriesHommes.setName("Hommes");

        XYChart.Series<String, Number> seriesFemmes = new XYChart.Series<>();
        seriesFemmes.setName("Femmes");

        XYChart.Series<String, Number> seriesModules = new XYChart.Series<>();
        seriesModules.setName("Modules");

        XYChart.Series<String, Number> seriesFormateurs = new XYChart.Series<>();
        seriesFormateurs.setName("Formateurs");

        // Utiliser directement les matricules comme catégories
        for (int i = 0; i < matricules.length; i++) {
            seriesHommes.getData().add(new XYChart.Data<>(matricules[i], hommes[i]));
            seriesFemmes.getData().add(new XYChart.Data<>(matricules[i], femmes[i]));
            seriesModules.getData().add(new XYChart.Data<>(matricules[i], modules[i]));
            seriesFormateurs.getData().add(new XYChart.Data<>(matricules[i], formateurs[i]));
        }

        // Ajouter les séries au graphique
        barChart.getData().addAll(seriesHommes, seriesFemmes, seriesModules, seriesFormateurs);

        // Personnaliser l'affichage de l'axe X
        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(5);
        xAxis.setTickLabelFont(Font.font("System", FontWeight.BOLD, 11));

        // Appliquer les couleurs
        applyChartColors();
    }

    private void applyChartColors() {
        // Appliquer les couleurs après un court délai pour que le graphique soit rendu
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.millis(100)
        );
        pause.setOnFinished(event -> {
            if (barChart.getData().size() >= 4) {
                // Couleurs selon votre design HTML
                applySeriesColor(0, "#3730a3"); // Hommes - Indigo
                applySeriesColor(1, "#ef4444"); // Femmes - Rouge
                applySeriesColor(2, "#06b6d4"); // Modules - Cyan
                applySeriesColor(3, "#059669"); // Formateurs - Émeraude
            }
        });
        pause.play();
    }

    private void applySeriesColor(int seriesIndex, String color) {
        if (barChart.getData().size() > seriesIndex) {
            XYChart.Series<String, Number> series = barChart.getData().get(seriesIndex);
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
    }

    private void updateCalendar() {
        try {
            // Mettre à jour le label du mois/année
            String month = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
            int year = currentYearMonth.getYear();
            monthYearLabel.setText(month.substring(0, 1).toUpperCase() + month.substring(1) + " " + year);

            // Nettoyer le grid
            calendarGrid.getChildren().clear();

            // Ajouter les jours de la semaine
            String[] joursSemaine = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
            for (int i = 0; i < 7; i++) {
                Label dayLabel = new Label(joursSemaine[i]);
                dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                dayLabel.setTextFill(Color.web("#6b7280"));
                dayLabel.setStyle("-fx-padding: 10; -fx-alignment: center;");
                calendarGrid.add(dayLabel, i, 0);
            }

            // Premier jour du mois
            LocalDate firstDay = currentYearMonth.atDay(1);
            int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // Dimanche = 0

            // Nombre de jours dans le mois
            int daysInMonth = currentYearMonth.lengthOfMonth();

            // Remplir les jours
            int row = 1;
            int col = firstDayOfWeek;

            for (int day = 1; day <= daysInMonth; day++) {
                StackPane dayCell = createDayCell(day);
                calendarGrid.add(dayCell, col, row);

                col++;
                if (col > 6) {
                    col = 0;
                    row++;
                }
            }

            System.out.println("✅ Calendrier mis à jour: " + month + " " + year);

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour calendrier: " + e.getMessage());
        }
    }

    private StackPane createDayCell(int day) {
        StackPane cell = new StackPane();
        cell.setPrefSize(50, 50);

        // Vérifier si c'est aujourd'hui
        boolean isToday = currentYearMonth.equals(YearMonth.from(LocalDate.now())) &&
                day == LocalDate.now().getDayOfMonth();

        // Cercle de fond pour aujourd'hui
        if (isToday) {
            Circle circle = new Circle(20);
            circle.setFill(Color.web("#059669"));
            cell.getChildren().add(circle);
        }

        // Label du jour
        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        dayLabel.setTextFill(isToday ? Color.WHITE : Color.BLACK);

        cell.getChildren().add(dayLabel);

        // Effet hover
        cell.setOnMouseEntered(e -> {
            if (!isToday) {
                cell.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 25;");
            }
        });

        cell.setOnMouseExited(e -> {
            if (!isToday) {
                cell.setStyle("");
            }
        });

        return cell;
    }

    private void initializeStyles() {
        // Style version label
        versionLabel.setText("V 0.1.0");

        // Appliquer les ombres aux cartes
        String shadowStyle = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);";

        if (cardFormateurs != null) {
            cardFormateurs.setStyle(cardFormateurs.getStyle() + shadowStyle);
        }
        if (cardStagiaires != null) {
            cardStagiaires.setStyle(cardStagiaires.getStyle() + shadowStyle);
        }
        if (cardGroupes != null) {
            cardGroupes.setStyle(cardGroupes.getStyle() + shadowStyle);
        }
    }

    // ========== NAVIGATION CALENDRIER ==========
    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
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

        alert.showAndWait();
    }
}