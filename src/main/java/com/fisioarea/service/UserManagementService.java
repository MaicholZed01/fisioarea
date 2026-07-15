package com.fisioarea.service;

import com.fisioarea.model.User;
import com.fisioarea.repository.UserRepository;
import com.fisioarea.util.PasswordUtil;
import com.fisioarea.util.ValidationUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UserManagementService {

    private final UserRepository userRepository = UserRepository.getInstance();

    public boolean isCurrentUserAdmin() {
        User currentUser = AuthService.getCurrentUser();
        return currentUser != null && isAdminRole(currentUser.getRuolo());
    }

    public List<User> getManagedUsers() {
        requireAdmin();

        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getRuolo)
                        .thenComparing(User::getCognome)
                        .thenComparing(User::getNome))
                .toList();
    }

    public List<User> getNonAdminUsers() {
        requireAdmin();

        return userRepository.findAll().stream()
                .filter(user -> !isAdminRole(user.getRuolo()))
                .sorted(Comparator.comparing(User::getCognome).thenComparing(User::getNome))
                .toList();
    }

    public AuthService.ServiceResult createUser(String nome, String cognome, String email,
                                                String password, String confirmPassword,
                                                String nomeStudio, String telefono, String ruolo) {
        requireAdmin();

        String normalizedEmail = ValidationUtil.normalizeEmail(email);
        String normalizedRole = normalizeRole(ruolo);

        AuthService.ServiceResult validation = validateUserFields(
                0,
                nome,
                cognome,
                normalizedEmail,
                nomeStudio,
                telefono,
                normalizedRole
        );

        if (!validation.isSuccess()) {
            return validation;
        }

        if (!ValidationUtil.isStrongPassword(password)) {
            return AuthService.ServiceResult.failure(ValidationUtil.passwordRulesMessage());
        }

        if (!password.equals(confirmPassword)) {
            return AuthService.ServiceResult.failure("Le password non coincidono.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            return AuthService.ServiceResult.failure("Esiste già un account con questa email.");
        }

        User user = new User(
                userRepository.nextId(),
                nome,
                cognome,
                normalizedEmail,
                PasswordUtil.hashPassword(password),
                nomeStudio,
                telefono,
                normalizedRole,
                true
        );

        userRepository.save(user);
        return AuthService.ServiceResult.success("Profilo creato correttamente.");
    }

    public AuthService.ServiceResult updateUser(int userId, String nome, String cognome, String email,
                                                String nomeStudio, String telefono, String ruolo,
                                                String newPassword, String confirmNewPassword) {
        requireAdmin();

        Optional<User> existingUser = userRepository.findById(userId);

        if (existingUser.isEmpty()) {
            return AuthService.ServiceResult.failure("Profilo non trovato.");
        }

        User currentUser = AuthService.getCurrentUser();

        if (currentUser == null) {
            return AuthService.ServiceResult.failure("Sessione scaduta. Effettua nuovamente il login.");
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(email);
        String normalizedRole = normalizeRole(ruolo);

        AuthService.ServiceResult validation = validateUserFields(
                userId,
                nome,
                cognome,
                normalizedEmail,
                nomeStudio,
                telefono,
                normalizedRole
        );

        if (!validation.isSuccess()) {
            return validation;
        }

        User originalUser = existingUser.get();

        if (currentUser.getId() == originalUser.getId()
                && !originalUser.getRuolo().equalsIgnoreCase(normalizedRole)) {
            return AuthService.ServiceResult.failure("Non puoi modificare il tuo ruolo personale.");
        }

        if (isAdminRole(originalUser.getRuolo()) && !isAdminRole(normalizedRole) && countAdmins() <= 1) {
            return AuthService.ServiceResult.failure("Non puoi rimuovere il ruolo Admin dall'ultimo Admin rimasto.");
        }

        boolean emailChanged = !originalUser.getEmail().equalsIgnoreCase(normalizedEmail);

        User updatedUser = originalUser.copy();
        updatedUser.setNome(nome);
        updatedUser.setCognome(cognome);
        updatedUser.setEmail(normalizedEmail);
        updatedUser.setNomeStudio(nomeStudio);
        updatedUser.setTelefono(telefono);
        updatedUser.setRuolo(normalizedRole);

        if (emailChanged) {
            updatedUser.setEmailVerified(true);
        }

        boolean passwordChangeRequested = newPassword != null || confirmNewPassword != null;
        String safePassword = newPassword == null ? "" : newPassword;
        String safeConfirmPassword = confirmNewPassword == null ? "" : confirmNewPassword;

        if (passwordChangeRequested && (!safePassword.isBlank() || !safeConfirmPassword.isBlank())) {
            if (!ValidationUtil.isStrongPassword(safePassword)) {
                return AuthService.ServiceResult.failure(ValidationUtil.passwordRulesMessage());
            }

            if (!safePassword.equals(safeConfirmPassword)) {
                return AuthService.ServiceResult.failure("Le nuove password non coincidono.");
            }

            updatedUser.setPassword(PasswordUtil.hashPassword(safePassword));
        }

        userRepository.update(updatedUser);
        return AuthService.ServiceResult.success("Profilo aggiornato correttamente.");
    }

    public AuthService.ServiceResult deleteUser(int userId) {
        requireAdmin();

        Optional<User> existingUser = userRepository.findById(userId);

        if (existingUser.isEmpty()) {
            return AuthService.ServiceResult.failure("Profilo non trovato.");
        }

        User currentUser = AuthService.getCurrentUser();

        if (currentUser == null) {
            return AuthService.ServiceResult.failure("Sessione scaduta. Effettua nuovamente il login.");
        }

        if (currentUser.getId() == userId) {
            return AuthService.ServiceResult.failure("Non puoi eliminare il profilo con cui hai effettuato l'accesso.");
        }

        if (isAdminRole(existingUser.get().getRuolo()) && countAdmins() <= 1) {
            return AuthService.ServiceResult.failure("Non puoi eliminare l'ultimo Admin rimasto.");
        }

        userRepository.deleteById(userId);
        return AuthService.ServiceResult.success("Profilo eliminato correttamente.");
    }

    public AuthService.ServiceResult createNonAdminUser(String nome, String cognome, String email,
                                                        String password, String confirmPassword,
                                                        String nomeStudio, String telefono, String ruolo) {
        if (isAdminRole(ruolo)) {
            return AuthService.ServiceResult.failure("Usa createUser per creare profili Admin.");
        }

        return createUser(nome, cognome, email, password, confirmPassword, nomeStudio, telefono, ruolo);
    }

    public AuthService.ServiceResult updateNonAdminUser(int userId, String nome, String cognome, String email,
                                                        String nomeStudio, String telefono, String ruolo,
                                                        String newPassword, String confirmNewPassword) {
        Optional<User> existingUser = userRepository.findById(userId);

        if (existingUser.isPresent() && isAdminRole(existingUser.get().getRuolo())) {
            return AuthService.ServiceResult.failure("Questo profilo è Admin. Usa la gestione completa utenti.");
        }

        if (isAdminRole(ruolo)) {
            return AuthService.ServiceResult.failure("Questa sezione non può assegnare il ruolo Admin.");
        }

        return updateUser(userId, nome, cognome, email, nomeStudio, telefono, ruolo, newPassword, confirmNewPassword);
    }

    public AuthService.ServiceResult deleteNonAdminUser(int userId) {
        Optional<User> existingUser = userRepository.findById(userId);

        if (existingUser.isPresent() && isAdminRole(existingUser.get().getRuolo())) {
            return AuthService.ServiceResult.failure("Questo profilo è Admin. Usa la gestione completa utenti.");
        }

        return deleteUser(userId);
    }

    public List<String> getAssignableRoles() {
        return List.of("Admin", "Fisioterapista", "Segreteria");
    }

    public List<String> getNonAdminRoles() {
        return List.of("Fisioterapista", "Segreteria");
    }

    private AuthService.ServiceResult validateUserFields(int userId, String nome, String cognome, String email,
                                                        String nomeStudio, String telefono, String ruolo) {
        if (!ValidationUtil.isValidName(nome)) {
            return AuthService.ServiceResult.failure("Il nome deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidName(cognome)) {
            return AuthService.ServiceResult.failure("Il cognome deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidEmail(email)) {
            return AuthService.ServiceResult.failure("Inserisci un indirizzo email valido.");
        }

        if (!ValidationUtil.isValidStudioName(nomeStudio)) {
            return AuthService.ServiceResult.failure("Il nome dello studio deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidPhone(telefono)) {
            return AuthService.ServiceResult.failure("Il numero di telefono contiene caratteri non validi.");
        }

        if (!isAllowedRole(ruolo)) {
            return AuthService.ServiceResult.failure("Ruolo non valido.");
        }

        if (userId > 0 && userRepository.existsByEmailForOtherUser(email, userId)) {
            return AuthService.ServiceResult.failure("Questa email è già usata da un altro account.");
        }

        return AuthService.ServiceResult.success("Validazione completata.");
    }

    private void requireAdmin() {
        if (!isCurrentUserAdmin()) {
            throw new SecurityException("Operazione consentita solo agli Admin.");
        }
    }

    private long countAdmins() {
        return userRepository.findAll().stream()
                .filter(user -> isAdminRole(user.getRuolo()))
                .count();
    }

    private boolean isAdminRole(String ruolo) {
        return ruolo != null && ruolo.trim().equalsIgnoreCase("Admin");
    }

    private boolean isAllowedRole(String ruolo) {
        return getAssignableRoles().stream()
                .anyMatch(role -> role.equalsIgnoreCase(ruolo == null ? "" : ruolo.trim()));
    }

    private String normalizeRole(String ruolo) {
        return getAssignableRoles().stream()
                .filter(role -> role.equalsIgnoreCase(ruolo == null ? "" : ruolo.trim()))
                .findFirst()
                .orElse("Fisioterapista");
    }
}
