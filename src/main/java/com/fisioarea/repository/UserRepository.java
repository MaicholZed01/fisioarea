package com.fisioarea.repository;

import com.fisioarea.model.User;
import com.fisioarea.util.FileStorage;
import com.fisioarea.util.OfflineDataConfig;
import com.fisioarea.util.PasswordUtil;
import com.fisioarea.util.ValidationUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private static final int COLUMN_COUNT = 8;
    private static final String HEADER = "id;nome;cognome;email;password;nomeStudio;telefono;ruolo";

    private static final UserRepository INSTANCE = new UserRepository();

    private final Path file = OfflineDataConfig.getUsersFile();
    private final List<User> users = new ArrayList<>();

    private UserRepository() {
        loadFromFile();

        if (users.isEmpty()) {
            users.add(new User(
                    1,
                    "Michele",
                    "Cardos",
                    "admin@fisioarea.it",
                    PasswordUtil.hashPassword("admin123"),
                    "Fisioarea",
                    "+39 000 000 0000",
                    "Admin"
            ));
            saveAllToFile();
        } else {
            migratePlainTextPasswordsIfNeeded();
        }
    }

    public static UserRepository getInstance() {
        return INSTANCE;
    }

    public Optional<User> findById(int id) {
        return users.stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .map(User::copy);
    }

    public Optional<User> findByEmail(String email) {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .map(User::copy);
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public boolean existsByEmailForOtherUser(String email, int userId) {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        return users.stream()
                .anyMatch(user -> user.getId() != userId && user.getEmail().equalsIgnoreCase(normalizedEmail));
    }

    public User save(User user) {
        users.add(user.copy());
        saveAllToFile();
        return user.copy();
    }

    public User update(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updatedUser.getId()) {
                users.set(i, updatedUser.copy());
                saveAllToFile();
                return updatedUser.copy();
            }
        }

        throw new IllegalArgumentException("Utente non trovato: " + updatedUser.getId());
    }

    public void deleteById(int userId) {
        boolean removed = users.removeIf(user -> user.getId() == userId);

        if (!removed) {
            throw new IllegalArgumentException("Utente non trovato: " + userId);
        }

        saveAllToFile();
    }

    public List<User> findAll() {
        return users.stream()
                .map(User::copy)
                .toList();
    }

    public int nextId() {
        return users.stream()
                .map(User::getId)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private void loadFromFile() {
        users.clear();

        List<List<String>> rows = FileStorage.readRows(file, COLUMN_COUNT);

        for (List<String> row : rows) {
            users.add(new User(
                    Integer.parseInt(row.get(0)),
                    row.get(1),
                    row.get(2),
                    ValidationUtil.normalizeEmail(row.get(3)),
                    row.get(4),
                    row.get(5),
                    row.get(6),
                    row.get(7)
            ));
        }
    }

    private void saveAllToFile() {
        List<List<String>> rows = new ArrayList<>();

        for (User user : users) {
            rows.add(List.of(
                    String.valueOf(user.getId()),
                    user.getNome(),
                    user.getCognome(),
                    ValidationUtil.normalizeEmail(user.getEmail()),
                    user.getPassword(),
                    user.getNomeStudio(),
                    user.getTelefono(),
                    user.getRuolo()
            ));
        }

        FileStorage.writeRows(file, HEADER, rows);
    }

    private void migratePlainTextPasswordsIfNeeded() {
        boolean changed = false;

        for (User user : users) {
            if (!PasswordUtil.isHashed(user.getPassword())) {
                user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
                changed = true;
            }
        }

        if (changed) {
            saveAllToFile();
        }
    }
}
