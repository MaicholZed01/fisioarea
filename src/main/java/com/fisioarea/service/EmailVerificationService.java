package com.fisioarea.service;

import com.fisioarea.util.ValidationUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Verifica email tramite codice.
 *
 * Versione offline:
 * - genera un codice a 6 cifre
 * - lo mantiene temporaneamente in memoria
 * - la UI lo mostra all'utente perché il software è offline
 *
 * In futuro, per invio email reale, basta sostituire il punto in cui la UI mostra
 * il codice con un servizio SMTP o provider email.
 */
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private static final Map<String, VerificationCode> CODES = new HashMap<>();

    public VerificationCode generateCode(String email) {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        if (!ValidationUtil.isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email non valida.");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        VerificationCode verificationCode = new VerificationCode(
                normalizedEmail,
                code,
                LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES),
                0
        );

        CODES.put(normalizedEmail, verificationCode);
        return verificationCode;
    }

    public boolean verifyCode(String email, String code) {
        String normalizedEmail = ValidationUtil.normalizeEmail(email);
        String safeCode = code == null ? "" : code.trim();

        VerificationCode verificationCode = CODES.get(normalizedEmail);

        if (verificationCode == null) {
            return false;
        }

        if (verificationCode.isExpired() || verificationCode.attempts() >= MAX_ATTEMPTS) {
            CODES.remove(normalizedEmail);
            return false;
        }

        verificationCode.incrementAttempts();

        boolean valid = verificationCode.code().equals(safeCode);

        if (valid) {
            CODES.remove(normalizedEmail);
        }

        return valid;
    }

    public int getExpirationMinutes() {
        return CODE_EXPIRATION_MINUTES;
    }

    public static final class VerificationCode {

        private final String email;
        private final String code;
        private final LocalDateTime expiresAt;
        private int attempts;

        private VerificationCode(String email, String code, LocalDateTime expiresAt, int attempts) {
            this.email = email;
            this.code = code;
            this.expiresAt = expiresAt;
            this.attempts = attempts;
        }

        public String email() {
            return email;
        }

        public String code() {
            return code;
        }

        public LocalDateTime expiresAt() {
            return expiresAt;
        }

        public int attempts() {
            return attempts;
        }

        private void incrementAttempts() {
            attempts++;
        }

        private boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
