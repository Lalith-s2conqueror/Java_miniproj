package com.campusportal.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtil tests")
class ValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "student@campus.edu",
            "alice.fernandes@univ.ac.in",
            "user123+tag@mail.com",
            "first.last@dept.sub.org"
    })
    @DisplayName("Valid email formats return true")
    void testValidEmails(String email) {
        assertTrue(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "plainaddress",
            "@missingusername.com",
            "username@.com",
            "username@com",
            "username@domain..com"
    })
    @DisplayName("Invalid email formats return false")
    void testInvalidEmails(String email) {
        assertFalse(ValidationUtil.isValidEmail(email));
    }

    @Test
    @DisplayName("Null email returns false")
    void testNullEmail() {
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    @DisplayName("Password length validation")
    void testPasswordLength() {
        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword("12345")); // 5 chars
        assertTrue(ValidationUtil.isValidPassword("123456"));  // 6 chars
        assertTrue(ValidationUtil.isValidPassword("StrongPass#2026"));
    }

    @Test
    @DisplayName("Password confirmation match check")
    void testPasswordsMatch() {
        assertTrue(ValidationUtil.doPasswordsMatch("pass123", "pass123"));
        assertFalse(ValidationUtil.doPasswordsMatch("pass123", "pass456"));
        assertFalse(ValidationUtil.doPasswordsMatch("pass123", null));
        assertFalse(ValidationUtil.doPasswordsMatch(null, "pass123"));
        assertFalse(ValidationUtil.doPasswordsMatch(null, null));
    }

    @Test
    @DisplayName("Identifier / Roll number validation")
    void testIdentifierValidation() {
        assertTrue(ValidationUtil.isValidIdentifier("STU-2023-001"));
        assertFalse(ValidationUtil.isValidIdentifier(null));
        assertFalse(ValidationUtil.isValidIdentifier(""));
        assertFalse(ValidationUtil.isValidIdentifier("   "));
        // Excessively long identifier
        String tooLong = "A".repeat(61);
        assertFalse(ValidationUtil.isValidIdentifier(tooLong));
    }

    @Test
    @DisplayName("Full name validation")
    void testFullNameValidation() {
        assertTrue(ValidationUtil.isValidFullName("Alice Fernandes"));
        assertFalse(ValidationUtil.isValidFullName(null));
        assertFalse(ValidationUtil.isValidFullName(""));
        assertFalse(ValidationUtil.isValidFullName("   "));
        String tooLong = "A".repeat(151);
        assertFalse(ValidationUtil.isValidFullName(tooLong));
    }

    @Test
    @DisplayName("safeTrim trims or returns null")
    void testSafeTrim() {
        assertNull(ValidationUtil.safeTrim(null));
        assertEquals("hello", ValidationUtil.safeTrim("  hello  "));
    }
}
