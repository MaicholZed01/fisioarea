package com.fisioarea.controller;

import com.fisioarea.service.AuthService;
import com.fisioarea.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheck;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Optional<String> rememberedEmail = authService.getRememberedEmail();
        rememberedEmail.ifPresent(emailField::setText);
        rememberMeCheck.setSelected(rememberedEmail.isPresent());

        emailField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> handleLogin());

        Platform.runLater(() -> {
            if (authService.restoreRememberedSession()) {
                SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
            }
        });
    }

    @FXML
    private void handleLogin() {
        AuthService.ServiceResult result = authService.login(
                emailField.getText(),
                passwordField.getText(),
                rememberMeCheck.isSelected()
        );

        if (result.isSuccess()) {
            clearError();
            SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
        } else {
            showError(result.getMessage());
        }
    }

    @FXML
    private void goToRegister() {
        SceneNavigator.loadScene("/com/fisioarea/view/Register.fxml", "Fisioarea - Registrazione");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
