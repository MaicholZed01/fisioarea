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
 * Copia le immagini selezionate dall'utente nella cartella dati offline.
 * In questo modo gli appuntamenti restano indipendenti dalla posizione
 * originale delle immagini sul computer.
 */
public final class ImageStorage {

    private static final String PATH_SEPARATOR = "\\|\\|";

    private ImageStorage() {
    }

    public static List<String> copyAppointmentImages(List<File> sourceFiles) {
        List<String> savedPaths = new ArrayList<>();

        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return savedPaths;
        }

        Path destinationDirectory = OfflineDataConfig.getAppointmentImagesDirectory();

        try {
            Files.createDirectories(destinationDirectory);

            for (File sourceFile : sourceFiles) {
                if (sourceFile == null || !sourceFile.exists()) {
                    continue;
                }

                String extension = getExtension(sourceFile.getName());
                String newFileName = UUID.randomUUID() + extension;
                Path destination = destinationDirectory.resolve(newFileName);

                Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                savedPaths.add(destination.toAbsolutePath().toString());
            }

            return savedPaths;

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio delle immagini dell'appuntamento.", e);
        }
    }

    public static String joinImagePaths(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return "";
        }

        return String.join("||", imagePaths);
    }

    public static List<String> splitImagePaths(String storedValue) {
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

    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0) {
            return ".img";
        }

        return fileName.substring(dotIndex);
    }
}
