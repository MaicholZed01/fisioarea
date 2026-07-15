package com.fisioarea.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9 +()\\-./]{5,25}$"
    );

    private ValidationUtil() {
    }

    public static boolean isValidName(String value) {
        return value != null && value.trim().length() >= 2;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return true;
        }

        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidStudioName(String value) {
        return value != null && value.trim().length() >= 2;
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasLetter && hasDigit;
    }

    public static String passwordRulesMessage() {
        return "La password deve contenere almeno 8 caratteri, una lettera e un numero.";
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
