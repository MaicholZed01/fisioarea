package com.fisioarea.service;

import com.fisioarea.model.User;
import com.fisioarea.repository.UserRepository;
import com.fisioarea.util.PasswordUtil;
import com.fisioarea.util.RememberMeUtil;
import com.fisioarea.util.ValidationUtil;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository = UserRepository.getInstance();
    private static User currentUser;

    public ServiceResult login(String email, String password) {
        return login(email, password, false);
    }

    public ServiceResult login(String email, String password, boolean rememberMe) {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        if (!ValidationUtil.isValidEmail(normalizedEmail) || password == null || password.isBlank()) {
            return ServiceResult.failure("Inserisci email e password validi.");
        }

        Optional<User> user = userRepository.findByEmail(normalizedEmail);

        if (user.isEmpty() || !PasswordUtil.verifyPassword(password, user.get().getPassword())) {
            return ServiceResult.failure("Email o password non corretti.");
        }

        if (!user.get().isEmailVerified()) {
            return ServiceResult.failure("Email non verificata. Completa la verifica prima di accedere.");
        }

        currentUser = user.get();

        if (rememberMe) {
            RememberMeUtil.save(currentUser);
        } else {
            RememberMeUtil.clear();
        }

        return ServiceResult.success("Accesso effettuato.");
    }

    public boolean restoreRememberedSession() {
        Optional<RememberMeUtil.RememberedLogin> rememberedLogin = RememberMeUtil.load();

        if (rememberedLogin.isEmpty()) {
            return false;
        }

        RememberMeUtil.RememberedLogin remembered = rememberedLogin.get();

        Optional<User> userById = userRepository.findById(remembered.getUserId());

        if (userById.isPresent()
                && userById.get().getEmail().equalsIgnoreCase(remembered.getEmail())
                && userById.get().isEmailVerified()) {
            currentUser = userById.get();
            return true;
        }

        Optional<User> userByEmail = userRepository.findByEmail(remembered.getEmail());

        if (userByEmail.isPresent() && userByEmail.get().isEmailVerified()) {
            currentUser = userByEmail.get();
            RememberMeUtil.save(currentUser);
            return true;
        }

        RememberMeUtil.clear();
        return false;
    }

    public Optional<String> getRememberedEmail() {
        return RememberMeUtil.getRememberedEmail();
    }

    public ServiceResult register(String nome, String cognome, String email, String password,
                                  String confirmPassword, String nomeStudio, String telefono, String ruolo,
                                  boolean acceptedTerms) {
        return register(nome, cognome, email, password, confirmPassword, nomeStudio, telefono, ruolo, acceptedTerms, true);
    }

    public ServiceResult register(String nome, String cognome, String email, String password,
                                  String confirmPassword, String nomeStudio, String telefono, String ruolo,
                                  boolean acceptedTerms, boolean emailVerified) {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        ServiceResult validation = validateProfileFields(nome, cognome, normalizedEmail, nomeStudio, telefono, 0);
        if (!validation.isSuccess()) {
            return validation;
        }

        if (ruolo == null || ruolo.isBlank()) {
            return ServiceResult.failure("Seleziona un ruolo.");
        }

        if (!acceptedTerms) {
            return ServiceResult.failure("Devi accettare termini di servizio e informativa privacy.");
        }

        if (!emailVerified) {
            return ServiceResult.failure("Devi verificare l'email prima di creare l'account.");
        }

        if (!ValidationUtil.isStrongPassword(password)) {
            return ServiceResult.failure(ValidationUtil.passwordRulesMessage());
        }

        if (!password.equals(confirmPassword)) {
            return ServiceResult.failure("Le password non coincidono.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            return ServiceResult.failure("Esiste già un account con questa email.");
        }

        User user = new User(
                userRepository.nextId(),
                nome,
                cognome,
                normalizedEmail,
                PasswordUtil.hashPassword(password),
                nomeStudio,
                telefono,
                ruolo,
                true
        );

        currentUser = userRepository.save(user);
        return ServiceResult.success("Account creato correttamente.");
    }

    public ServiceResult updateCurrentUserProfile(String nome, String cognome, String email,
                                                  String nomeStudio, String telefono) {
        return updateCurrentUserProfile(nome, cognome, email, nomeStudio, telefono, false);
    }

    public ServiceResult updateCurrentUserProfile(String nome, String cognome, String email,
                                                  String nomeStudio, String telefono,
                                                  boolean emailVerifiedByCode) {
        if (currentUser == null) {
            return ServiceResult.failure("Sessione scaduta. Effettua nuovamente il login.");
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        ServiceResult validation = validateProfileFields(
                nome,
                cognome,
                normalizedEmail,
                nomeStudio,
                telefono,
                currentUser.getId()
        );

        if (!validation.isSuccess()) {
            return validation;
        }

        Optional<User> freshUser = userRepository.findById(currentUser.getId());

        if (freshUser.isEmpty()) {
            return ServiceResult.failure("Utente non trovato.");
        }

        User updatedUser = freshUser.get();

        boolean emailChanged = !updatedUser.getEmail().equalsIgnoreCase(normalizedEmail);

        if (emailChanged && !emailVerifiedByCode) {
            return ServiceResult.failure("Per modificare l'email devi prima verificarla con il codice.");
        }

        updatedUser.setNome(nome);
        updatedUser.setCognome(cognome);
        updatedUser.setEmail(normalizedEmail);
        updatedUser.setNomeStudio(nomeStudio);
        updatedUser.setTelefono(telefono);

        if (emailChanged) {
            updatedUser.setEmailVerified(true);
        }

        // Il ruolo viene volutamente preservato.
        // Può essere modificato solo da un Admin nella sezione Gestione utenti.
        currentUser = userRepository.update(updatedUser);

        if (RememberMeUtil.load().isPresent()) {
            RememberMeUtil.save(currentUser);
        }

        return ServiceResult.success("Profilo aggiornato correttamente.");
    }

    public ServiceResult changeCurrentUserPassword(String currentPassword, String newPassword, String confirmNewPassword) {
        if (currentUser == null) {
            return ServiceResult.failure("Sessione scaduta. Effettua nuovamente il login.");
        }

        Optional<User> freshUser = userRepository.findById(currentUser.getId());

        if (freshUser.isEmpty()) {
            return ServiceResult.failure("Utente non trovato.");
        }

        if (!PasswordUtil.verifyPassword(currentPassword, freshUser.get().getPassword())) {
            return ServiceResult.failure("La password attuale non è corretta.");
        }

        if (!ValidationUtil.isStrongPassword(newPassword)) {
            return ServiceResult.failure(ValidationUtil.passwordRulesMessage());
        }

        if (!newPassword.equals(confirmNewPassword)) {
            return ServiceResult.failure("Le nuove password non coincidono.");
        }

        User updatedUser = freshUser.get();
        updatedUser.setPassword(PasswordUtil.hashPassword(newPassword));

        currentUser = userRepository.update(updatedUser);
        return ServiceResult.success("Password aggiornata correttamente.");
    }

    public static User getCurrentUser() {
        return currentUser == null ? null : currentUser.copy();
    }

    public static void logout() {
        currentUser = null;
        RememberMeUtil.clear();
    }

    private ServiceResult validateProfileFields(String nome, String cognome, String email,
                                                String nomeStudio, String telefono,
                                                int currentUserId) {
        if (!ValidationUtil.isValidName(nome)) {
            return ServiceResult.failure("Il nome deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidName(cognome)) {
            return ServiceResult.failure("Il cognome deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidEmail(email)) {
            return ServiceResult.failure("Inserisci un indirizzo email valido.");
        }

        if (!ValidationUtil.isValidStudioName(nomeStudio)) {
            return ServiceResult.failure("Il nome dello studio deve contenere almeno 2 caratteri.");
        }

        if (!ValidationUtil.isValidPhone(telefono)) {
            return ServiceResult.failure("Il numero di telefono contiene caratteri non validi.");
        }

        if (currentUserId > 0 && userRepository.existsByEmailForOtherUser(email, currentUserId)) {
            return ServiceResult.failure("Questa email è già usata da un altro account.");
        }

        return ServiceResult.success("Validazione completata.");
    }

    public static final class ServiceResult {

        private final boolean success;
        private final String message;

        private ServiceResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ServiceResult success(String message) {
            return new ServiceResult(true, message);
        }

        public static ServiceResult failure(String message) {
            return new ServiceResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
