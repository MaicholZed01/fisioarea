package com.fisioarea.controller;

import com.fisioarea.model.Appuntamento;
import com.fisioarea.model.User;
import com.fisioarea.service.AppuntamentoService;
import com.fisioarea.service.AuthService;
import com.fisioarea.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReportController {

    @FXML
    private Label welcomeLabel;

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
        reportDatePicker.setValue(today);
        refreshReports(today);

        reportDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshReports(newValue);
            }
        });
    }

    @FXML
    private void handleToday() {
        setSelectedReportDate(LocalDate.now());
    }

    @FXML
    private void handlePreviousDay() {
        setSelectedReportDate(getSelectedReportDate().minusDays(1));
    }

    @FXML
    private void handleNextDay() {
        setSelectedReportDate(getSelectedReportDate().plusDays(1));
    }

    private LocalDate getSelectedReportDate() {
        return reportDatePicker.getValue() == null ? LocalDate.now() : reportDatePicker.getValue();
    }

    private void setSelectedReportDate(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        reportDatePicker.setValue(date);
        refreshReports(date);
    }

    private void refreshReports(LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
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

        reportPeriodLabel.setText("Periodo: "
                + weekStart.format(DateTimeFormatter.ofPattern("dd/MM"))
                + " - "
                + weekEnd.format(DateTimeFormatter.ofPattern("dd/MM"))
                + " • "
                + capitalize(selectedMonth.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"))));

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

        reportChartSubtitleLabel.setText("Andamento giornaliero dei pazienti nel mese di "
                + capitalize(month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"))));
    }

    @FXML
    private void handleOpenDashboard() {
        SceneNavigator.loadScene("/com/fisioarea/view/Dashboard.fxml", "Fisioarea - Dashboard");
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
        SceneNavigator.loadScene("/com/fisioarea/view/AdminUsers.fxml", "Fisioarea - Gestione utenti");
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
}
