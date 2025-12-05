package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.util.StageManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Enregistrer le stage principal
        StageManager.setPrimaryStage(stage);

        // Charger la première scène
        StageManager.loadScene("/view/login_register.fxml", "/styles/auth.css", "Gestion Scolaire");

        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}