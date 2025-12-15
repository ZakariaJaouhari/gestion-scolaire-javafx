package org.example.controller;

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
import java.util.Locale;
import java.io.IOException;

import javafx.stage.Modality;
import javafx.stage.Stage;
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
        // Données d'exemple (remplacer par vos vraies données)
        nombreFormateursLabel.setText("12");
        nombreStagiairesLabel.setText("150");
        nombreGroupesLabel.setText("8");
        nombreTotalGroupesLabel.setText("8");

        totalHommesLabel.setText("85");
        totalFemmesLabel.setText("65");
    }

    @SuppressWarnings("unchecked")
    private void initializeChart() {
        try {
            // Configuration du graphique
            barChart.setTitle("");
            barChart.setLegendVisible(false);
            barChart.setAnimated(true);

            // Cacher les axes
            xAxis.setTickLabelsVisible(false);
            xAxis.setTickMarkVisible(false);
            xAxis.setOpacity(0);

            yAxis.setTickLabelsVisible(false);
            yAxis.setTickMarkVisible(false);
            yAxis.setOpacity(0);

            // Données d'exemple pour les groupes
            String[] groupes = {"Groupe 1", "Groupe 2", "Groupe 3", "Groupe 4", "Groupe 5"};
            int[] hommes = {20, 15, 25, 18, 7};
            int[] femmes = {15, 20, 10, 12, 18};
            int[] modules = {6, 5, 7, 5, 4};
            int[] formateurs = {2, 3, 2, 1, 4};

            // Créer les séries
            XYChart.Series<String, Number> seriesHommes = new XYChart.Series<>();
            seriesHommes.setName("Hommes");

            XYChart.Series<String, Number> seriesFemmes = new XYChart.Series<>();
            seriesFemmes.setName("Femmes");

            XYChart.Series<String, Number> seriesModules = new XYChart.Series<>();
            seriesModules.setName("Modules");

            XYChart.Series<String, Number> seriesFormateurs = new XYChart.Series<>();
            seriesFormateurs.setName("Formateurs");

            // Ajouter les données
            for (int i = 0; i < groupes.length; i++) {
                seriesHommes.getData().add(new XYChart.Data<>(groupes[i], hommes[i]));
                seriesFemmes.getData().add(new XYChart.Data<>(groupes[i], femmes[i]));
                seriesModules.getData().add(new XYChart.Data<>(groupes[i], modules[i]));
                seriesFormateurs.getData().add(new XYChart.Data<>(groupes[i], formateurs[i]));
            }

            // Ajouter les séries au graphique
            barChart.getData().addAll(seriesHommes, seriesFemmes, seriesModules, seriesFormateurs);

            // Appliquer les couleurs après le rendu
            applyChartColors();

            System.out.println("✅ Graphique initialisé avec 4 séries");

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation graphique: " + e.getMessage());
        }
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
        System.out.println("Navigation: Home");
        // Déjà sur la page home
    }

    @FXML
    private void handleFormateurs() {
        try {
            StageManager.loadScene("/view/directeur/Gestion Formateurs/gestionFormateurs.fxml", "/styles/gestionFormateurs.css", "gesttion des Formateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStagiaires() {
        try {
            StageManager.loadScene("/view/directeur/gestionEtudiants.fxml", "/styles/gestionFormateurs.css", "gestion des Etudiants");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGroupes() {
        showAlert("Groupes", "Gestion des groupes",
                "Cette fonctionnalité sera disponible prochainement.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleModules() {
        showAlert("Modules", "Gestion des modules",
                "Cette fonctionnalité sera disponible prochainement.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleNotes() {
        showAlert("Notes", "Gestion des notes",
                "Cette fonctionnalité sera disponible prochainement.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCertificats() {
        showAlert("Certificats", "Gestion des certificats",
                "Cette fonctionnalité sera disponible prochainement.",
                Alert.AlertType.INFORMATION);
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