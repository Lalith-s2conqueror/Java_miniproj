package com.campusportal.util;

import java.util.regex.Pattern;

/**
 * Utility class for user input validation and sanitization.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9]+(-[A-Za-z0-9]+)*\\.)+[A-Za-z]{2,}$"
    );

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_NAME_LENGTH = 150;
    public static final int MAX_IDENTIFIER_LENGTH = 60;

    private ValidationUtil() {
        // Utility class; prevent instantiation
    }

    /**
     * Checks if a string is non-null and contains at least one non-whitespace character.
     *
     * @param str the string to test
     * @return {@code true} if not blank, {@code false} otherwise
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validates an email address format and length.
     *
     * @param email the email to validate
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (!isNotBlank(email)) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates password strength (minimum length).
     *
     * @param password the password to check
     * @return {@code true} if non-null and meets minimum length
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Checks whether password and confirmPassword match.
     *
     * @param password        the primary password
     * @param confirmPassword the confirmation password
     * @return {@code true} if both are non-null and equal
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Validates a student identifier / roll number.
     *
     * @param identifier the roll number to validate
     * @return {@code true} if not blank and within max length
     */
    public static boolean isValidIdentifier(String identifier) {
        return isNotBlank(identifier) && identifier.trim().length() <= MAX_IDENTIFIER_LENGTH;
    }

    /**
     * Validates a user's full name.
     *
     * @param fullName the name to validate
     * @return {@code true} if not blank and within max length
     */
    public static boolean isValidFullName(String fullName) {
        return isNotBlank(fullName) && fullName.trim().length() <= MAX_NAME_LENGTH;
    }

    /**
     * Safely trims a string or returns null if the string is null.
     *
     * @param str the string to trim
     * @return trimmed string or null
     */
    public static String safeTrim(String str) {
        return str == null ? null : str.trim();
    }
}
