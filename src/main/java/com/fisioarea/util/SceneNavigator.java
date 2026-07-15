package com.fisioarea.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class SceneNavigator {

    private static Stage mainStage;

    private SceneNavigator() {
    }

    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    public static void loadScene(String fxmlPath, String title) {
        if (mainStage == null) {
            throw new IllegalStateException("Stage principale non impostato.");
        }

        try {
            URL resource = SceneNavigator.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IllegalArgumentException("FXML non trovato: " + fxmlPath);
            }

            Parent root = FXMLLoader.load(resource);
            Scene scene = new Scene(root);
            mainStage.setTitle(title);
            mainStage.setScene(scene);
            mainStage.centerOnScreen();

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento della scena: " + fxmlPath, e);
        }
    }
}
