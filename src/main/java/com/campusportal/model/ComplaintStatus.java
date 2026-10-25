package com.campusportal.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing the five complaint lifecycle states defined in the API contract (§6).
 *
 * <h2>Status Transition Rules (API Contract §6)</h2>
 * <p>Only the following forward/backward movements are allowed:
 *
 * <pre>
 *   SUBMITTED    → UNDER_REVIEW
 *   UNDER_REVIEW → IN_PROGRESS | REJECTED
 *   IN_PROGRESS  → RESOLVED    | REJECTED
 *   RESOLVED     → UNDER_REVIEW   (admin "reopen")
 *   REJECTED     → (none)         TERMINAL — no further transitions
 * </pre>
 *
 * <p>The transition table is defined once — inside this enum — so no other class needs
 * to repeat or re-check the rules. To validate a transition, call:
 * <pre>
 *   boolean ok = ComplaintStatus.isValidTransition(currentStatus, newStatus);
 * </pre>
 *
 * <p><b>This enum only defines rules — it does not perform database updates.</b>
 * The servlet is responsible for updating the database after confirming a valid transition.
 */
public enum ComplaintStatus {

    // --- API Contract §6: exactly these five values, in workflow order ---

    /**
     * The student has submitted the complaint; no admin action taken yet.
     * Every newly created complaint starts here.
     */
    SUBMITTED,

    /**
     * An admin has picked up the complaint and is reviewing it.
     */
    UNDER_REVIEW,

    /**
     * An admin is actively working to resolve the issue.
     */
    IN_PROGRESS,

    /**
     * The complaint has been fully resolved. Can be reopened to UNDER_REVIEW.
     */
    RESOLVED,

    /**
     * The complaint has been rejected. This is a <em>terminal</em> state:
     * no further transitions are allowed.
     */
    REJECTED;

    // -------------------------------------------------------
    //  Transition table  (single source of truth)
    // -------------------------------------------------------

    /**
     * Checks whether transitioning from {@code from} to {@code to} is allowed
     * by the API contract.
     *
     * <p><b>Design note for beginners:</b> We use an {@code EnumSet} for the
     * allowed targets. An {@code EnumSet} is a fast, type-safe set that only
     * works with enum values — perfect here because we only ever store
     * {@link ComplaintStatus} constants in it.
     *
     * <p>Calling this method with {@link ComplaintStatus#REJECTED} as {@code from}
     * always returns {@code false} because REJECTED is terminal.
     *
     * @param from the current status of the complaint (must not be null)
     * @param to   the desired new status (must not be null)
     * @return {@code true} if the transition is permitted; {@code false} otherwise
     * @throws IllegalArgumentException if either argument is null
     */
    public static boolean isValidTransition(ComplaintStatus from, ComplaintStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "Status arguments must not be null."
            );
        }
        return getAllowedNextStatuses(from).contains(to);
    }

    /**
     * Returns the complete set of statuses that are reachable from {@code current}
     * in a single admin action.
     *
     * <p>Returning an {@code EnumSet} makes it easy for a JSP or servlet to build
     * a dropdown list of only the permitted options.
     *
     * <p><b>Design note for beginners:</b> A {@code switch} expression is used here
     * (Java 14+ standard). Each arm returns a different {@code EnumSet}. The
     * {@code default} arm covers REJECTED (terminal) by returning an empty set.
     *
     * @param current the current status; must not be null
     * @return an unmodifiable {@link EnumSet} of valid next statuses; empty for REJECTED
     * @throws IllegalArgumentException if {@code current} is null
     */
    public static Set<ComplaintStatus> getAllowedNextStatuses(ComplaintStatus current) {
        if (current == null) {
            throw new IllegalArgumentException("Current status must not be null.");
        }
        /*
         * Switch expression (Java 14 standard form, compatible with --release 17).
         * Each case returns exactly the set of permitted next states as documented
         * in the API contract §6.
         */
        return switch (current) {
            case SUBMITTED    -> EnumSet.of(UNDER_REVIEW);
            case UNDER_REVIEW -> EnumSet.of(IN_PROGRESS, REJECTED);
            case IN_PROGRESS  -> EnumSet.of(RESOLVED, REJECTED);
            case RESOLVED     -> EnumSet.of(UNDER_REVIEW);   // admin reopen
            case REJECTED     -> EnumSet.noneOf(ComplaintStatus.class); // terminal
        };
    }

    /**
     * Convenience method: returns {@code true} if this status has no allowed
     * next states (i.e., it is a terminal state). Currently only {@link #REJECTED}
     * is terminal.
     *
     * @return {@code true} if no transitions are permitted from this status
     */
    public boolean isTerminal() {
        return getAllowedNextStatuses(this).isEmpty();
    }

    /**
     * Returns the enum constant name — this is the value stored in the database.
     * Example: {@code ComplaintStatus.UNDER_REVIEW.toDbValue()} returns
     * {@code "UNDER_REVIEW"}.
     *
     * @return the constant name as stored in the database ENUM column
     */
    public String toDbValue() {
        return this.name();
    }

    /**
     * Parses a raw String from the database into a {@link ComplaintStatus} constant.
     *
     * @param value the raw string (e.g., "SUBMITTED") — case-sensitive
     * @return the matching {@link ComplaintStatus}
     * @throws IllegalArgumentException if the value is null, blank, or unrecognised
     */
    public static ComplaintStatus fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Complaint status cannot be null or blank."
            );
        }
        try {
            return ComplaintStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown complaint status: \"" + value + "\". "
                    + "Valid values: SUBMITTED, UNDER_REVIEW, IN_PROGRESS, RESOLVED, REJECTED."
            );
        }
    }
}
