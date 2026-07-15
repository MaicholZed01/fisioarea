package com.fisioarea.controller;

import com.fisioarea.model.Paziente;
import com.fisioarea.service.PazienteService;
import com.fisioarea.util.PatientFileStorage;
import com.fisioarea.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.stage.Stage;

public class PatientsController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Paziente> patientsTable;

    @FXML
    private TableColumn<Paziente, Integer> idColumn;

    @FXML
    private TableColumn<Paziente, String> nameColumn;

    @FXML
    private TableColumn<Paziente, String> phoneColumn;

    @FXML
    private TableColumn<Paziente, String> emailColumn;

    @FXML
    private TableColumn<Paziente, String> addressColumn;

    @FXML
    private TableColumn<Paziente, String> filesColumn;

    @FXML
    private ListView<String> patientFilesList;

    @FXML
    private Label selectedPatientLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button openFileButton;

    private final PazienteService pazienteService = new PazienteService();

    @FXML
    private void initialize() {
        configureTable();
        configureSelection();
        configureSearch();
        loadPatients();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nomeCompleto"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("indirizzoCasa"));
        filesColumn.setCellValueFactory(new PropertyValueFactory<>("fileCountLabel"));

        if (patientFilesList != null) {
            patientFilesList.setCellFactory(listView -> new ListCell<>() {
                @Override
                protected void updateItem(String filePath, boolean empty) {
                    super.updateItem(filePath, empty);

                    if (empty || filePath == null || filePath.isBlank()) {
                        setText(null);
                    } else {
                        setText(PatientFileStorage.getDisplayName(filePath));
                    }
                }
            });
        }

        patientsTable.setRowFactory(tableView -> {
            TableRow<Paziente> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()
                        && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 1) {
                    showPatientSheet(row.getItem());
                }
            });

            return row;
        });
    }

    private void configureSelection() {
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        if (openFileButton != null) {
            openFileButton.setDisable(true);
        }

        patientsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedPatient) -> {
            boolean hasSelection = selectedPatient != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            renderPatientFiles(selectedPatient);
        });

        if (patientFilesList != null && openFileButton != null) {
            patientFilesList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedFile) -> {
                openFileButton.setDisable(selectedFile == null);
            });
        }
    }

    private void configureSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadPatients());
    }

    private void loadPatients() {
        String search = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        List<Paziente> patients = pazienteService.getPazienti().stream()
                .filter(paziente -> search.isBlank()
                        || paziente.getNomeCompleto().toLowerCase().contains(search)
                        || paziente.getTelefono().toLowerCase().contains(search)
                        || paziente.getEmail().toLowerCase().contains(search)
                        || paziente.getIndirizzoCasa().toLowerCase().contains(search)
                        || paziente.getDiagnosi().toLowerCase().contains(search)
                        || paziente.getProfessione().toLowerCase().contains(search))
                .toList();

        patientsTable.setItems(FXCollections.observableArrayList(patients));
        setStatus("Pazienti: " + patients.size(), false);
        renderPatientFiles(patientsTable.getSelectionModel().getSelectedItem());
    }


    private void renderPatientFiles(Paziente paziente) {
        if (patientFilesList == null) {
            if (selectedPatientLabel != null) {
                selectedPatientLabel.setText(paziente == null
                        ? "Nessun paziente selezionato"
                        : paziente.getNomeCompleto());
            }

            if (openFileButton != null) {
                openFileButton.setDisable(true);
            }

            return;
        }

        patientFilesList.getItems().clear();

        if (paziente == null) {
            if (selectedPatientLabel != null) {
                selectedPatientLabel.setText("Nessun paziente selezionato");
            }

            if (openFileButton != null) {
                openFileButton.setDisable(true);
            }

            return;
        }

        if (selectedPatientLabel != null) {
            selectedPatientLabel.setText(paziente.getNomeCompleto() + " • " + paziente.getFileCountLabel());
        }

        for (String filePath : paziente.getListaFileAllegati()) {
            patientFilesList.getItems().add(filePath);
        }

        if (openFileButton != null) {
            openFileButton.setDisable(patientFilesList.getSelectionModel().getSelectedItem() == null);
        }
    }

    @FXML
    private void handleAddPatient() {
        openPatientDialog(null);
    }

    @FXML
    private void handleEditPatient() {
        Paziente selectedPatient = patientsTable.getSelectionModel().getSelectedItem();

        if (selectedPatient == null) {
            setStatus("Seleziona un paziente da modificare.", true);
            return;
        }

        openPatientDialog(selectedPatient);
    }

    @FXML
    private void handleDeletePatient() {
        Paziente selectedPatient = patientsTable.getSelectionModel().getSelectedItem();

        if (selectedPatient == null) {
            setStatus("Seleziona un paziente da eliminare.", true);
            return;
        }

        showDeletePatientDialog(selectedPatient);
    }

    @FXML
    private void handleOpenSelectedFile() {
        if (patientFilesList == null) {
            setStatus("La sezione file paziente non è presente in questa schermata.", true);
            return;
        }

        openFile(patientFilesList.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleBackToDashboard() {
        SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
    }


    private void openPatientDialog(Paziente patientToEdit) {
        boolean editMode = patientToEdit != null;

        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle(editMode ? "Modifica scheda paziente" : "Nuova scheda paziente");
        dialog.getDialogPane().getStyleClass().add("register-style-dialog-pane");
        dialog.getDialogPane().setPrefWidth(1120);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinWidth(1120);
        dialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(dialog);

        TextField firstNameField = textField("Nome");
        TextField lastNameField = textField("Cognome");
        TextField ageField = textField("Età");
        TextField professionField = textField("Professione");
        TextField phoneField = textField("+39 000 000 0000");
        TextField emailField = textField("email@paziente.it");
        TextField addressField = textField("Via, numero civico, città");

        TextArea diagnosisArea = textArea("Diagnosi", 3);
        TextArea muscleStrengthArea = textArea("Forza muscolare", 3);

        ComboBox<String> natureComboBox = new ComboBox<>();
        natureComboBox.getItems().setAll("Articolare", "Muscolare", "Articolare e muscolare", "Neurologica", "Posturale", "Altro");
        natureComboBox.setPromptText("Seleziona natura problema");
        natureComboBox.setMaxWidth(Double.MAX_VALUE);
        natureComboBox.getStyleClass().add("input-field");

        TextArea instrumentalExamsArea = textArea("Esami strumentali in visione, TAC, RM, RX, ecografie...", 3);
        TextArea coreArea = textArea("Core", 3);
        TextArea remoteAnamnesisArea = textArea("Anamnesi remota", 3);
        TextArea recentAnamnesisArea = textArea("Anamnesi prossima", 3);
        TextArea therapiesArea = textArea("Terapie svolte", 3);
        TextArea specificTestsArea = textArea("Elenco dei test specifici somministrati", 6);
        TextArea performanceMeasurementArea = textArea("Misurazione prestazione", 3);
        TextArea balanceEvaluationArea = textArea("Valutazione funzionale dell'equilibrio", 3);
        TextArea romEvaluationArea = textArea("ROM / AAROM / PROM", 3);
        TextArea muscleElasticityArea = textArea("Elasticità muscolare", 3);
        TextArea posturalEvaluationArea = textArea("Valutazione posturale", 3);
        TextArea notesArea = textArea("Note libere", 5);

        if (editMode) {
            firstNameField.setText(patientToEdit.getNome());
            lastNameField.setText(patientToEdit.getCognome());
            ageField.setText(patientToEdit.getEta());
            professionField.setText(patientToEdit.getProfessione());
            phoneField.setText(patientToEdit.getTelefono());
            emailField.setText(patientToEdit.getEmail());
            addressField.setText(patientToEdit.getIndirizzoCasa());
            diagnosisArea.setText(patientToEdit.getDiagnosi());
            muscleStrengthArea.setText(patientToEdit.getForzaMuscolare());
            natureComboBox.getSelectionModel().select(patientToEdit.getNaturaProblema());
            instrumentalExamsArea.setText(patientToEdit.getEsamiStrumentaliInVisione());
            coreArea.setText(patientToEdit.getCore());
            remoteAnamnesisArea.setText(patientToEdit.getAnamnesiRemota());
            recentAnamnesisArea.setText(patientToEdit.getAnamnesiProssima());
            therapiesArea.setText(patientToEdit.getTerapieSvolte());
            specificTestsArea.setText(patientToEdit.getTestSpecificiSomministrati());
            performanceMeasurementArea.setText(patientToEdit.getMisurazionePrestazione());
            balanceEvaluationArea.setText(patientToEdit.getValutazioneEquilibrio());
            romEvaluationArea.setText(patientToEdit.getValutazioneRomAaromProm());
            muscleElasticityArea.setText(patientToEdit.getValutazioneElasticitaMuscolare());
            posturalEvaluationArea.setText(patientToEdit.getValutazionePosturale());
            notesArea.setText(patientToEdit.getNote());
        }

        List<File> selectedFiles = new ArrayList<>();

        VBox hero = registerStyleHero(
                "Scheda paziente",
                editMode ? "Aggiorna scheda clinica" : "Nuova scheda clinica",
                "Un percorso guidato per raccogliere anagrafica, quadro clinico, test e valutazioni.",
                List.of(
                        "Compila i dati anagrafici",
                        "Registra valutazione e anamnesi",
                        "Salva test, note e misurazioni"
                )
        );

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("register-form-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab anagraficaTab = new Tab("Anagrafica");
        anagraficaTab.setContent(tabContent(
                registerStyleCard("Dati principali", formStack(
                        formRow(field("Nome", firstNameField), field("Cognome", lastNameField)),
                        formRow(field("Età", ageField), field("Professione", professionField)),
                        formRow(field("Telefono", phoneField), field("Email", emailField)),
                        fullRow(field("Indirizzo casa", addressField))
                ))
        ));

        Tab valutazioneTab = new Tab("Valutazione");
        valutazioneTab.setContent(tabContent(
                registerStyleCard("Quadro clinico", formStack(
                        fullRow(field("Diagnosi", diagnosisArea)),
                        formRow(field("Forza muscolare", muscleStrengthArea), field("Natura problema", natureComboBox)),
                        formRow(field("Esami strumentali in visione", instrumentalExamsArea), field("Core", coreArea))
                ))
        ));

        Tab anamnesiTab = new Tab("Anamnesi");
        anamnesiTab.setContent(tabContent(
                registerStyleCard("Anamnesi e terapie", formStack(
                        formRow(field("Anamnesi remota", remoteAnamnesisArea), field("Anamnesi prossima", recentAnamnesisArea)),
                        fullRow(field("Terapie svolte", therapiesArea))
                ))
        ));

        Tab testTab = new Tab("Test");
        testTab.setContent(tabContent(
                registerStyleCard("Test e misurazioni", formStack(
                        fullRow(field("Test specifici somministrati", specificTestsArea)),
                        fullRow(field("Misurazione prestazione", performanceMeasurementArea))
                ))
        ));

        Tab funzionaleTab = new Tab("Funzionale");
        funzionaleTab.setContent(tabContent(
                registerStyleCard("Valutazioni funzionali", formStack(
                        formRow(field("Equilibrio", balanceEvaluationArea), field("ROM / AAROM / PROM", romEvaluationArea)),
                        formRow(field("Elasticità muscolare", muscleElasticityArea), field("Valutazione posturale", posturalEvaluationArea)),
                        fullRow(field("Note", notesArea))
                ))
        ));

        tabs.getTabs().addAll(anagraficaTab, valutazioneTab, anamnesiTab, testTab, funzionaleTab);

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> closeDialog(dialog));

        Button saveButton = new Button(editMode ? "Salva modifiche" : "Crea paziente");
        saveButton.getStyleClass().add("primary-button-compact");
        saveButton.setDefaultButton(true);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("register-dialog-actions");
        actions.getChildren().addAll(cancelButton, saveButton);

        VBox formPanel = new VBox(14);
        formPanel.getStyleClass().add("register-dialog-form-panel");
        formPanel.getChildren().addAll(
                registerStyleFormHeader(
                        editMode ? "Modifica dati paziente" : "Crea scheda paziente",
                        "Le sezioni sono organizzate come un percorso di valutazione fisioterapica."
                ),
                tabs,
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
            PazienteService.PatientClinicalData data = new PazienteService.PatientClinicalData(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    phoneField.getText(),
                    emailField.getText(),
                    addressField.getText(),
                    ageField.getText(),
                    professionField.getText(),
                    diagnosisArea.getText(),
                    muscleStrengthArea.getText(),
                    natureComboBox.getValue(),
                    instrumentalExamsArea.getText(),
                    coreArea.getText(),
                    remoteAnamnesisArea.getText(),
                    recentAnamnesisArea.getText(),
                    therapiesArea.getText(),
                    specificTestsArea.getText(),
                    performanceMeasurementArea.getText(),
                    balanceEvaluationArea.getText(),
                    romEvaluationArea.getText(),
                    muscleElasticityArea.getText(),
                    posturalEvaluationArea.getText(),
                    notesArea.getText()
            );

            PazienteService.ServiceResult<Paziente> result;

            if (editMode) {
                result = pazienteService.aggiornaPaziente(patientToEdit, data, selectedFiles);
            } else {
                result = pazienteService.creaPaziente(data, selectedFiles);
            }

            if (result.isSuccess()) {
                loadPatients();
                setStatus(result.getMessage(), false);
                closeDialog(dialog);
            } else {
                setStatus(result.getMessage(), true);
                showError("Dati non validi", result.getMessage());
            }
        });

        dialog.showAndWait();
    }

    private VBox formStack(Node... rows) {
        VBox stack = new VBox(14);
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

    private VBox tabContent(Node... nodes) {
        VBox content = new VBox(16);
        content.setPadding(new Insets(18));
        content.getChildren().addAll(nodes);
        content.getStyleClass().add("register-tab-content");
        return content;
    }


    private void showPatientSheet(Paziente patient) {
        if (patient == null) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle("Scheda paziente");
        dialog.getDialogPane().setPrefWidth(1120);
        dialog.getDialogPane().setPrefHeight(690);
        dialog.getDialogPane().setMinWidth(1120);

        VBox leftPanel = new VBox(22);
        leftPanel.getStyleClass().add("patient-sheet-left-panel");
        leftPanel.setFillWidth(true);
        leftPanel.setPrefWidth(340);
        leftPanel.setMinWidth(340);
        leftPanel.setMaxWidth(340);

        HBox brand = new HBox(12);
        brand.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label brandMark = new Label("F");
        brandMark.getStyleClass().add("patient-sheet-brand-mark");

        VBox brandTexts = new VBox(2);
        Label brandTitle = new Label("Fisioarea");
        brandTitle.getStyleClass().add("patient-sheet-brand-title");
        Label brandSubtitle = new Label("Scheda paziente");
        brandSubtitle.getStyleClass().add("patient-sheet-brand-subtitle");
        brandTexts.getChildren().addAll(brandTitle, brandSubtitle);

        brand.getChildren().addAll(brandMark, brandTexts);

        Region leftTopSpace = new Region();
        leftTopSpace.setMinHeight(96);

        Label patientName = new Label(patient.getNomeCompleto());
        patientName.setWrapText(true);
        patientName.getStyleClass().add("patient-sheet-left-title");

        Label patientDescription = new Label("Riepilogo clinico completo con anagrafica, diagnosi, valutazioni e file allegati.");
        patientDescription.setWrapText(true);
        patientDescription.getStyleClass().add("patient-sheet-left-description");

        VBox steps = new VBox(12);
        steps.getChildren().addAll(
                patientSheetStep("1", "Consulta i dati principali"),
                patientSheetStep("2", "Rivedi test e valutazioni"),
                patientSheetStep("3", "Apri i documenti clinici")
        );

        Region leftSpacer = new Region();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);

        Label offlineFooter = new Label("I dati sono salvati offline nella cartella FisioareaData.");
        offlineFooter.setWrapText(true);
        offlineFooter.getStyleClass().add("patient-sheet-left-footer");

        leftPanel.getChildren().addAll(
                brand,
                leftTopSpace,
                patientName,
                patientDescription,
                steps,
                leftSpacer,
                offlineFooter
        );

        VBox mainContent = new VBox(18);
        mainContent.getStyleClass().add("patient-sheet-main-content");
        mainContent.setFillWidth(true);

        HBox topBar = new HBox(16);
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox heading = new VBox(4);
        Label title = new Label("Scheda clinica");
        title.getStyleClass().add("patient-sheet-title");
        Label subtitle = new Label("Vista completa delle informazioni raccolte per il paziente.");
        subtitle.getStyleClass().add("patient-sheet-subtitle");
        heading.getChildren().addAll(title, subtitle);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button closeButton = new Button("Chiudi");
        closeButton.getStyleClass().add("patient-sheet-close-button");
        closeButton.setOnAction(event -> closeDialog(dialog));

        topBar.getChildren().addAll(heading, topSpacer, closeButton);

        VBox sheetSections = new VBox(16);
        sheetSections.getChildren().addAll(
                sheetSection("Anagrafica",
                        sheetLine("Nome e cognome", patient.getNomeCompleto()),
                        sheetLine("Età", patient.getEta()),
                        sheetLine("Professione", patient.getProfessione()),
                        sheetLine("Telefono", patient.getTelefono()),
                        sheetLine("Email", patient.getEmail()),
                        sheetLine("Indirizzo casa", patient.getIndirizzoCasa())
                ),
                sheetSection("Quadro clinico",
                        sheetLine("Diagnosi", patient.getDiagnosi()),
                        sheetLine("Forza muscolare", patient.getForzaMuscolare()),
                        sheetLine("Natura problema", patient.getNaturaProblema()),
                        sheetLine("Esami strumentali in visione", patient.getEsamiStrumentaliInVisione()),
                        sheetLine("Core", patient.getCore())
                ),
                sheetSection("Anamnesi e terapie",
                        sheetLine("Anamnesi remota", patient.getAnamnesiRemota()),
                        sheetLine("Anamnesi prossima", patient.getAnamnesiProssima()),
                        sheetLine("Terapie svolte", patient.getTerapieSvolte())
                ),
                sheetSection("Test e misurazioni",
                        sheetLine("Test specifici somministrati", patient.getTestSpecificiSomministrati()),
                        sheetLine("Misurazione prestazione", patient.getMisurazionePrestazione())
                ),
                sheetSection("Valutazioni funzionali",
                        sheetLine("Equilibrio", patient.getValutazioneEquilibrio()),
                        sheetLine("ROM / AAROM / PROM", patient.getValutazioneRomAaromProm()),
                        sheetLine("Elasticità muscolare", patient.getValutazioneElasticitaMuscolare()),
                        sheetLine("Valutazione posturale", patient.getValutazionePosturale())
                ),
                sheetSection("Note",
                        sheetLine("Note", patient.getNote())
                ),
                createFilesSheetSection(patient)
        );

        ScrollPane scrollPane = new ScrollPane(sheetSections);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("patient-sheet-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainContent.getChildren().addAll(topBar, scrollPane);

        HBox root = new HBox();
        root.getStyleClass().add("patient-sheet-root");
        root.setFillHeight(true);
        root.getChildren().addAll(leftPanel, mainContent);
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        dialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(dialog);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    private HBox patientSheetStep(String number, String text) {
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

    private VBox createFilesSheetSection(Paziente patient) {
        VBox box = new VBox(10);
        box.getStyleClass().add("clinical-section");

        Label title = new Label("File allegati");
        title.getStyleClass().add("clinical-section-title");

        ListView<String> filesList = new ListView<>();
        filesList.setPrefHeight(170);
        filesList.getStyleClass().add("files-list");
        filesList.getItems().setAll(patient.getListaFileAllegati());
        filesList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String filePath, boolean empty) {
                super.updateItem(filePath, empty);

                if (empty || filePath == null || filePath.isBlank()) {
                    setText(null);
                } else {
                    setText(PatientFileStorage.getDisplayName(filePath));
                }
            }
        });

        Button openButton = new Button("Apri file selezionato");
        openButton.getStyleClass().add("primary-button-compact");
        openButton.setOnAction(event -> openFile(filesList.getSelectionModel().getSelectedItem()));

        if (!patient.hasFileAllegati()) {
            Label empty = new Label("Nessun file allegato.");
            empty.getStyleClass().add("muted-text");
            box.getChildren().addAll(title, empty);
        } else {
            box.getChildren().addAll(title, filesList, openButton);
        }

        return box;
    }

    private void openFile(String selectedFile) {
        if (selectedFile == null || selectedFile.isBlank()) {
            setStatus("Seleziona un file da aprire.", true);
            return;
        }

        Path path = Path.of(selectedFile);

        if (!path.toFile().exists()) {
            setStatus("Il file non esiste più nel percorso salvato.", true);
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            setStatus("Apertura file non supportata su questo sistema.", true);
            return;
        }

        try {
            Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            setStatus("Impossibile aprire il file selezionato.", true);
        }
    }


    private TextField textField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("input-field");
        return textField;
    }

    private TextArea textArea(String prompt, int rows) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(prompt);
        textArea.setPrefRowCount(rows);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("input-field");
        return textArea;
    }

    private VBox field(String label, Control control) {
        VBox box = new VBox(7);
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");
        box.getChildren().addAll(fieldLabel, control);
        return box;
    }

    private VBox field(String label, HBox content) {
        VBox box = new VBox(7);
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");
        box.getChildren().addAll(fieldLabel, content);
        return box;
    }

    private GridPane grid2(VBox... nodes) {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);

        grid.getColumnConstraints().addAll(col1, col2);

        for (int i = 0; i < nodes.length; i++) {
            int column = i % 2;
            int row = i / 2;
            grid.add(nodes[i], column, row);
        }

        return grid;
    }

    private VBox registerStyleHero(String eyebrow, String title, String subtitle, List<String> steps) {
        VBox hero = new VBox(24);
        hero.getStyleClass().add("register-dialog-hero");
        hero.setMaxHeight(Double.MAX_VALUE);
        hero.setPrefWidth(340);
        hero.setMinWidth(340);

        HBox brand = new HBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);

        Label brandMark = new Label("F");
        brandMark.getStyleClass().add("brand-mark");

        VBox brandText = new VBox(1);
        Label brandTitle = new Label("Fisioarea");
        brandTitle.getStyleClass().add("brand-title-light");
        Label brandSubtitle = new Label(eyebrow);
        brandSubtitle.getStyleClass().add("brand-subtitle-light");
        brandText.getChildren().addAll(brandTitle, brandSubtitle);

        brand.getChildren().addAll(brandMark, brandText);

        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);

        VBox copy = new VBox(10);
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("register-dialog-hero-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.getStyleClass().add("register-dialog-hero-text");
        copy.getChildren().addAll(titleLabel, subtitleLabel);

        VBox stepsBox = new VBox(12);
        for (int i = 0; i < steps.size(); i++) {
            stepsBox.getChildren().add(registerStyleStep(i + 1, steps.get(i)));
        }

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        Label footer = new Label("I dati sono salvati offline nella cartella FisioareaData.");
        footer.setWrapText(true);
        footer.getStyleClass().add("hero-footer");

        hero.getChildren().addAll(brand, topSpacer, copy, stepsBox, bottomSpacer, footer);
        return hero;
    }

    private HBox registerStyleStep(int number, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("register-dialog-step");

        Label badge = new Label(String.valueOf(number));
        badge.getStyleClass().add("step-badge");

        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("hero-feature-text");

        row.getChildren().addAll(badge, label);
        return row;
    }

    private VBox registerStyleFormHeader(String title, String subtitle) {
        VBox header = new VBox(6);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private VBox registerStyleCard(String title, Node content) {
        VBox card = new VBox(16);
        card.getStyleClass().add("register-dialog-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title-large");

        card.getChildren().addAll(titleLabel, content);
        return card;
    }

    private VBox sheetSection(String title, VBox... rows) {
        VBox box = new VBox(10);
        box.getStyleClass().add("register-dialog-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title-large");

        box.getChildren().add(titleLabel);
        box.getChildren().addAll(rows);

        return box;
    }

    private VBox sheetLine(String label, String value) {
        VBox box = new VBox(4);
        box.getStyleClass().add("sheet-line");

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("sheet-field-label");

        Label valueNode = new Label(value == null || value.isBlank() ? "—" : value);
        valueNode.setWrapText(true);
        valueNode.getStyleClass().add("sheet-field-value");

        box.getChildren().addAll(labelNode, valueNode);
        return box;
    }



    private void showDeletePatientDialog(Paziente selectedPatient) {
        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle("Elimina paziente");
        dialog.getDialogPane().getStyleClass().add("register-style-dialog-pane");
        dialog.getDialogPane().setPrefWidth(1040);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinWidth(1040);
        dialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(dialog);

        VBox hero = registerStyleHero(
                "Pazienti",
                "Elimina paziente",
                "Conferma la rimozione del paziente selezionato. I file copiati nella cartella dati resteranno conservati.",
                List.of(
                        "Controlla il paziente",
                        "Conferma eliminazione",
                        "Aggiorna la lista pazienti"
                )
        );

        VBox summary = confirmationStack(
                confirmationRow(
                        confirmationField("Paziente", confirmationValue(selectedPatient.getNomeCompleto())),
                        confirmationField("Telefono", confirmationValue(selectedPatient.getTelefono()))
                ),
                confirmationRow(
                        confirmationField("Email", confirmationValue(selectedPatient.getEmail())),
                        confirmationField("File", confirmationValue(selectedPatient.getFileCountLabel()))
                ),
                confirmationFullRow(confirmationWarning(
                        "Vuoi eliminare " + selectedPatient.getNomeCompleto()
                                + "? Il paziente verrà rimosso dalla lista, mentre i file copiati nella cartella dati resteranno conservati."
                ))
        );

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> closeDialog(dialog));

        Button deleteButton = new Button("Elimina paziente");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setDefaultButton(true);
        deleteButton.setOnAction(event -> {
            pazienteService.eliminaPaziente(selectedPatient.getId());
            loadPatients();
            setStatus("Paziente eliminato correttamente.", false);
            closeDialog(dialog);
        });

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("register-dialog-actions");
        actions.getChildren().addAll(cancelButton, deleteButton);

        VBox formPanel = new VBox(14);
        formPanel.getStyleClass().add("register-dialog-form-panel");
        formPanel.getChildren().addAll(
                registerStyleFormHeader(
                        "Conferma eliminazione",
                        "Prima di procedere verifica il paziente selezionato."
                ),
                registerStyleCard("Riepilogo paziente", summary),
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

    private VBox confirmationStack(Node... rows) {
        VBox stack = new VBox(12);
        stack.getStyleClass().add("register-form-stack");
        stack.getChildren().addAll(rows);
        return stack;
    }

    private HBox confirmationRow(Node left, Node right) {
        HBox row = new HBox(14);
        row.getStyleClass().add("register-form-row");
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        row.getChildren().addAll(left, right);
        return row;
    }

    private HBox confirmationFullRow(Node node) {
        HBox row = new HBox();
        row.getStyleClass().add("register-form-row");
        HBox.setHgrow(node, Priority.ALWAYS);
        row.getChildren().add(node);
        return row;
    }

    private VBox confirmationField(String label, Node value) {
        VBox box = new VBox(7);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("field-label");

        box.getChildren().addAll(labelNode, value);
        return box;
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fisioarea");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setStatus(String message, boolean error) {
        if (statusLabel == null) {
            return;
        }

        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success-label", "error-text");

        if (error) {
            statusLabel.getStyleClass().add("error-text");
        } else {
            statusLabel.getStyleClass().add("success-label");
        }
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

    private void applyDialogStyles(Dialog<?> dialog) {
        String stylesheet = getClass().getResource("/com/fisioarea/view/styles.css").toExternalForm();
        dialog.getDialogPane().getStylesheets().clear();
        dialog.getDialogPane().getStylesheets().add(stylesheet);
        dialog.getDialogPane().getStyleClass().add("modern-dialog-pane");
    }

    private record PatientFormData(
            PazienteService.PatientClinicalData data,
            List<File> files
    ) {
    }
}
