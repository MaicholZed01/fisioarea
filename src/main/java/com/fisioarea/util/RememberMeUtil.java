package com.fisioarea.util;

import com.fisioarea.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Gestisce il "Ricordami" in modalità offline.
 *
 * Non salva la password.
 * Salva solo id utente ed email in un file locale per ripristinare la sessione.
 */
public final class RememberMeUtil {

    private static final int COLUMN_COUNT = 2;
    private static final String HEADER = "userId;email";

    private RememberMeUtil() {
    }

    public static void save(User user) {
        if (user == null) {
            clear();
            return;
        }

        FileStorage.writeRows(
                OfflineDataConfig.getRememberMeFile(),
                HEADER,
                List.of(List.of(
                        String.valueOf(user.getId()),
                        ValidationUtil.normalizeEmail(user.getEmail())
                ))
        );
    }

    public static Optional<RememberedLogin> load() {
        List<List<String>> rows = FileStorage.readRows(
                OfflineDataConfig.getRememberMeFile(),
                COLUMN_COUNT
        );

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        List<String> row = rows.get(0);

        try {
            int userId = Integer.parseInt(row.get(0));
            String email = ValidationUtil.normalizeEmail(row.get(1));

            if (userId <= 0 || !ValidationUtil.isValidEmail(email)) {
                clear();
                return Optional.empty();
            }

            return Optional.of(new RememberedLogin(userId, email));

        } catch (NumberFormatException e) {
            clear();
            return Optional.empty();
        }
    }

    public static Optional<String> getRememberedEmail() {
        return load().map(RememberedLogin::getEmail);
    }

    public static void clear() {
        Path file = OfflineDataConfig.getRememberMeFile();

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile cancellare il file Ricordami.", e);
        }
    }

    public static final class RememberedLogin {

        private final int userId;
        private final String email;

        public RememberedLogin(int userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        public int getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }
    }
}
