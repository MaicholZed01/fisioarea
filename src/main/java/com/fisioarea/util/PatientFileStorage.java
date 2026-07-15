package com.fisioarea.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gestisce i file caricati per i pazienti in modalità offline.
 *
 * I file originali vengono copiati nella cartella dati locale di Fisioarea:
 * <cartella_utente>/FisioareaData/file_pazienti/<id_paziente>
 *
 * In questo modo il software non dipende più dal percorso originale scelto dall'utente.
 */
public final class PatientFileStorage {

    private static final String PATH_SEPARATOR = "\\|\\|";

    private PatientFileStorage() {
    }

    public static List<String> copyPatientFiles(int patientId, List<File> sourceFiles) {
        List<String> savedPaths = new ArrayList<>();

        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return savedPaths;
        }

        Path destinationDirectory = OfflineDataConfig.getPatientFilesDirectory()
                .resolve(String.valueOf(patientId));

        try {
            Files.createDirectories(destinationDirectory);

            for (File sourceFile : sourceFiles) {
                if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
                    continue;
                }

                String extension = getExtension(sourceFile.getName());
                String cleanOriginalName = sanitizeFileName(removeExtension(sourceFile.getName()));
                String newFileName = cleanOriginalName + "_" + UUID.randomUUID() + extension;
                Path destination = destinationDirectory.resolve(newFileName);

                Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                savedPaths.add(destination.toAbsolutePath().toString());
            }

            return savedPaths;

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio dei file del paziente.", e);
        }
    }

    public static String joinFilePaths(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return "";
        }

        return String.join("||", filePaths);
    }

    public static List<String> splitFilePaths(String storedValue) {
        List<String> paths = new ArrayList<>();

        if (storedValue == null || storedValue.isBlank()) {
            return paths;
        }

        String[] parts = storedValue.split(PATH_SEPARATOR);

        for (String part : parts) {
            if (!part.isBlank()) {
                paths.add(part);
            }
        }

        return paths;
    }

    public static String getDisplayName(String pathValue) {
        if (pathValue == null || pathValue.isBlank()) {
            return "";
        }

        return Path.of(pathValue).getFileName().toString();
    }

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0) {
            return "";
        }

        return fileName.substring(dotIndex);
    }

    private static String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0) {
            return fileName;
        }

        return fileName.substring(0, dotIndex);
    }

    private static String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "file";
        }

        return value
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_");
    }
}
