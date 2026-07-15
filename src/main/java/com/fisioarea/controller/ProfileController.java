package com.fisioarea.controller;

import com.fisioarea.model.User;
import com.fisioarea.service.AuthService;
import com.fisioarea.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProfileController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField studioNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label roleValueLabel;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmNewPasswordField;

    @FXML
    private Label profileMessageLabel;

    @FXML
    private Label passwordMessageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        hideLabel(profileMessageLabel);
        hideLabel(passwordMessageLabel);
        loadCurrentUser();
    }

    @FXML
    private void handleSaveProfile() {
        AuthService.ServiceResult result = authService.updateCurrentUserProfile(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                studioNameField.getText(),
                phoneField.getText()
        );

        showResult(profileMessageLabel, result);
        loadCurrentUser();
    }

    @FXML
    private void handleChangePassword() {
        AuthService.ServiceResult result = authService.changeCurrentUserPassword(
                currentPasswordField.getText(),
                newPasswordField.getText(),
                confirmNewPasswordField.getText()
        );

        if (result.isSuccess()) {
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();
        }

        showResult(passwordMessageLabel, result);
    }

    @FXML
    private void handleBackToDashboard() {
        SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneNavigator.loadScene("/com/fisioarea/view/Login.fxml", "Fisioarea - Login");
    }

    private void loadCurrentUser() {
        User currentUser = AuthService.getCurrentUser();

        if (currentUser == null) {
            SceneNavigator.loadScene("/com/fisioarea/view/Login.fxml", "Fisioarea - Login");
            return;
        }

        firstNameField.setText(currentUser.getNome());
        lastNameField.setText(currentUser.getCognome());
        emailField.setText(currentUser.getEmail());
        studioNameField.setText(currentUser.getNomeStudio());
        phoneField.setText(currentUser.getTelefono());
        roleValueLabel.setText(currentUser.getRuolo());
    }

    private void showResult(Label label, AuthService.ServiceResult result) {
        label.setText(result.getMessage());
        label.getStyleClass().removeAll("success-label", "error-label");
        label.getStyleClass().add(result.isSuccess() ? "success-label" : "error-label");
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideLabel(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }
}
