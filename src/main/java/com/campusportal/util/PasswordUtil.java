package com.campusportal.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for BCrypt password hashing and verification.
 *
 * <p>Uses {@code org.mindrot:jbcrypt} with a cost factor (log rounds) of 12.
 * Plaintext passwords must NEVER be logged, printed, or stored in plaintext.
 */
public final class PasswordUtil {

    /**
     * BCrypt work factor (log rounds). 12 is recommended for good security/performance balance.
     */
    public static final int WORK_FACTOR = 12;

    private PasswordUtil() {
        // Utility class; prevent instantiation
    }

    /**
     * Hashes a plaintext password using BCrypt with work factor 12.
     *
     * @param plainTextPassword the plaintext password to hash; must not be null or empty
     * @return the resulting BCrypt hash string
     * @throws IllegalArgumentException if the password is null or empty
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty for hashing");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param plainTextPassword the plaintext password to verify
     * @param hashedPassword    the stored BCrypt hash string
     * @return {@code true} if the password matches the hash, {@code false} otherwise or if inputs are invalid
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()
                || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Malformed hash string (e.g. invalid salt or not a valid BCrypt hash)
            return false;
        }
    }
}
