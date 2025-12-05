package org.example.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StageManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void loadScene(String fxmlPath, String cssPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        if (cssPath != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(StageManager.class.getResource(cssPath).toExternalForm());
        }

        primaryStage.setTitle(title);
    }
}