package com.fisioarea.repository;

import com.fisioarea.model.Appuntamento;
import com.fisioarea.util.FileStorage;
import com.fisioarea.util.OfflineDataConfig;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AppuntamentoRepository {

    private static final int CURRENT_COLUMN_COUNT = 7;
    private static final int OLD_COLUMN_COUNT = 6;
    private static final String HEADER = "id;dataOra;paziente;trattamento;stato;sala;immagini";

    private final Path file = OfflineDataConfig.getAppuntamentiFile();
    private final List<Appuntamento> appuntamenti = new ArrayList<>();

    public AppuntamentoRepository() {
        loadFromFile();

        if (appuntamenti.isEmpty()) {
            LocalDate today = LocalDate.now();

            appuntamenti.add(new Appuntamento(1, LocalDateTime.of(today, java.time.LocalTime.of(9, 0)),
                    "Marco Bianchi", "Riabilitazione ginocchio", "Confermato", "Sala 1", ""));
            appuntamenti.add(new Appuntamento(2, LocalDateTime.of(today, java.time.LocalTime.of(10, 30)),
                    "Giulia Leone", "Fisioterapia lombare", "In attesa", "Sala 2", ""));
            appuntamenti.add(new Appuntamento(3, LocalDateTime.of(today, java.time.LocalTime.of(11, 15)),
                    "Anna Ricci", "Terapia posturale", "Confermato", "Sala 1", ""));
            appuntamenti.add(new Appuntamento(4, LocalDateTime.of(today, java.time.LocalTime.of(15, 0)),
                    "Luca Ferri", "Massoterapia", "Completato", "Sala 3", ""));

            saveAllToFile();
        }
    }

    public List<Appuntamento> findAll() {
        return appuntamenti.stream()
                .sorted(Comparator.comparing(Appuntamento::getDataOra))
                .toList();
    }

    public Optional<Appuntamento> findById(int id) {
        return appuntamenti.stream()
                .filter(appuntamento -> appuntamento.getId() == id)
                .findFirst();
    }

    public List<Appuntamento> findByDate(LocalDate date) {
        return appuntamenti.stream()
                .filter(appuntamento -> appuntamento.getDataOra().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Appuntamento::getDataOra))
                .toList();
    }

    public Appuntamento save(Appuntamento appuntamento) {
        Appuntamento nuovoAppuntamento = appuntamento;

        if (appuntamento.getId() <= 0) {
            nuovoAppuntamento = new Appuntamento(
                    nextId(),
                    appuntamento.getDataOra(),
                    appuntamento.getPaziente(),
                    appuntamento.getTrattamento(),
                    appuntamento.getStato(),
                    appuntamento.getSala(),
                    appuntamento.getImmagini()
            );
        }

        appuntamenti.add(nuovoAppuntamento);
        saveAllToFile();

        return nuovoAppuntamento;
    }

    public Appuntamento update(Appuntamento updatedAppointment) {
        if (updatedAppointment.getId() <= 0) {
            throw new IllegalArgumentException("ID appuntamento non valido.");
        }

        for (int i = 0; i < appuntamenti.size(); i++) {
            if (appuntamenti.get(i).getId() == updatedAppointment.getId()) {
                appuntamenti.set(i, updatedAppointment);
                saveAllToFile();
                return updatedAppointment;
            }
        }

        throw new IllegalArgumentException("Appuntamento non trovato: " + updatedAppointment.getId());
    }

    public void deleteById(int id) {
        appuntamenti.removeIf(appuntamento -> appuntamento.getId() == id);
        saveAllToFile();
    }

    public int nextId() {
        return appuntamenti.stream()
                .map(Appuntamento::getId)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private void loadFromFile() {
        appuntamenti.clear();

        List<List<String>> rows = FileStorage.readRowsFlexible(file, OLD_COLUMN_COUNT, CURRENT_COLUMN_COUNT);

        for (List<String> row : rows) {
            String immagini = row.size() >= CURRENT_COLUMN_COUNT ? row.get(6) : "";

            appuntamenti.add(new Appuntamento(
                    Integer.parseInt(row.get(0)),
                    LocalDateTime.parse(row.get(1)),
                    row.get(2),
                    row.get(3),
                    row.get(4),
                    row.get(5),
                    immagini
            ));
        }

        if (!appuntamenti.isEmpty()) {
            saveAllToFile();
        }
    }

    private void saveAllToFile() {
        List<List<String>> rows = new ArrayList<>();

        for (Appuntamento appuntamento : appuntamenti) {
            rows.add(List.of(
                    String.valueOf(appuntamento.getId()),
                    appuntamento.getDataOra().toString(),
                    appuntamento.getPaziente(),
                    appuntamento.getTrattamento(),
                    appuntamento.getStato(),
                    appuntamento.getSala(),
                    appuntamento.getImmagini()
            ));
        }

        FileStorage.writeRows(file, HEADER, rows);
    }
}
