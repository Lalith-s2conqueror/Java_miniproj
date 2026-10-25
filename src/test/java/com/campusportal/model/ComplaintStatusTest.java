package com.campusportal.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ComplaintStatus}.
 *
 * <p>Tests cover:
 * <ol>
 *   <li>All five enum constants exist (API contract §6).</li>
 *   <li>Every valid transition from the contract is accepted by {@code isValidTransition()}.</li>
 *   <li>Every invalid direct jump is rejected.</li>
 *   <li>REJECTED is a terminal state — {@code isTerminal()} returns true only for it.</li>
 *   <li>Reopening: RESOLVED → UNDER_REVIEW is allowed.</li>
 *   <li>{@code getAllowedNextStatuses()} returns correct sets.</li>
 *   <li>{@code fromDbValue()} parsing round-trip.</li>
 *   <li>Null safety of all public methods.</li>
 * </ol>
 */
@DisplayName("ComplaintStatus enum and transition rules")
class ComplaintStatusTest {

    // -------------------------------------------------------
    // 1. Enum constant existence
    // -------------------------------------------------------

    @Test
    @DisplayName("All five API-contract statuses exist")
    void allStatusesExist() {
        // Verifies the API contract §6 values are present at compile+runtime.
        assertNotNull(ComplaintStatus.SUBMITTED);
        assertNotNull(ComplaintStatus.UNDER_REVIEW);
        assertNotNull(ComplaintStatus.IN_PROGRESS);
        assertNotNull(ComplaintStatus.RESOLVED);
        assertNotNull(ComplaintStatus.REJECTED);
    }

    @Test
    @DisplayName("Exactly five statuses are defined (API contract §6)")
    void exactlyFiveStatusesDefined() {
        assertEquals(5, ComplaintStatus.values().length,
                "Only the five approved statuses should exist.");
    }

    // -------------------------------------------------------
    // 2. Valid transitions (API contract §6, all 6 allowed paths)
    // -------------------------------------------------------

    /**
     * Parameterised source for all VALID transitions defined in the API contract.
     * Each Arguments row is: (from, to, descriptive label for the test name).
     */
    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(ComplaintStatus.SUBMITTED,    ComplaintStatus.UNDER_REVIEW, "SUBMITTED → UNDER_REVIEW"),
                Arguments.of(ComplaintStatus.UNDER_REVIEW, ComplaintStatus.IN_PROGRESS,  "UNDER_REVIEW → IN_PROGRESS"),
                Arguments.of(ComplaintStatus.UNDER_REVIEW, ComplaintStatus.REJECTED,     "UNDER_REVIEW → REJECTED"),
                Arguments.of(ComplaintStatus.IN_PROGRESS,  ComplaintStatus.RESOLVED,     "IN_PROGRESS → RESOLVED"),
                Arguments.of(ComplaintStatus.IN_PROGRESS,  ComplaintStatus.REJECTED,     "IN_PROGRESS → REJECTED"),
                Arguments.of(ComplaintStatus.RESOLVED,     ComplaintStatus.UNDER_REVIEW, "RESOLVED → UNDER_REVIEW (reopen)")
        );
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("validTransitions")
    @DisplayName("Valid transition is accepted")
    void validTransitionIsAccepted(ComplaintStatus from, ComplaintStatus to, String label) {
        assertTrue(ComplaintStatus.isValidTransition(from, to),
                label + " should be a valid transition.");
    }

    // -------------------------------------------------------
    // 3. Invalid transitions (direct jumps not in the contract)
    // -------------------------------------------------------

    /**
     * Parameterised source for common INVALID transitions.
     * Covers skipping steps, forward-only paths, and any exit from REJECTED.
     */
    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                // Skipping states
                Arguments.of(ComplaintStatus.SUBMITTED,    ComplaintStatus.IN_PROGRESS,  "SUBMITTED → IN_PROGRESS (skip)"),
                Arguments.of(ComplaintStatus.SUBMITTED,    ComplaintStatus.RESOLVED,     "SUBMITTED → RESOLVED (skip)"),
                Arguments.of(ComplaintStatus.SUBMITTED,    ComplaintStatus.REJECTED,     "SUBMITTED → REJECTED (skip)"),
                Arguments.of(ComplaintStatus.UNDER_REVIEW, ComplaintStatus.RESOLVED,     "UNDER_REVIEW → RESOLVED (skip)"),
                Arguments.of(ComplaintStatus.UNDER_REVIEW, ComplaintStatus.SUBMITTED,    "UNDER_REVIEW → SUBMITTED (backward)"),
                // No exit from REJECTED
                Arguments.of(ComplaintStatus.REJECTED,     ComplaintStatus.SUBMITTED,    "REJECTED → SUBMITTED (terminal)"),
                Arguments.of(ComplaintStatus.REJECTED,     ComplaintStatus.UNDER_REVIEW, "REJECTED → UNDER_REVIEW (terminal)"),
                Arguments.of(ComplaintStatus.REJECTED,     ComplaintStatus.IN_PROGRESS,  "REJECTED → IN_PROGRESS (terminal)"),
                Arguments.of(ComplaintStatus.REJECTED,     ComplaintStatus.RESOLVED,     "REJECTED → RESOLVED (terminal)"),
                // Self-transition
                Arguments.of(ComplaintStatus.SUBMITTED,    ComplaintStatus.SUBMITTED,    "SUBMITTED → SUBMITTED (self)")
        );
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("invalidTransitions")
    @DisplayName("Invalid transition is rejected")
    void invalidTransitionIsRejected(ComplaintStatus from, ComplaintStatus to, String label) {
        assertFalse(ComplaintStatus.isValidTransition(from, to),
                label + " should NOT be a valid transition.");
    }

    // -------------------------------------------------------
    // 4. REJECTED is the only terminal state
    // -------------------------------------------------------

    @Test
    @DisplayName("REJECTED.isTerminal() returns true")
    void rejectedIsTerminal() {
        assertTrue(ComplaintStatus.REJECTED.isTerminal(),
                "REJECTED must be terminal — no further transitions permitted.");
    }

    @Test
    @DisplayName("No other status is terminal")
    void onlyRejectedIsTerminal() {
        for (ComplaintStatus status : ComplaintStatus.values()) {
            if (status == ComplaintStatus.REJECTED) continue;
            assertFalse(status.isTerminal(),
                    status + " should NOT be terminal — admin must be able to progress it.");
        }
    }

    @Test
    @DisplayName("REJECTED.getAllowedNextStatuses() returns empty set")
    void rejectedHasNoNextStatuses() {
        Set<ComplaintStatus> allowed = ComplaintStatus.getAllowedNextStatuses(ComplaintStatus.REJECTED);
        assertTrue(allowed.isEmpty(),
                "Terminal state REJECTED must have no valid next statuses.");
    }

    // -------------------------------------------------------
    // 5. Reopen path (special case — contract allows RESOLVED → UNDER_REVIEW)
    // -------------------------------------------------------

    @Test
    @DisplayName("Admin can reopen a RESOLVED complaint to UNDER_REVIEW")
    void adminCanReopenResolvedComplaint() {
        assertTrue(ComplaintStatus.isValidTransition(
                        ComplaintStatus.RESOLVED, ComplaintStatus.UNDER_REVIEW),
                "RESOLVED → UNDER_REVIEW must be allowed so an admin can reopen a complaint.");
    }

    @Test
    @DisplayName("RESOLVED cannot go to any state other than UNDER_REVIEW")
    void resolvedCanOnlyGoToUnderReview() {
        Set<ComplaintStatus> allowed = ComplaintStatus.getAllowedNextStatuses(ComplaintStatus.RESOLVED);
        assertEquals(1, allowed.size());
        assertTrue(allowed.contains(ComplaintStatus.UNDER_REVIEW));
    }

    // -------------------------------------------------------
    // 6. getAllowedNextStatuses() correctness
    // -------------------------------------------------------

    @Test
    @DisplayName("SUBMITTED can only go to UNDER_REVIEW")
    void submittedAllowedNext() {
        Set<ComplaintStatus> allowed = ComplaintStatus.getAllowedNextStatuses(ComplaintStatus.SUBMITTED);
        assertEquals(Set.of(ComplaintStatus.UNDER_REVIEW), allowed);
    }

    @Test
    @DisplayName("UNDER_REVIEW can go to IN_PROGRESS or REJECTED")
    void underReviewAllowedNext() {
        Set<ComplaintStatus> allowed = ComplaintStatus.getAllowedNextStatuses(ComplaintStatus.UNDER_REVIEW);
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(ComplaintStatus.IN_PROGRESS));
        assertTrue(allowed.contains(ComplaintStatus.REJECTED));
    }

    @Test
    @DisplayName("IN_PROGRESS can go to RESOLVED or REJECTED")
    void inProgressAllowedNext() {
        Set<ComplaintStatus> allowed = ComplaintStatus.getAllowedNextStatuses(ComplaintStatus.IN_PROGRESS);
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(ComplaintStatus.RESOLVED));
        assertTrue(allowed.contains(ComplaintStatus.REJECTED));
    }

    // -------------------------------------------------------
    // 7. fromDbValue() and toDbValue() round-trip
    // -------------------------------------------------------

    @Test
    @DisplayName("fromDbValue(toDbValue()) round-trip preserves enum identity")
    void roundTripConsistency() {
        for (ComplaintStatus status : ComplaintStatus.values()) {
            assertEquals(status, ComplaintStatus.fromDbValue(status.toDbValue()),
                    "Round-trip failed for: " + status);
        }
    }

    @Test
    @DisplayName("fromDbValue() throws for unknown status string")
    void fromDbValueUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintStatus.fromDbValue("PENDING"));
    }

    @Test
    @DisplayName("fromDbValue() throws for null input")
    void fromDbValueNull() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintStatus.fromDbValue(null));
    }

    // -------------------------------------------------------
    // 8. Null safety
    // -------------------------------------------------------

    @Test
    @DisplayName("isValidTransition() throws for null 'from' argument")
    void isValidTransitionNullFrom() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintStatus.isValidTransition(null, ComplaintStatus.UNDER_REVIEW));
    }

    @Test
    @DisplayName("isValidTransition() throws for null 'to' argument")
    void isValidTransitionNullTo() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintStatus.isValidTransition(ComplaintStatus.SUBMITTED, null));
    }

    @Test
    @DisplayName("getAllowedNextStatuses() throws for null argument")
    void getAllowedNextStatusesNull() {
        assertThrows(IllegalArgumentException.class,
                () -> ComplaintStatus.getAllowedNextStatuses(null));
    }
}
