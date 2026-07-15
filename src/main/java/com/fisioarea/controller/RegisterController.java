package com.fisioarea.controller;

import com.fisioarea.service.AuthService;
import com.fisioarea.util.SceneNavigator;
import com.fisioarea.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField confirmEmailField;

    @FXML
    private TextField studioNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox termsCheck;

    @FXML
    private Label emailWarningLabel;

    @FXML
    private Label passwordWarningLabel;

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        roleComboBox.getItems().setAll("Admin", "Fisioterapista", "Segreteria");
        roleComboBox.getSelectionModel().select("Fisioterapista");

        hideLabel(errorLabel);
        hideLabel(emailWarningLabel);
        hideLabel(passwordWarningLabel);

        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmailMatchLive());
        confirmEmailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmailMatchLive());

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validatePasswordMatchLive());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validatePasswordMatchLive());
    }

    @FXML
    private void handleRegister() {
        String nome = firstNameField.getText().trim();
        String cognome = lastNameField.getText().trim();
        String email = ValidationUtil.normalizeEmail(emailField.getText());
        String confirmEmail = ValidationUtil.normalizeEmail(confirmEmailField.getText());
        String studio = studioNameField.getText().trim();
        String telefono = phoneField.getText().trim();
        String ruolo = roleComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!ValidationUtil.isValidEmail(email)) {
            showError("Inserisci un indirizzo email valido.");
            return;
        }

        if (!email.equalsIgnoreCase(confirmEmail)) {
            showError("Le email non coincidono.");
            showWarning(emailWarningLabel, "Le email non coincidono.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Le password non coincidono.");
            showWarning(passwordWarningLabel, "Le password non coincidono.");
            return;
        }

        AuthService.ServiceResult result = authService.register(
                nome,
                cognome,
                email,
                password,
                confirmPassword,
                studio,
                telefono,
                ruolo,
                termsCheck.isSelected()
        );

        if (result.isSuccess()) {
            clearError();
            SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
        } else {
            showError(result.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneNavigator.loadScene("/com/fisioarea/view/Login.fxml", "Fisioarea - Login");
    }

    private void validateEmailMatchLive() {
        String email = ValidationUtil.normalizeEmail(emailField.getText());
        String confirmEmail = ValidationUtil.normalizeEmail(confirmEmailField.getText());

        if (email.isBlank() || confirmEmail.isBlank()) {
            hideLabel(emailWarningLabel);
            return;
        }

        if (!email.equalsIgnoreCase(confirmEmail)) {
            showWarning(emailWarningLabel, "Le email non coincidono.");
        } else {
            hideLabel(emailWarningLabel);
        }
    }

    private void validatePasswordMatchLive() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.isBlank() || confirmPassword.isBlank()) {
            hideLabel(passwordWarningLabel);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showWarning(passwordWarningLabel, "Le password non coincidono.");
        } else {
            hideLabel(passwordWarningLabel);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        hideLabel(errorLabel);
    }

    private void showWarning(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideLabel(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }
}
