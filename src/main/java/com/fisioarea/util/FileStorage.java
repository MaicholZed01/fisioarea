package com.fisioarea.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Utility per leggere e scrivere file locali in modo semplice e robusto.
 *
 * Ogni campo viene codificato in Base64 prima di essere scritto.
 * In questo modo i testi possono contenere caratteri speciali, accenti,
 * punti e virgola, a capo e descrizioni lunghe senza rompere il formato.
 */
public final class FileStorage {

    private static final String SEPARATOR = ";";

    private FileStorage() {
    }

    public static void writeRows(Path file, String header, List<List<String>> rows) {
        try {
            Files.createDirectories(file.getParent());

            List<String> lines = new ArrayList<>();
            lines.add(header);

            for (List<String> row : rows) {
                List<String> encodedFields = new ArrayList<>();

                for (String field : row) {
                    encodedFields.add(encode(field));
                }

                lines.add(String.join(SEPARATOR, encodedFields));
            }

            Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio del file: " + file, e);
        }
    }

    public static List<List<String>> readRows(Path file, int columnCount) {
        return readRowsFlexible(file, columnCount);
    }

    public static List<List<String>> readRowsFlexible(Path file, int... acceptedColumnCounts) {
        List<List<String>> rows = new ArrayList<>();

        if (!Files.exists(file)) {
            return rows;
        }

        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line == null || line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(SEPARATOR, -1);

                if (!isAcceptedColumnCount(parts.length, acceptedColumnCounts)) {
                    continue;
                }

                List<String> row = new ArrayList<>();

                for (String part : parts) {
                    row.add(decode(part));
                }

                rows.add(row);
            }

            return rows;

        } catch (IOException e) {
            throw new RuntimeException("Errore durante la lettura del file: " + file, e);
        }
    }

    private static boolean isAcceptedColumnCount(int currentColumnCount, int... acceptedColumnCounts) {
        if (acceptedColumnCounts == null || acceptedColumnCounts.length == 0) {
            return true;
        }

        for (int acceptedColumnCount : acceptedColumnCounts) {
            if (currentColumnCount == acceptedColumnCount) {
                return true;
            }
        }

        return false;
    }

    private static String encode(String value) {
        String safeValue = value == null ? "" : value;

        return Base64.getEncoder()
                .encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Compatibilità con eventuali vecchi file non codificati in Base64.
            return value == null ? "" : value;
        }
    }
}
