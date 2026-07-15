package com.fisioarea.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configurazione centrale per il salvataggio offline.
 *
 * Tutti i dati vengono salvati nella cartella utente:
 * <cartella_utente>/FisioareaData
 */
public final class OfflineDataConfig {

    private static final Path DATA_DIRECTORY = Paths.get(
            System.getProperty("user.home"),
            "FisioareaData"
    );

    private OfflineDataConfig() {
    }

    public static Path getDataDirectory() {
        return DATA_DIRECTORY;
    }

    public static Path getUsersFile() {
        return DATA_DIRECTORY.resolve("users.fisioarea");
    }

    public static Path getPazientiFile() {
        return DATA_DIRECTORY.resolve("pazienti.fisioarea");
    }

    public static Path getAppuntamentiFile() {
        return DATA_DIRECTORY.resolve("appuntamenti.fisioarea");
    }

    public static Path getAppointmentImagesDirectory() {
        return DATA_DIRECTORY.resolve("immagini_appuntamenti");
    }

    public static Path getPatientFilesDirectory() {
        return DATA_DIRECTORY.resolve("file_pazienti");
    }

    public static Path getRememberMeFile() {
        return DATA_DIRECTORY.resolve("remember_me.fisioarea");
    }
}
