package com.campusportal.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ComplaintCategory}.
 *
 * Tests verify:
 *  - All five enum constants exist and match the API contract.
 *  - Display labels are correct.
 *  - fromDbValue() accepts valid values (case-insensitive).
 *  - fromDbValue() rejects unknown values and null/blank input.
 *  - toDbValue() returns the database-stored constant name.
 */
@DisplayName("ComplaintCategory enum")
class ComplaintCategoryTest {

    // -------------------------------------------------------
    // 1. Enum constant existence
    // -------------------------------------------------------

    @Test
    @DisplayName("All five API-contract categories exist")
    void allCategoriesExist() {
        // These five values are locked in the API contract §5.
        // If any is missing, the DAO and form validation will break.
        assertNotNull(ComplaintCategory.INFRASTRUCTURE);
        assertNotNull(ComplaintCategory.SANITATION);
        assertNotNull(ComplaintCategory.ACADEMICS);
        assertNotNull(ComplaintCategory.IT);
        assertNotNull(ComplaintCategory.OTHER);
    }

    @Test
    @DisplayName("Exactly five categories are defined (API contract §5)")
    void exactlyFiveCategoriesDefined() {
        assertEquals(5, ComplaintCategory.values().length,
                "Only the five approved categories should exist. "
                + "Add new categories to the API contract first.");
    }

    // -------------------------------------------------------
    // 2. Display labels
    // -------------------------------------------------------

    @Test
    @DisplayName("Display labels match the API contract §5")
    void displayLabelsMatchContract() {
        assertEquals("Infrastructure & Facilities", ComplaintCategory.INFRASTRUCTURE.getDisplayLabel());
        assertEquals("Sanitation & Cleanliness",    ComplaintCategory.SANITATION.getDisplayLabel());
        assertEquals("Academics & Classroom Issues", ComplaintCategory.ACADEMICS.getDisplayLabel());
        assertEquals("IT & Technology",              ComplaintCategory.IT.getDisplayLabel());
        assertEquals("Other",                        ComplaintCategory.OTHER.getDisplayLabel());
    }

    // -------------------------------------------------------
    // 3. fromDbValue() – valid input
    // -------------------------------------------------------

    @Test
    @DisplayName("fromDbValue() parses exact match")
    void fromDbValueExactMatch() {
        assertEquals(ComplaintCategory.IT, ComplaintCategory.fromDbValue("IT"));
        assertEquals(ComplaintCategory.SANITATION, ComplaintCategory.fromDbValue("SANITATION"));
        assertEquals(ComplaintCategory.OTHER, ComplaintCategory.fromDbValue("OTHER"));
    }

    @Test
    @DisplayName("fromDbValue() accepts and trims surrounding whitespace")
    void fromDbValueTrimsWhitespace() {
        assertEquals(ComplaintCategory.INFRASTRUCTURE,
                ComplaintCategory.fromDbValue("  INFRASTRUCTURE  "));
    }

    @Test
    @DisplayName("fromDbValue() is case-insensitive via toUpperCase normalisation")
    void fromDbValueCaseInsensitive() {
        // The method normalises to upper case before calling valueOf()
        assertEquals(ComplaintCategory.ACADEMICS, ComplaintCategory.fromDbValue("academics"));
        assertEquals(ComplaintCategory.IT, ComplaintCategory.fromDbValue("it"));
    }

    // -------------------------------------------------------
    // 4. fromDbValue() – invalid input
    // -------------------------------------------------------

    @Test
    @DisplayName("fromDbValue() throws IllegalArgumentException for unknown value")
    void fromDbValueUnknownValue() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintCategory.fromDbValue("FOOD"),
                "A category not in the contract must be rejected.");
    }

    @Test
    @DisplayName("fromDbValue() throws for null input")
    void fromDbValueNull() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintCategory.fromDbValue(null));
    }

    @Test
    @DisplayName("fromDbValue() throws for blank string input")
    void fromDbValueBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintCategory.fromDbValue("   "));
    }

    @Test
    @DisplayName("fromDbValue() throws for empty string input")
    void fromDbValueEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintCategory.fromDbValue(""));
    }

    // -------------------------------------------------------
    // 5. toDbValue()
    // -------------------------------------------------------

    @Test
    @DisplayName("toDbValue() returns the constant name stored in the database")
    void toDbValueMatchesConstantName() {
        assertEquals("INFRASTRUCTURE", ComplaintCategory.INFRASTRUCTURE.toDbValue());
        assertEquals("SANITATION",     ComplaintCategory.SANITATION.toDbValue());
        assertEquals("ACADEMICS",      ComplaintCategory.ACADEMICS.toDbValue());
        assertEquals("IT",             ComplaintCategory.IT.toDbValue());
        assertEquals("OTHER",          ComplaintCategory.OTHER.toDbValue());
    }

    @Test
    @DisplayName("fromDbValue(toDbValue()) round-trip returns same constant")
    void roundTripConsistency() {
        for (ComplaintCategory category : ComplaintCategory.values()) {
            assertEquals(category, ComplaintCategory.fromDbValue(category.toDbValue()),
                    "Round-trip failed for: " + category);
        }
    }
}
