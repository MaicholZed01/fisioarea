package com.fisioarea.app;

import com.fisioarea.util.SceneNavigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;

public class MainApp extends Application {

    private static final String APP_TITLE = "Fisioarea";
    private static final String APP_ICON = "/com/fisioarea/assets/fisioarea-icon.png";

    @Override
    public void start(Stage stage) {
        applyApplicationIcon(stage);
        SceneNavigator.setStage(stage);
        SceneNavigator.loadScene("/com/fisioarea/view/Login.fxml", APP_TITLE + " - Login");
        stage.setTitle(APP_TITLE);
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.centerOnScreen();
        stage.show();
    }

    private void applyApplicationIcon(Stage stage) {
        try (InputStream iconStream = getClass().getResourceAsStream(APP_ICON)) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {
            // L'icona non deve mai bloccare l'avvio del software.
        }

        applyDockOrTaskbarIcon();
    }

    private void applyDockOrTaskbarIcon() {
        try {
            System.setProperty("apple.awt.application.name", APP_TITLE);

            URL iconUrl = getClass().getResource(APP_ICON);
            if (iconUrl == null || !java.awt.Taskbar.isTaskbarSupported()) {
                return;
            }

            java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
            if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                java.awt.Image dockImage = java.awt.Toolkit.getDefaultToolkit().getImage(iconUrl);
                taskbar.setIconImage(dockImage);
            }
        } catch (Throwable ignored) {
            // Su alcuni sistemi la Taskbar API non è disponibile.
        }
    }

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", APP_TITLE);
        launch(args);
    }
}
