package com.campusportal.model;

import java.time.LocalDateTime;

/**
 * Plain Java model representing a row in the {@code users} database table.
 *
 * <h2>Schema mapping ({@code database/schema.sql})</h2>
 * <table border="1">
 *   <tr><th>DB column</th>        <th>Java field</th>      <th>Java type</th></tr>
 *   <tr><td>user_id</td>          <td>userId</td>           <td>int</td></tr>
 *   <tr><td>email</td>            <td>email</td>            <td>String</td></tr>
 *   <tr><td>password_hash</td>    <td>passwordHash</td>     <td>String</td></tr>
 *   <tr><td>full_name</td>        <td>fullName</td>         <td>String</td></tr>
 *   <tr><td>role</td>             <td>role</td>             <td>String ("STUDENT"/"ADMIN")</td></tr>
 *   <tr><td>identifier</td>       <td>identifier</td>       <td>String (nullable)</td></tr>
 *   <tr><td>created_at</td>       <td>createdAt</td>        <td>LocalDateTime</td></tr>
 *   <tr><td>updated_at</td>       <td>updatedAt</td>        <td>LocalDateTime</td></tr>
 *   <tr><td>is_active</td>        <td>active</td>           <td>boolean</td></tr>
 * </table>
 *
 * <h2>Design decisions</h2>
 * <ul>
 *   <li><b>passwordHash</b> is intentionally <em>not</em> included in {@link #toString()}
 *       to prevent accidental logging of the credential. It is kept in the model because
 *       the DAO reads and writes it, but no display method should ever print it.</li>
 *   <li><b>role</b> is stored as a plain {@code String} ("STUDENT" or "ADMIN") because
 *       that is what the database ENUM and session attribute contract use. Converting it
 *       to a separate Role enum would add a layer without benefit for this project scope.</li>
 *   <li><b>identifier</b> is nullable in the database: admin accounts legitimately have
 *       no roll number. The NOT-NULL constraint for students is enforced by
 *       {@code RegisterServlet}, not the model.</li>
 *   <li><b>updatedAt</b> is managed by MySQL's {@code ON UPDATE CURRENT_TIMESTAMP}; it
 *       is included for completeness when reading from the database.</li>
 * </ul>
 *
 * <p><b>This model class contains no SQL, no servlet logic, and no authentication logic.</b>
 * It is a pure data holder (a "POJO" — Plain Old Java Object).
 */
public class User {

    // -------------------------------------------------------
    //  Fields  (match schema.sql columns exactly)
    // -------------------------------------------------------

    /** Primary key. Corresponds to {@code user_id INT UNSIGNED AUTO_INCREMENT}. */
    private int userId;

    /**
     * Login email address. Corresponds to {@code email VARCHAR(255) NOT NULL UNIQUE}.
     * Never null in a valid user record.
     */
    private String email;

    /**
     * BCrypt-hashed password. Corresponds to {@code password_hash VARCHAR(255) NOT NULL}.
     *
     * <p><strong>IMPORTANT:</strong> This field exists so that the DAO can read and write
     * the hash. It must NEVER be printed, logged, serialised to JSON, or returned in a
     * {@code toString()}. Use {@link com.campusportal.util.PasswordUtil} to compare
     * plain-text passwords against this hash during login.
     */
    private String passwordHash;

    /** Display name. Corresponds to {@code full_name VARCHAR(150) NOT NULL}. */
    private String fullName;

    /**
     * User role. Corresponds to {@code role ENUM('STUDENT','ADMIN') NOT NULL DEFAULT 'STUDENT'}.
     * Valid values are the string constants {@code "STUDENT"} and {@code "ADMIN"}.
     * These must match the session attribute {@code userRole} defined in the API contract §7.
     */
    private String role;

    /**
     * Roll number (for students) or optional staff ID (for admins).
     * Corresponds to {@code identifier VARCHAR(60) NULL}.
     *
     * <p>This field will be {@code null} for admin accounts (SQL seed data uses NULL).
     * For student accounts the value is mandatory and set at registration time.
     */
    private String identifier;

    /**
     * Account creation timestamp.
     * Corresponds to {@code created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP}.
     * Set by the database; typically populated by the DAO after INSERT.
     */
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp.
     * Corresponds to {@code updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP}.
     * Managed automatically by MySQL; populated by the DAO when reading rows.
     */
    private LocalDateTime updatedAt;

    /**
     * Soft-delete / account-status flag.
     * Corresponds to {@code is_active TINYINT(1) NOT NULL DEFAULT 1}.
     * {@code true} means the account is active; {@code false} means deactivated.
     */
    private boolean active;

    // -------------------------------------------------------
    //  Constructors
    // -------------------------------------------------------

    /**
     * No-argument constructor. Required by some frameworks and convenient for
     * building a User step-by-step with setters (e.g., when reading from ResultSet).
     */
    public User() {
    }

    /**
     * Full constructor used by the DAO when reading a complete row from the database.
     *
     * <p>Note that {@code passwordHash} is included here because the DAO must store it
     * in the object immediately after reading it from the database — but this constructor
     * should only be called from DAO code, never from servlet or JSP layer.
     *
     * @param userId       the primary key
     * @param email        the login email
     * @param passwordHash the BCrypt-hashed password (not the plaintext!)
     * @param fullName     the user's display name
     * @param role         "STUDENT" or "ADMIN"
     * @param identifier   roll number (students) or null (admins)
     * @param createdAt    account creation time from the database
     * @param updatedAt    last update time from the database
     * @param active       true if the account is active
     */
    public User(int userId, String email, String passwordHash,
                String fullName, String role, String identifier,
                LocalDateTime createdAt, LocalDateTime updatedAt, boolean active) {
        this.userId       = userId;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.role         = role;
        this.identifier   = identifier;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
        this.active       = active;
    }

    /**
     * Convenience constructor for creating a new user before INSERT into the database.
     * The {@code userId}, {@code createdAt}, and {@code updatedAt} will be assigned by
     * the database and populated later by the DAO.
     *
     * @param email        the login email
     * @param passwordHash the BCrypt hash of the chosen password
     * @param fullName     the user's display name
     * @param role         "STUDENT" or "ADMIN"
     * @param identifier   roll number for students; null for admins
     */
    public User(String email, String passwordHash,
                String fullName, String role, String identifier) {
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.role         = role;
        this.identifier   = identifier;
        this.active       = true;  // new accounts are always active
    }

    // -------------------------------------------------------
    //  Getters and setters
    // -------------------------------------------------------

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the BCrypt password hash.
     *
     * <p><strong>Security note:</strong> Call this method only from DAO code (when
     * writing to the database) or from {@link com.campusportal.util.PasswordUtil}
     * (when verifying a login). Never pass this value to a JSP or log it.
     *
     * @return the stored BCrypt hash string, never plaintext
     */
    public String getPasswordHash() { return passwordHash; }

    /**
     * Stores a BCrypt hash.
     *
     * <p>Only call this with a hash produced by
     * {@link com.campusportal.util.PasswordUtil#hashPassword(String)}.
     * Never call {@code setPasswordHash("plainTextPassword")}.
     *
     * @param passwordHash a BCrypt hash; must not be a plaintext password
     */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    /** @return "STUDENT" or "ADMIN" — matches the session attribute {@code userRole}. */
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /**
     * @return the roll number (students) or null (admins). Never null for a registered student.
     */
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** @return true if this account is active; false if soft-deleted/deactivated. */
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // -------------------------------------------------------
    //  Helper methods
    // -------------------------------------------------------

    /**
     * Returns {@code true} if this user has the ADMIN role.
     * Equivalent to {@code "ADMIN".equals(role)}.
     *
     * @return true when role is "ADMIN"
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Returns {@code true} if this user has the STUDENT role.
     *
     * @return true when role is "STUDENT"
     */
    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    /**
     * Returns a safe, human-readable representation of this user.
     *
     * <p><strong>passwordHash is intentionally excluded</strong> to prevent leaks
     * in application logs, debug output, or error pages.
     *
     * @return a string showing userId, email, fullName, role, and identifier
     */
    @Override
    public String toString() {
        return "User{"
                + "userId=" + userId
                + ", email='" + email + '\''
                + ", fullName='" + fullName + '\''
                + ", role='" + role + '\''
                + ", identifier='" + identifier + '\''
                + ", active=" + active
                + '}';
        // passwordHash deliberately omitted — never log it
    }
}
