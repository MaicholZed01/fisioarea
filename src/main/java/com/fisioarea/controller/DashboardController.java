package com.fisioarea.controller;

import com.fisioarea.model.Appuntamento;
import com.fisioarea.model.User;
import com.fisioarea.service.AppuntamentoService;
import com.fisioarea.service.AuthService;
import com.fisioarea.util.ImageStorage;
import com.fisioarea.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.fisioarea.util.AppointmentExcelExporter;
import java.util.Comparator;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Window;
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.Locale;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label calendarTitleLabel;

    @FXML
    private DatePicker agendaDatePicker;

    @FXML
    private TextField globalSearchField;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private DatePicker reportDatePicker;

    @FXML
    private Label dailyPatientsValueLabel;

    @FXML
    private Label weeklyPatientsValueLabel;

    @FXML
    private Label monthlyPatientsValueLabel;

    @FXML
    private Label reportPeriodLabel;

    @FXML
    private Label reportChartSubtitleLabel;

    @FXML
    private LineChart<String, Number> patientsLineChart;

    @FXML
    private Button adminUsersButton;

    private final AppuntamentoService appuntamentoService = new AppuntamentoService();

    private final DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        User currentUser = AuthService.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText(currentUser.getNomeCompleto());
        } else {
            welcomeLabel.setText("Profilo");
        }

        configureAdminControls(currentUser);

        LocalDate today = LocalDate.now();

        if (reportDatePicker != null) {
            reportDatePicker.setValue(today);
            refreshReports(today);

            reportDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    refreshReports(newValue);
                }
            });
        }

        if (agendaDatePicker != null && calendarGrid != null) {
            agendaDatePicker.setValue(today);
            renderCalendar(today);

            agendaDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    renderCalendar(newValue);
                }
            });
        }

        if (globalSearchField != null) {
            globalSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (agendaDatePicker != null && calendarGrid != null) {
                    LocalDate date = agendaDatePicker.getValue() == null ? LocalDate.now() : agendaDatePicker.getValue();
                    renderCalendar(date);
                }
            });
        }
    }

    @FXML
    private void handleToday() {
        LocalDate today = LocalDate.now();
        setSelectedDashboardDate(today);
    }

    @FXML
    private void handlePreviousDay() {
        LocalDate current = getSelectedDashboardDate();
        setSelectedDashboardDate(current.minusDays(1));
    }

    @FXML
    private void handleNextDay() {
        LocalDate current = getSelectedDashboardDate();
        setSelectedDashboardDate(current.plusDays(1));
    }

    private LocalDate getSelectedDashboardDate() {
        if (reportDatePicker != null && reportDatePicker.getValue() != null) {
            return reportDatePicker.getValue();
        }

        if (agendaDatePicker != null && agendaDatePicker.getValue() != null) {
            return agendaDatePicker.getValue();
        }

        return LocalDate.now();
    }

    private void setSelectedDashboardDate(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        if (reportDatePicker != null) {
            reportDatePicker.setValue(date);
            refreshReports(date);
        }

        if (agendaDatePicker != null && calendarGrid != null) {
            agendaDatePicker.setValue(date);
            renderCalendar(date);
        }
    }

    private void refreshReports(LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
        }

        if (dailyPatientsValueLabel == null
                || weeklyPatientsValueLabel == null
                || monthlyPatientsValueLabel == null
                || patientsLineChart == null) {
            return;
        }

        List<Appuntamento> appointments = appuntamentoService.getAppuntamenti();

        LocalDate dayStart = referenceDate;
        LocalDate weekStart = referenceDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        YearMonth selectedMonth = YearMonth.from(referenceDate);
        LocalDate monthStart = selectedMonth.atDay(1);
        LocalDate monthEnd = selectedMonth.atEndOfMonth();

        long dailyPatients = countDistinctPatients(appointments, dayStart, dayStart);
        long weeklyPatients = countDistinctPatients(appointments, weekStart, weekEnd);
        long monthlyPatients = countDistinctPatients(appointments, monthStart, monthEnd);

        dailyPatientsValueLabel.setText(String.valueOf(dailyPatients));
        weeklyPatientsValueLabel.setText(String.valueOf(weeklyPatients));
        monthlyPatientsValueLabel.setText(String.valueOf(monthlyPatients));

        if (reportPeriodLabel != null) {
            reportPeriodLabel.setText("Periodo: "
                    + weekStart.format(DateTimeFormatter.ofPattern("dd/MM"))
                    + " - "
                    + weekEnd.format(DateTimeFormatter.ofPattern("dd/MM"))
                    + " • "
                    + capitalize(selectedMonth.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
        }

        renderPatientsLineChart(appointments, selectedMonth);
    }

    private long countDistinctPatients(List<Appuntamento> appointments, LocalDate startDate, LocalDate endDate) {
        if (appointments == null || appointments.isEmpty()) {
            return 0;
        }

        return appointments.stream()
                .filter(appuntamento -> {
                    LocalDate appointmentDate = appuntamento.getDataOra().toLocalDate();
                    return !appointmentDate.isBefore(startDate) && !appointmentDate.isAfter(endDate);
                })
                .map(Appuntamento::getPaziente)
                .map(this::normalizePatientName)
                .filter(patient -> !patient.isBlank())
                .distinct()
                .count();
    }

    private String normalizePatientName(String patientName) {
        return patientName == null ? "" : patientName.trim().toLowerCase(Locale.ITALIAN);
    }

    private void renderPatientsLineChart(List<Appuntamento> appointments, YearMonth month) {
        patientsLineChart.getData().clear();
        patientsLineChart.setLegendVisible(false);
        patientsLineChart.setAnimated(false);
        patientsLineChart.setCreateSymbols(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pazienti");

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate currentDay = month.atDay(day);
            long count = countDistinctPatients(appointments, currentDay, currentDay);
            series.getData().add(new XYChart.Data<>(String.format("%02d", day), count));
        }

        patientsLineChart.getData().add(series);

        if (reportChartSubtitleLabel != null) {
            reportChartSubtitleLabel.setText("Andamento giornaliero dei pazienti nel mese di "
                    + capitalize(month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
        }
    }

    private void renderCalendar(LocalDate date) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        calendarTitleLabel.setText(capitalize(date.format(titleFormatter)));

        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setMinWidth(82);
        timeColumn.setPrefWidth(82);
        timeColumn.setMaxWidth(82);

        ColumnConstraints appointmentColumn = new ColumnConstraints();
        appointmentColumn.setHgrow(Priority.ALWAYS);

        calendarGrid.getColumnConstraints().addAll(timeColumn, appointmentColumn);

        String search = globalSearchField == null || globalSearchField.getText() == null
                ? ""
                : globalSearchField.getText().trim().toLowerCase();

        List<Appuntamento> appointments = appuntamentoService.getAppuntamentiDelGiorno(date).stream()
                .filter(appuntamento -> search.isBlank()
                        || appuntamento.getPaziente().toLowerCase().contains(search)
                        || appuntamento.getTrattamento().toLowerCase().contains(search)
                        || appuntamento.getStato().toLowerCase().contains(search)
                        || appuntamento.getSala().toLowerCase().contains(search))
                .sorted(Comparator.comparing(Appuntamento::getDataOra))
                .toList();

        int row = 0;

        for (int hour = 8; hour <= 20; hour++) {
            int currentHour = hour;

            List<Appuntamento> appointmentsInHour = appointments.stream()
                    .filter(appuntamento -> appuntamento.getDataOra().getHour() == currentHour)
                    .sorted(Comparator.comparing(Appuntamento::getDataOra))
                    .toList();

            double rowHeight = calculateHourRowHeight(appointmentsInHour);

            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(rowHeight);
            rowConstraints.setPrefHeight(rowHeight);
            rowConstraints.setVgrow(Priority.NEVER);
            calendarGrid.getRowConstraints().add(rowConstraints);

            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setAlignment(Pos.TOP_RIGHT);
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setPadding(new Insets(12, 12, 0, 0));
            timeLabel.getStyleClass().add("calendar-time-label");

            VBox slot = new VBox(10);
            slot.setMinHeight(rowHeight);
            slot.setPrefHeight(rowHeight);
            slot.setMaxWidth(Double.MAX_VALUE);
            slot.setFillWidth(true);
            slot.setPadding(new Insets(10, 10, 10, 10));
            slot.getStyleClass().add("calendar-slot");

            slot.setOnMouseClicked(event -> openAppointmentDialog(
                    null,
                    LocalDateTime.of(date, LocalTime.of(currentHour, 0))
            ));

            if (appointmentsInHour.size() > 1) {
                Label countLabel = new Label(appointmentsInHour.size() + " appuntamenti in questa fascia oraria");
                countLabel.getStyleClass().add("calendar-slot-count");
                slot.getChildren().add(countLabel);
            }

            for (Appuntamento appuntamento : appointmentsInHour) {
                slot.getChildren().add(createAppointmentCard(appuntamento));
            }

            calendarGrid.add(timeLabel, 0, row);
            calendarGrid.add(slot, 1, row);

            row++;
        }
    }

    private double calculateHourRowHeight(List<Appuntamento> appointmentsInHour) {
        if (appointmentsInHour == null || appointmentsInHour.isEmpty()) {
            return 92;
        }

        double basePadding = appointmentsInHour.size() > 1 ? 48 : 22;
        double totalCardsHeight = appointmentsInHour.stream()
                .mapToDouble(this::estimateAppointmentCardHeight)
                .sum();

        return Math.max(96, basePadding + totalCardsHeight + Math.max(0, appointmentsInHour.size() - 1) * 10);
    }

    private double estimateAppointmentCardHeight(Appuntamento appuntamento) {
        return appuntamento != null && appuntamento.hasImmagini() ? 92 : 70;
    }

    private Node createAppointmentCard(Appuntamento appuntamento) {
        VBox card = new VBox(5);
        card.getStyleClass().add("calendar-event-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setOnMouseClicked(event -> {
            event.consume();
            showAppointmentDetails(appuntamento);
        });

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label time = new Label(appuntamento.getDataOra().format(timeFormatter));
        time.getStyleClass().add("calendar-event-time");

        Label patient = new Label(appuntamento.getPaziente());
        patient.setWrapText(true);
        patient.getStyleClass().add("calendar-event-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(appuntamento.getStato());
        status.setMinWidth(Region.USE_PREF_SIZE);
        status.getStyleClass().add("calendar-event-status");

        topRow.getChildren().addAll(time, patient, spacer, status);

        Label treatment = new Label(appuntamento.getTrattamento() + " • " + appuntamento.getSala());
        treatment.setWrapText(true);
        treatment.getStyleClass().add("calendar-event-subtitle");

        card.getChildren().addAll(topRow, treatment);

        if (appuntamento.hasImmagini()) {
            Label images = new Label("📎 " + appuntamento.getNumeroImmagini() + " immagine/i allegate");
            images.getStyleClass().add("calendar-event-attachment");
            card.getChildren().add(images);
        }

        return card;
    }

    @FXML
    private void handleNewAppointment() {
        LocalDate date = getSelectedDashboardDate();
        openAppointmentDialog(null, LocalDateTime.of(date, LocalTime.of(9, 0)));
    }


    private void openAppointmentDialog(Appuntamento appointmentToEdit, LocalDateTime defaultDateTime) {
        boolean editMode = appointmentToEdit != null;

        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle(editMode ? "Modifica appuntamento" : "Nuovo appuntamento");
        dialog.getDialogPane().getStyleClass().add("register-style-dialog-pane");
        dialog.getDialogPane().setPrefWidth(1040);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinWidth(1040);
        dialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(dialog);

        LocalDateTime initialDateTime = editMode ? appointmentToEdit.getDataOra() : defaultDateTime;

        DatePicker datePicker = new DatePicker(initialDateTime.toLocalDate());
        datePicker.getStyleClass().add("input-field");

        TextField timeField = new TextField();
        timeField.setPromptText("09:30");
        timeField.setText(initialDateTime.toLocalTime().format(timeFormatter));
        timeField.getStyleClass().add("input-field");

        TextField patientField = new TextField();
        patientField.setPromptText("Nome e cognome paziente");
        patientField.setText(editMode ? appointmentToEdit.getPaziente() : "");
        patientField.getStyleClass().add("input-field");

        TextField treatmentField = new TextField();
        treatmentField.setPromptText("Es. Riabilitazione ginocchio, terapia manuale...");
        treatmentField.setText(editMode ? appointmentToEdit.getTrattamento() : "");
        treatmentField.getStyleClass().add("input-field");

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().setAll("Confermato", "In attesa", "Completato", "Annullato");
        statusComboBox.getSelectionModel().select(editMode ? appointmentToEdit.getStato() : "Confermato");
        statusComboBox.setMaxWidth(Double.MAX_VALUE);
        statusComboBox.getStyleClass().add("input-field");

        if (statusComboBox.getValue() == null) {
            statusComboBox.getSelectionModel().selectFirst();
        }

        TextField roomField = new TextField();
        roomField.setPromptText("Sala 1");
        roomField.setText(editMode ? appointmentToEdit.getSala() : "");
        roomField.getStyleClass().add("input-field");

        List<File> selectedImages = new ArrayList<>();

        int existingImagesCount = editMode ? appointmentToEdit.getNumeroImmagini() : 0;
        Label selectedImagesLabel = new Label(existingImagesCount > 0
                ? existingImagesCount + " immagine/i già allegate"
                : "Nessuna immagine selezionata");
        selectedImagesLabel.getStyleClass().add("modern-helper-text");

        Button chooseImagesButton = new Button(editMode ? "Aggiungi immagini" : "Carica immagini");
        chooseImagesButton.getStyleClass().add("ghost-button");
        chooseImagesButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleziona immagini appuntamento");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                    new FileChooser.ExtensionFilter("Tutti i file", "*.*")
            );

            List<File> files = fileChooser.showOpenMultipleDialog(dialog.getDialogPane().getScene().getWindow());

            if (files != null && !files.isEmpty()) {
                selectedImages.clear();
                selectedImages.addAll(files);

                int total = existingImagesCount + selectedImages.size();
                selectedImagesLabel.setText(total + " immagine/i totali");
            }
        });

        HBox imagesBox = new HBox(12, chooseImagesButton, selectedImagesLabel);
        imagesBox.setAlignment(Pos.CENTER_LEFT);

        VBox hero = registerStyleHero(
                "Agenda",
                editMode ? "Modifica appuntamento" : "Nuovo appuntamento",
                "Inserisci rapidamente i dettagli della seduta mantenendo l'agenda ordinata.",
                List.of(
                        "Scegli data e ora",
                        "Collega paziente e trattamento",
                        "Conferma sala e stato"
                )
        );

        VBox appointmentForm = formStack(
                formRow(field("Data", datePicker), field("Ora", timeField)),
                fullRow(field("Paziente", patientField)),
                fullRow(field("Trattamento", treatmentField)),
                formRow(field("Stato", statusComboBox), field("Sala", roomField)),
                fullRow(field("Immagini allegate", imagesBox))
        );

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> closeDialog(dialog));

        Button saveButton = new Button(editMode ? "Salva modifiche" : "Crea appuntamento");
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
                        editMode ? "Aggiorna appuntamento" : "Crea appuntamento",
                        "Compila i dettagli principali. L'appuntamento verrà salvato offline."
                ),
                registerStyleCard("Dettagli appuntamento", appointmentForm),
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
            try {
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeField.getText().trim());

                List<String> imagePaths = new ArrayList<>();

                if (editMode) {
                    imagePaths.addAll(appointmentToEdit.getListaImmagini());
                }

                imagePaths.addAll(ImageStorage.copyAppointmentImages(selectedImages));

                Appuntamento appuntamento = new Appuntamento(
                        editMode ? appointmentToEdit.getId() : 0,
                        LocalDateTime.of(date, time),
                        patientField.getText().trim(),
                        treatmentField.getText().trim(),
                        statusComboBox.getValue(),
                        roomField.getText().trim(),
                        ImageStorage.joinImagePaths(imagePaths)
                );

                if (appuntamento.getPaziente().isBlank() || appuntamento.getTrattamento().isBlank()) {
                    showError("Dati mancanti", "Inserisci almeno paziente e trattamento.");
                    return;
                }

                if (editMode) {
                    appuntamentoService.aggiornaAppuntamento(appuntamento);
                    showInfo("Appuntamento aggiornato", "Le modifiche sono state salvate su file locale.");
                } else {
                    appuntamentoService.salvaAppuntamento(appuntamento);
                    showInfo("Appuntamento salvato", "L'appuntamento è stato salvato su file locale.");
                }

                if (agendaDatePicker != null && calendarGrid != null) {
                    agendaDatePicker.setValue(appuntamento.getDataOra().toLocalDate());
                    renderCalendar(appuntamento.getDataOra().toLocalDate());
                }

                if (reportDatePicker != null) {
                    reportDatePicker.setValue(appuntamento.getDataOra().toLocalDate());
                    refreshReports(appuntamento.getDataOra().toLocalDate());
                }

                closeDialog(dialog);

            } catch (Exception e) {
                showError("Dati non validi", "Controlla l'ora nel formato HH:mm e le immagini selezionate.");
            }
        });

        dialog.showAndWait();
    }


    private VBox field(String label, Node input) {
        VBox box = new VBox(7);

        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");

        box.getChildren().addAll(fieldLabel, input);
        return box;
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

        Label footer = new Label("Tutto resta salvato nella cartella locale FisioareaData.");
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

    private GridPane registerStyleGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    private VBox registerStyleField(String label, Node input) {
        VBox box = new VBox(7);
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("field-label");
        box.getChildren().addAll(labelNode, input);
        return box;
    }


    private void showAppointmentDetails(Appuntamento appuntamento) {
        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle("Dettaglio appuntamento");
        dialog.getDialogPane().getStyleClass().add("register-style-dialog-pane");
        dialog.getDialogPane().setPrefWidth(1040);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinWidth(1040);
        dialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(dialog);

        VBox hero = registerStyleHero(
                "Agenda",
                "Dettaglio appuntamento",
                "Controlla i dati della seduta, modifica l'appuntamento oppure eliminalo dall'agenda.",
                List.of(
                        "Visualizza data e ora",
                        "Controlla trattamento e sala",
                        "Modifica o rimuovi l'appuntamento"
                )
        );

        VBox details = formStack(
                formRow(
                        field("Data", appointmentDetailValue(appuntamento.getDataOra().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))),
                        field("Ora", appointmentDetailValue(appuntamento.getOra()))
                ),
                fullRow(field("Paziente", appointmentDetailValue(appuntamento.getPaziente()))),
                fullRow(field("Trattamento", appointmentDetailValue(appuntamento.getTrattamento()))),
                formRow(
                        field("Stato", appointmentDetailValue(appuntamento.getStato())),
                        field("Sala", appointmentDetailValue(appuntamento.getSala()))
                )
        );

        Node imagesSection = createAppointmentImagesSection(appuntamento);

        Button closeButton = new Button("Chiudi");
        closeButton.getStyleClass().add("ghost-button");
        closeButton.setOnAction(event -> closeDialog(dialog));

        Button deleteButton = new Button("Elimina");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(event -> confirmAndDeleteAppointment(appuntamento, dialog));

        Button editButton = new Button("Modifica");
        editButton.getStyleClass().add("primary-button-compact");
        editButton.setDefaultButton(true);
        editButton.setOnAction(event -> {
            closeDialog(dialog);
            openAppointmentDialog(appuntamento, appuntamento.getDataOra());
        });

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("register-dialog-actions");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actions.getChildren().addAll(deleteButton, spacer, closeButton, editButton);

        VBox formPanel = new VBox(14);
        formPanel.getStyleClass().add("register-dialog-form-panel");
        formPanel.getChildren().addAll(
                registerStyleFormHeader(
                        appuntamento.getPaziente(),
                        appuntamento.getDataOra().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")) + " alle " + appuntamento.getOra()
                ),
                registerStyleCard("Informazioni appuntamento", details),
                registerStyleCard("Immagini allegate", imagesSection),
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
        dialog.showAndWait();
    }

    private Label appointmentDetailValue(String value) {
        Label label = new Label(value == null || value.isBlank() ? "—" : value);
        label.setWrapText(true);
        label.getStyleClass().add("appointment-detail-value");
        return label;
    }

    private Node createAppointmentImagesSection(Appuntamento appuntamento) {
        if (!appuntamento.hasImmagini()) {
            Label noImages = new Label("Nessuna immagine allegata a questo appuntamento.");
            noImages.setWrapText(true);
            noImages.getStyleClass().add("appointment-detail-empty");
            return noImages;
        }

        TilePane imagePane = new TilePane();
        imagePane.setHgap(12);
        imagePane.setVgap(12);
        imagePane.setPrefColumns(3);
        imagePane.getStyleClass().add("appointment-detail-image-pane");

        for (String imagePath : appuntamento.getListaImmagini()) {
            Path path = Path.of(imagePath);

            if (!path.toFile().exists()) {
                continue;
            }

            Image image = new Image(path.toUri().toString(), 150, 110, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(150);
            imageView.setFitHeight(110);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("appointment-detail-image-preview");

            imageView.setOnMouseClicked(event -> {
                event.consume();
                showImagePreview(path);
            });

            imagePane.getChildren().add(imageView);
        }

        if (imagePane.getChildren().isEmpty()) {
            Label missingImages = new Label("Le immagini risultano collegate, ma i file non sono più disponibili sul computer.");
            missingImages.setWrapText(true);
            missingImages.getStyleClass().add("appointment-detail-empty");
            return missingImages;
        }

        ScrollPane scrollPane = new ScrollPane(imagePane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(160);
        scrollPane.getStyleClass().add("appointment-detail-images-scroll");
        return scrollPane;
    }

    private void confirmAndDeleteAppointment(Appuntamento appuntamento) {
        confirmAndDeleteAppointment(appuntamento, null);
    }

    private void confirmAndDeleteAppointment(Appuntamento appuntamento, Dialog<?> parentDialog) {
        Dialog<Void> confirmDialog = new Dialog<>();
        applyDialogStyles(confirmDialog);
        confirmDialog.setTitle("Elimina appuntamento");
        confirmDialog.getDialogPane().getStyleClass().add("register-style-dialog-pane");
        confirmDialog.getDialogPane().setPrefWidth(1040);
        confirmDialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        confirmDialog.getDialogPane().setMinWidth(1040);
        confirmDialog.getDialogPane().getButtonTypes().clear();
        enableWindowClose(confirmDialog);

        VBox hero = registerStyleHero(
                "Agenda",
                "Conferma eliminazione",
                "Questa azione rimuove l'appuntamento dall'agenda locale.",
                List.of(
                        "Verifica appuntamento",
                        "Conferma eliminazione",
                        "Aggiorna agenda"
                )
        );

        Label warning = new Label(
                "Vuoi eliminare l'appuntamento di " + appuntamento.getPaziente()
                        + " delle " + appuntamento.getOra() + "?"
        );
        warning.setWrapText(true);
        warning.getStyleClass().add("appointment-delete-warning");

        VBox info = formStack(
                fullRow(field("Paziente", appointmentDetailValue(appuntamento.getPaziente()))),
                formRow(
                        field("Data", appointmentDetailValue(appuntamento.getDataOra().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))),
                        field("Ora", appointmentDetailValue(appuntamento.getOra()))
                ),
                fullRow(warning)
        );

        Button cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("ghost-button");
        cancelButton.setOnAction(event -> closeDialog(confirmDialog));

        Button deleteButton = new Button("Elimina appuntamento");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setDefaultButton(true);
        deleteButton.setOnAction(event -> {
            appuntamentoService.eliminaAppuntamento(appuntamento.getId());

            LocalDate date = getSelectedDashboardDate();

            if (agendaDatePicker != null && calendarGrid != null) {
                renderCalendar(date);
            }

            if (reportDatePicker != null) {
                refreshReports(date);
            }

            closeDialog(confirmDialog);

            if (parentDialog != null) {
                closeDialog(parentDialog);
            }

            showInfo("Appuntamento eliminato", "L'appuntamento è stato eliminato dal file locale.");
        });

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("register-dialog-actions");
        actions.getChildren().addAll(cancelButton, deleteButton);

        VBox formPanel = new VBox(14);
        formPanel.getStyleClass().add("register-dialog-form-panel");
        formPanel.getChildren().addAll(
                registerStyleFormHeader(
                        "Eliminare appuntamento?",
                        "L'operazione aggiornerà subito l'agenda salvata offline."
                ),
                registerStyleCard("Riepilogo", info),
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

        confirmDialog.getDialogPane().setContent(layout);
        confirmDialog.setResizable(false);
        confirmDialog.showAndWait();
    }


    private void showImagePreview(Path imagePath) {
        Dialog<Void> dialog = new Dialog<>();
        applyDialogStyles(dialog);
        dialog.setTitle("Anteprima immagine");
        dialog.setHeaderText(imagePath.getFileName().toString());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Image image = new Image(imagePath.toUri().toString(), 720, 520, true, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(720);
        imageView.setFitHeight(520);
        imageView.setPreserveRatio(true);

        StackPane wrapper = new StackPane(imageView);
        wrapper.setPadding(new Insets(12));
        wrapper.getStyleClass().add("image-preview-wrapper");

        dialog.getDialogPane().setContent(wrapper);
        dialog.showAndWait();
    }



    @FXML
    private void handleExportAppointmentsToExcel() {
        List<Appuntamento> appointments = appuntamentoService.getAppuntamenti();

        if (appointments == null || appointments.isEmpty()) {
            showError("Nessun appuntamento", "Non ci sono appuntamenti da esportare.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Esporta appuntamenti in Excel");
        fileChooser.setInitialFileName("appuntamenti_fisioarea.xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx")
        );

        File selectedFile = fileChooser.showSaveDialog(getDashboardWindow());

        if (selectedFile == null) {
            return;
        }

        File destinationFile = selectedFile.getName().toLowerCase().endsWith(".xlsx")
                ? selectedFile
                : new File(selectedFile.getAbsolutePath() + ".xlsx");

        try {
            AppointmentExcelExporter.exportAppointments(destinationFile.toPath(), appointments);
            showInfo("Esportazione completata", "File Excel salvato correttamente:\n" + destinationFile.getAbsolutePath());
        } catch (RuntimeException e) {
            showError("Errore esportazione", "Non è stato possibile esportare gli appuntamenti in Excel.");
        }
    }


    private Window getDashboardWindow() {
        if (reportDatePicker != null && reportDatePicker.getScene() != null) {
            return reportDatePicker.getScene().getWindow();
        }

        if (agendaDatePicker != null && agendaDatePicker.getScene() != null) {
            return agendaDatePicker.getScene().getWindow();
        }

        if (calendarGrid != null && calendarGrid.getScene() != null) {
            return calendarGrid.getScene().getWindow();
        }

        return null;
    }


    @FXML
    private void handleOpenReports() {
        SceneNavigator.loadScene("/com/fisioarea/view/Reports.fxml", "Fisioarea - Report");
    }

    @FXML
    private void handleOpenPatients() {
        SceneNavigator.loadScene("/com/fisioarea/view/Patients.fxml", "Fisioarea - Pazienti");
    }

    @FXML
    private void handleOpenProfile() {
        SceneNavigator.loadScene("/com/fisioarea/view/Profile.fxml", "Fisioarea - Profilo");
    }

    @FXML
    private void handleOpenUserManagement() {
        User currentUser = AuthService.getCurrentUser();

        if (!isAdmin(currentUser)) {
            showError("Accesso negato", "Solo un profilo Admin può gestire gli altri utenti.");
            return;
        }

        SceneNavigator.loadScene("/com/fisioarea/view/AdminUsers.fxml", "Fisioarea - Gestione profili");
    }

    private void configureAdminControls(User currentUser) {
        if (adminUsersButton == null) {
            return;
        }

        boolean isAdmin = isAdmin(currentUser);
        adminUsersButton.setVisible(isAdmin);
        adminUsersButton.setManaged(isAdmin);
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRuolo() != null && user.getRuolo().equalsIgnoreCase("Admin");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneNavigator.loadScene("/com/fisioarea/view/Login.fxml", "Fisioarea - Login");
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fisioarea");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fisioarea");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
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

}
