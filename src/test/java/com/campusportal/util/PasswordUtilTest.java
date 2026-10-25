package com.campusportal.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtil tests")
class PasswordUtilTest {

    @Test
    @DisplayName("Hash starts with BCrypt prefix and cost factor 12")
    void testHashFormat() {
        String hash = PasswordUtil.hashPassword("Secret123!");
        assertNotNull(hash);
        // BCrypt work factor 12 hash starts with $2a$12$
        assertTrue(hash.startsWith("$2a$12$") || hash.startsWith("$2b$12$"),
                "Hash should start with $2a$12$ or $2b$12$, but was: " + hash);
    }

    @Test
    @DisplayName("Valid password matches generated hash")
    void testCheckPasswordSuccess() {
        String password = "MySecurePassword#2026";
        String hash = PasswordUtil.hashPassword(password);
        assertTrue(PasswordUtil.checkPassword(password, hash));
    }

    @Test
    @DisplayName("Incorrect password does not match hash")
    void testCheckPasswordFailure() {
        String hash = PasswordUtil.hashPassword("CorrectPass");
        assertFalse(PasswordUtil.checkPassword("WrongPass", hash));
    }

    @Test
    @DisplayName("Empty or null inputs for checkPassword return false safely")
    void testCheckPasswordNullOrEmpty() {
        String hash = PasswordUtil.hashPassword("ValidPass");
        assertFalse(PasswordUtil.checkPassword(null, hash));
        assertFalse(PasswordUtil.checkPassword("", hash));
        assertFalse(PasswordUtil.checkPassword("ValidPass", null));
        assertFalse(PasswordUtil.checkPassword("ValidPass", ""));
        assertFalse(PasswordUtil.checkPassword(null, null));
    }

    @Test
    @DisplayName("Malformed hash does not throw uncaught exception")
    void testMalformedHash() {
        assertFalse(PasswordUtil.checkPassword("password", "not-a-bcrypt-hash"));
    }

    @Test
    @DisplayName("Null or empty password to hash throws IllegalArgumentException")
    void testHashNullOrEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));
    }

    @Test
    @DisplayName("Two hashes of the same password produce distinct salts and strings")
    void testSaltUniqueness() {
        String hash1 = PasswordUtil.hashPassword("SamePassword");
        String hash2 = PasswordUtil.hashPassword("SamePassword");
        assertNotEquals(hash1, hash2, "Distinct BCrypt salts must produce distinct hashes");
        assertTrue(PasswordUtil.checkPassword("SamePassword", hash1));
        assertTrue(PasswordUtil.checkPassword("SamePassword", hash2));
    }
}
