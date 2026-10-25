package com.campusportal.model;

/**
 * Enum representing the five complaint categories defined in the API contract (§5).
 *
 * <p>The database stores category as a plain VARCHAR(60). This enum is the single source
 * of truth for valid values inside the application. Any user-supplied category string
 * must be validated against this enum before being written to the database.
 *
 * <p>Usage example (in a servlet):
 * <pre>
 *   String raw = request.getParameter("category");
 *   ComplaintCategory category = ComplaintCategory.fromDbValue(raw); // validates and parses
 * </pre>
 *
 * <p><b>Do not add new categories without updating the API contract and schema comments.</b>
 */
public enum ComplaintCategory {

    // --- API Contract §5: exactly these five values ---

    /** Campus infrastructure: buildings, classrooms, furniture, electrical, plumbing. */
    INFRASTRUCTURE("Infrastructure & Facilities"),

    /** Sanitation: cleanliness, waste disposal, pest control, canteen hygiene. */
    SANITATION("Sanitation & Cleanliness"),

    /** Academic issues: syllabus, faculty, exam scheduling, attendance. */
    ACADEMICS("Academics & Classroom Issues"),

    /** IT and technology: computers, Wi-Fi, projectors, software, printing. */
    IT("IT & Technology"),

    /** Any complaint that does not fit the categories above. */
    OTHER("Other");

    // -------------------------------------------------------

    /**
     * The human-readable label shown on screen to students and admins.
     * Matches the display labels in the API contract §5.
     */
    private final String displayLabel;

    /**
     * Private constructor — enums cannot be created outside this class.
     *
     * @param displayLabel the user-facing label for this category
     */
    ComplaintCategory(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    /**
     * Returns the user-friendly display label (e.g., "IT & Technology").
     * Use this when rendering category names in JSP views.
     *
     * @return the display label string
     */
    public String getDisplayLabel() {
        return displayLabel;
    }

    /**
     * Parses a raw String from the database or HTTP parameter into a
     * {@link ComplaintCategory} enum constant.
     *
     * <p>This is the preferred way to convert untrusted input: it throws a clear,
     * descriptive exception rather than a confusing {@link IllegalArgumentException}
     * from {@link Enum#valueOf(Class, String)}.
     *
     * @param value the raw string (e.g., "IT", "INFRASTRUCTURE") — case-sensitive,
     *              must match the enum constant name exactly
     * @return the matching {@link ComplaintCategory}
     * @throws IllegalArgumentException if the value is null or does not match any constant
     */
    public static ComplaintCategory fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Complaint category cannot be null or blank."
            );
        }
        try {
            return ComplaintCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown complaint category: \"" + value + "\". "
                    + "Valid values: INFRASTRUCTURE, SANITATION, ACADEMICS, IT, OTHER."
            );
        }
    }

    /**
     * Returns the enum constant name — this is the value stored in the database.
     * Example: {@code ComplaintCategory.IT.toDbValue()} returns {@code "IT"}.
     *
     * @return the constant name as stored in the database VARCHAR column
     */
    public String toDbValue() {
        return this.name();
    }
}
