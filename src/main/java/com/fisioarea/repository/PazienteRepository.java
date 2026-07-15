package com.fisioarea.repository;

import com.fisioarea.model.Paziente;
import com.fisioarea.util.FileStorage;
import com.fisioarea.util.OfflineDataConfig;
import com.fisioarea.util.ValidationUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PazienteRepository {

    private static final int OLD_COLUMN_COUNT = 4;
    private static final int BASIC_COLUMN_COUNT = 7;
    private static final int CURRENT_COLUMN_COUNT = 24;
    private static final String HEADER = "id;nome;cognome;telefono;email;indirizzoCasa;fileAllegati;eta;professione;diagnosi;forzaMuscolare;naturaProblema;esamiStrumentaliInVisione;core;anamnesiRemota;anamnesiProssima;terapieSvolte;testSpecificiSomministrati;misurazionePrestazione;valutazioneEquilibrio;valutazioneRomAaromProm;valutazioneElasticitaMuscolare;valutazionePosturale;note";

    private final Path file = OfflineDataConfig.getPazientiFile();
    private final List<Paziente> pazienti = new ArrayList<>();

    public PazienteRepository() {
        loadFromFile();

        if (pazienti.isEmpty()) {
            pazienti.add(new Paziente(1, "Marco", "Bianchi", "+39 333 123 4567",
                    "marco.bianchi@email.it", "Via Roma 12, Milano", ""));
            pazienti.add(new Paziente(2, "Giulia", "Leone", "+39 347 987 6543",
                    "giulia.leone@email.it", "Via Verdi 8, Roma", ""));
            pazienti.add(new Paziente(3, "Anna", "Ricci", "+39 320 456 7890",
                    "anna.ricci@email.it", "Corso Italia 20, Torino", ""));
            pazienti.add(new Paziente(4, "Luca", "Ferri", "+39 349 222 1111",
                    "luca.ferri@email.it", "Via Garibaldi 5, Bologna", ""));
            saveAllToFile();
        }
    }

    public List<Paziente> findAll() {
        return pazienti.stream()
                .sorted(Comparator.comparing(Paziente::getCognome).thenComparing(Paziente::getNome))
                .map(Paziente::copy)
                .toList();
    }

    public Optional<Paziente> findById(int id) {
        return pazienti.stream()
                .filter(paziente -> paziente.getId() == id)
                .findFirst()
                .map(Paziente::copy);
    }

    public Paziente save(Paziente paziente) {
        Paziente nuovoPaziente = paziente.copy();

        if (nuovoPaziente.getId() <= 0) {
            nuovoPaziente.setId(nextId());
        }

        pazienti.add(nuovoPaziente.copy());
        saveAllToFile();

        return nuovoPaziente.copy();
    }

    public Paziente update(Paziente pazienteAggiornato) {
        if (pazienteAggiornato.getId() <= 0) {
            throw new IllegalArgumentException("ID paziente non valido.");
        }

        for (int i = 0; i < pazienti.size(); i++) {
            if (pazienti.get(i).getId() == pazienteAggiornato.getId()) {
                pazienti.set(i, pazienteAggiornato.copy());
                saveAllToFile();
                return pazienteAggiornato.copy();
            }
        }

        throw new IllegalArgumentException("Paziente non trovato: " + pazienteAggiornato.getId());
    }

    public void deleteById(int id) {
        pazienti.removeIf(paziente -> paziente.getId() == id);
        saveAllToFile();
    }

    public int nextId() {
        return pazienti.stream()
                .map(Paziente::getId)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private void loadFromFile() {
        pazienti.clear();

        List<List<String>> rows = FileStorage.readRowsFlexible(file, OLD_COLUMN_COUNT, BASIC_COLUMN_COUNT, CURRENT_COLUMN_COUNT);

        for (List<String> row : rows) {
            pazienti.add(new Paziente(
                    Integer.parseInt(get(row, 0)),
                    get(row, 1),
                    get(row, 2),
                    get(row, 3),
                    row.size() >= BASIC_COLUMN_COUNT ? ValidationUtil.normalizeEmail(get(row, 4)) : "",
                    row.size() >= BASIC_COLUMN_COUNT ? get(row, 5) : "",
                    row.size() >= BASIC_COLUMN_COUNT ? get(row, 6) : "",
                    get(row, 7),
                    get(row, 8),
                    get(row, 9),
                    get(row, 10),
                    get(row, 11),
                    get(row, 12),
                    get(row, 13),
                    get(row, 14),
                    get(row, 15),
                    get(row, 16),
                    get(row, 17),
                    get(row, 18),
                    get(row, 19),
                    get(row, 20),
                    get(row, 21),
                    get(row, 22),
                    get(row, 23)
            ));
        }

        if (!pazienti.isEmpty()) {
            saveAllToFile();
        }
    }

    private void saveAllToFile() {
        List<List<String>> rows = new ArrayList<>();

        for (Paziente paziente : pazienti) {
            rows.add(List.of(
                    String.valueOf(paziente.getId()),
                    paziente.getNome(),
                    paziente.getCognome(),
                    paziente.getTelefono(),
                    ValidationUtil.normalizeEmail(paziente.getEmail()),
                    paziente.getIndirizzoCasa(),
                    paziente.getFileAllegati(),
                    paziente.getEta(),
                    paziente.getProfessione(),
                    paziente.getDiagnosi(),
                    paziente.getForzaMuscolare(),
                    paziente.getNaturaProblema(),
                    paziente.getEsamiStrumentaliInVisione(),
                    paziente.getCore(),
                    paziente.getAnamnesiRemota(),
                    paziente.getAnamnesiProssima(),
                    paziente.getTerapieSvolte(),
                    paziente.getTestSpecificiSomministrati(),
                    paziente.getMisurazionePrestazione(),
                    paziente.getValutazioneEquilibrio(),
                    paziente.getValutazioneRomAaromProm(),
                    paziente.getValutazioneElasticitaMuscolare(),
                    paziente.getValutazionePosturale(),
                    paziente.getNote()
            ));
        }

        FileStorage.writeRows(file, HEADER, rows);
    }

    private String get(List<String> row, int index) {
        if (row == null || index < 0 || index >= row.size()) {
            return "";
        }

        return row.get(index);
    }
}
