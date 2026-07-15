package com.fisioarea.controller;

import com.fisioarea.model.User;
import com.fisioarea.service.AuthService;
import com.fisioarea.service.UserManagementService;
import com.fisioarea.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminUsersController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    private final UserManagementService userManagementService = new UserManagementService();

    @FXML
    private void initialize() {
        if (!userManagementService.isCurrentUserAdmin()) {
            showError("Accesso negato", "Solo un profilo Admin può gestire gli altri utenti.");
            SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
            return;
        }

        configureTable();
        configureSelection();
        configureSearch();
        loadUsers();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nomeCompleto"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("ruolo"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
    }

    private void configureSelection() {
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedUser) -> {
            boolean hasSelection = selectedUser != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });
    }

    private void configureSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadUsers());
    }

    private void loadUsers() {
        String search = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        List<User> users = userManagementService.getManagedUsers().stream()
                .filter(user -> search.isBlank()
                        || user.getNomeCompleto().toLowerCase().contains(search)
                        || user.getEmail().toLowerCase().contains(search)
                        || user.getRuolo().toLowerCase().contains(search)
                        || user.getTelefono().toLowerCase().contains(search))
                .toList();

        usersTable.setItems(FXCollections.observableArrayList(users));
        setStatus("Profili totali: " + users.size(), false);
    }

    @FXML
    private void handleAddUser() {
        openUserDialog(null);
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            setStatus("Seleziona un profilo da modificare.", true);
            return;
        }

        openUserDialog(selectedUser);
    }


    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showError("Nessun profilo selezionato", "Seleziona un profilo dalla tabella prima di eliminarlo.");
            return;
        }

        showDeleteUserDialog(selectedUser);
    }


    @FXML
    private void handleBackToDashboard() {
        SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
    }


    private void openUserDialog(User userToEdit) {
        boolean editMode = userToEdit != null;

        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle(editMode ? "Modifica profilo" : "Nuovo profilo");
        dialog.getDialogPane().getStyleClass().add("register-style-dialog-pane");
        dialog.getDialogPane().setPrefWidth(1040);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinWidth(1040);
        dialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(dialog);

        TextField firstNameField = textField("Nome");
        TextField lastNameField = textField("Cognome");
        TextField emailField = textField("email@studio.it");
        TextField studioNameField = textField("Nome studio");
        TextField phoneField = textField("+39 000 000 0000");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().setAll(userManagementService.getAssignableRoles());
        roleComboBox.getSelectionModel().select("Fisioterapista");
        roleComboBox.setMaxWidth(Double.MAX_VALUE);
        roleComboBox.getStyleClass().add("input-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(editMode ? "Lascia vuoto per non cambiarla" : "Password");
        passwordField.getStyleClass().add("input-field");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText(editMode ? "Conferma nuova password" : "Conferma password");
        confirmPasswordField.getStyleClass().add("input-field");

        if (editMode) {
            firstNameField.setText(userToEdit.getNome());
            lastNameField.setText(userToEdit.getCognome());
            emailField.setText(userToEdit.getEmail());
            studioNameField.setText(userToEdit.getNomeStudio());
            phoneField.setText(userToEdit.getTelefono());
            roleComboBox.getSelectionModel().select(userToEdit.getRuolo());

            if (roleComboBox.getValue() == null) {
                roleComboBox.getSelectionModel().select("Fisioterapista");
            }
        }

        VBox hero = registerStyleHero(
                "Gestione utenti",
                editMode ? "Modifica profilo" : "Nuovo profilo",
                editMode
                        ? "Aggiorna dati, ruolo e password del profilo selezionato."
                        : "Crea un nuovo accesso per lo studio e assegna il ruolo corretto.",
                List.of(
                        "Inserisci dati anagrafici",
                        "Imposta ruolo e contatti",
                        "Salva credenziali sicure"
                )
        );

        VBox accountFields = formStack(
                formRow(field("Nome", firstNameField), field("Cognome", lastNameField)),
                fullRow(field("Email", emailField)),
                formRow(field("Studio", studioNameField), field("Telefono", phoneField)),
                fullRow(field("Ruolo", roleComboBox))
        );

        VBox passwordFields = formStack(
                formRow(field(editMode ? "Nuova password" : "Password", passwordField),
                        field("Conferma password", confirmPasswordField)),
                helperText(editMode
                        ? "Lascia vuoti i campi password se non vuoi modificarla. Solo un Admin può modificare i ruoli."
                        : "La password deve contenere almeno 8 caratteri, una lettera e un numero.")
        );

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> closeDialog(dialog));

        Button saveButton = new Button(editMode ? "Salva modifiche" : "Crea profilo");
        saveButton.getStyleClass().add("primary-button-compact");
        saveButton.setDefaultButton(true);

        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.getStyleClass().add("register-dialog-actions");
        actions.getChildren().addAll(cancelButton, saveButton);

        VBox formPanel = new VBox(14);
        formPanel.getStyleClass().add("register-dialog-form-panel");
        formPanel.getChildren().addAll(
                registerStyleFormHeader(
                        editMode ? "Modifica profilo utente" : "Crea profilo utente",
                        "Gestisci i dati dell'account e i permessi di accesso allo studio."
                ),
                registerStyleCard("Dati account", accountFields),
                registerStyleCard("Credenziali", passwordFields),
                actions
        );
        HBox.setHgrow(formPanel, Priority.ALWAYS);

        HBox layout = new HBox(0, hero, formPanel);
        layout.getStyleClass().add("register-dialog-layout");
        layout.setFillHeight(true);
        layout.setMinHeight(Region.USE_PREF_SIZE);
        layout.setPrefHeight(Region.USE_COMPUTED_SIZE);
        hero.setMaxHeight(Double.MAX_VALUE);
        formPanel.setMaxHeight(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(layout);

        saveButton.setOnAction(event -> {
            AuthService.ServiceResult result;

            if (editMode) {
                result = userManagementService.updateUser(
                        userToEdit.getId(),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        emailField.getText(),
                        studioNameField.getText(),
                        phoneField.getText(),
                        roleComboBox.getValue(),
                        passwordField.getText(),
                        confirmPasswordField.getText()
                );
            } else {
                result = userManagementService.createUser(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        confirmPasswordField.getText(),
                        studioNameField.getText(),
                        phoneField.getText(),
                        roleComboBox.getValue()
                );
            }

            if (result.isSuccess()) {
                loadUsers();
                setStatus(result.getMessage(), false);
                closeDialog(dialog);
            } else {
                setStatus(result.getMessage(), true);
                showError("Dati non validi", result.getMessage());
            }
        });

        dialog.showAndWait();
    }

    private TextField textField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("input-field");
        return textField;
    }

    private VBox field(String label, Node input) {
        VBox box = new VBox(7);
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");
        box.getChildren().addAll(fieldLabel, input);
        return box;
    }

    private VBox formStack(Node... rows) {
        VBox stack = new VBox(12);
        stack.getStyleClass().add("register-form-stack");
        stack.getChildren().addAll(rows);
        return stack;
    }

    private HBox formRow(Node left, Node right) {
        HBox row = new HBox(14);
        row.getStyleClass().add("register-form-row");
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        row.getChildren().addAll(left, right);
        return row;
    }

    private HBox fullRow(Node node) {
        HBox row = new HBox();
        row.getStyleClass().add("register-form-row");
        HBox.setHgrow(node, Priority.ALWAYS);
        row.getChildren().add(node);
        return row;
    }

    private Label helperText(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("modern-helper-text");
        return label;
    }

    private VBox registerStyleHero(String section, String title, String description, List<String> steps) {
        VBox hero = new VBox(22);
        hero.getStyleClass().add("register-dialog-hero");
        hero.setMaxHeight(Double.MAX_VALUE);

        HBox brand = new HBox(12);
        brand.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label brandMark = new Label("F");
        brandMark.getStyleClass().add("patient-sheet-brand-mark");

        VBox brandTexts = new VBox(2);
        Label brandTitle = new Label("Fisioarea");
        brandTitle.getStyleClass().add("patient-sheet-brand-title");
        Label brandSubtitle = new Label(section);
        brandSubtitle.getStyleClass().add("patient-sheet-brand-subtitle");
        brandTexts.getChildren().addAll(brandTitle, brandSubtitle);

        brand.getChildren().addAll(brandMark, brandTexts);

        Region topSpace = new Region();
        topSpace.setMinHeight(70);

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("patient-sheet-left-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("patient-sheet-left-description");

        VBox stepsBox = new VBox(12);
        for (int i = 0; i < steps.size(); i++) {
            stepsBox.getChildren().add(registerStyleStep(String.valueOf(i + 1), steps.get(i)));
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label footer = new Label("Le modifiche sono salvate offline nella cartella FisioareaData.");
        footer.setWrapText(true);
        footer.getStyleClass().add("patient-sheet-left-footer");

        hero.getChildren().addAll(brand, topSpace, titleLabel, descriptionLabel, stepsBox, spacer, footer);
        return hero;
    }

    private HBox registerStyleStep(String number, String text) {
        HBox step = new HBox(10);
        step.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        step.getStyleClass().add("patient-sheet-step");

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("patient-sheet-step-number");

        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.getStyleClass().add("patient-sheet-step-text");

        step.getChildren().addAll(numberLabel, textLabel);
        return step;
    }

    private VBox registerStyleFormHeader(String title, String subtitle) {
        VBox header = new VBox(4);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("register-style-form-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.getStyleClass().add("register-style-form-subtitle");

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private VBox registerStyleCard(String title, Node content) {
        VBox card = new VBox(14);
        card.getStyleClass().add("register-style-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("register-style-card-title");

        card.getChildren().addAll(titleLabel, content);
        return card;
    }

    private void applyDialogStyles(Dialog<?> dialog) {
        String stylesheet = getClass().getResource("/com/fisioarea/view/styles.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().clear();
        dialog.getDialogPane().getStylesheets().add(stylesheet);
        dialog.getDialogPane().getStyleClass().add("modern-dialog-pane");
    }

    private void enableWindowClose(Dialog<?> dialog) {
        if (!dialog.getDialogPane().getButtonTypes().contains(ButtonType.CLOSE)) {
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        }

        Node closeNode = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);

        if (closeNode != null) {
            closeNode.setVisible(false);
            closeNode.setManaged(false);
            closeNode.setDisable(false);
        }

        dialog.setOnCloseRequest(event -> dialog.setResult(null));

        dialog.setOnShown(event -> Platform.runLater(() -> {
            hideDialogButtonBar(dialog);
            dialog.getDialogPane().applyCss();
            dialog.getDialogPane().layout();

            if (dialog.getDialogPane().getScene() != null
                    && dialog.getDialogPane().getScene().getWindow() instanceof Stage stage) {
                stage.sizeToScene();
                stage.centerOnScreen();
            }
        }));
    }

    private void hideDialogButtonBar(Dialog<?> dialog) {
        Node buttonBar = dialog.getDialogPane().lookup(".button-bar");

        if (buttonBar != null) {
            buttonBar.setVisible(false);
            buttonBar.setManaged(false);
            buttonBar.setDisable(true);
            buttonBar.setOpacity(0);
        }

        Node closeNode = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);

        if (closeNode != null) {
            closeNode.setVisible(false);
            closeNode.setManaged(false);
            closeNode.setDisable(false);
            closeNode.setOpacity(0);
        }
    }

    private void closeDialog(Dialog<?> dialog) {
        dialog.setResult(null);
        dialog.close();
    }


    private void showDeleteUserDialog(User selectedUser) {
        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle("Elimina profilo");
        dialog.getDialogPane().getStyleClass().add("register-style-dialog-pane");
        dialog.getDialogPane().setPrefWidth(1040);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinWidth(1040);
        dialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(dialog);

        VBox hero = registerStyleHero(
                "Gestione utenti",
                "Elimina profilo",
                "Conferma la rimozione del profilo selezionato. L'elenco utenti verrà aggiornato nel file locale.",
                List.of(
                        "Controlla il profilo",
                        "Conferma eliminazione",
                        "Aggiorna la lista utenti"
                )
        );

        VBox summary = formStack(
                formRow(
                        field("Nome", confirmationValue(selectedUser.getNomeCompleto())),
                        field("Ruolo", confirmationValue(selectedUser.getRuolo()))
                ),
                fullRow(field("Email", confirmationValue(selectedUser.getEmail()))),
                fullRow(confirmationWarning(
                        "Vuoi eliminare definitivamente questo profilo? L'operazione non può essere annullata."
                ))
        );

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> closeDialog(dialog));

        Button deleteButton = new Button("Elimina profilo");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setDefaultButton(true);
        deleteButton.setOnAction(event -> {
            AuthService.ServiceResult result = userManagementService.deleteUser(selectedUser.getId());

            if (result.isSuccess()) {
                loadUsers();
                setStatus(result.getMessage(), false);
                closeDialog(dialog);
            } else {
                setStatus(result.getMessage(), true);
                showError("Eliminazione non riuscita", result.getMessage());
            }
        });

        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.getStyleClass().add("register-dialog-actions");
        actions.getChildren().addAll(cancelButton, deleteButton);

        VBox formPanel = new VBox(14);
        formPanel.getStyleClass().add("register-dialog-form-panel");
        formPanel.getChildren().addAll(
                registerStyleFormHeader(
                        "Conferma eliminazione",
                        "Prima di procedere verifica il profilo selezionato."
                ),
                registerStyleCard("Riepilogo profilo", summary),
                actions
        );
        HBox.setHgrow(formPanel, Priority.ALWAYS);

        HBox layout = new HBox(0, hero, formPanel);
        layout.getStyleClass().add("register-dialog-layout");
        layout.setFillHeight(true);
        layout.setMinHeight(Region.USE_PREF_SIZE);
        layout.setPrefHeight(Region.USE_COMPUTED_SIZE);
        hero.setMaxHeight(Double.MAX_VALUE);
        formPanel.setMaxHeight(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(layout);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private Label confirmationValue(String value) {
        Label label = new Label(value == null || value.isBlank() ? "—" : value);
        label.setWrapText(true);
        label.getStyleClass().add("confirmation-detail-value");
        return label;
    }

    private Label confirmationWarning(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add("confirmation-warning-box");
        return label;
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success-label", "error-text");

        if (error) {
            statusLabel.getStyleClass().add("error-text");
        } else {
            statusLabel.getStyleClass().add("success-label");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fisioarea");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
