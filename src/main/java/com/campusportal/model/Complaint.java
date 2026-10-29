package com.campusportal.model;

import java.time.LocalDateTime;

/**
 * Plain Java model representing a row in the {@code complaints} database table.
 *
 * <h2>Schema mapping ({@code database/schema.sql})</h2>
 * <table border="1">
 *   <tr><th>DB column</th>            <th>Java field</th>      <th>Java type</th>           <th>Nullable?</th></tr>
 *   <tr><td>complaint_id</td>         <td>complaintId</td>     <td>int</td>                 <td>No (PK, auto)</td></tr>
 *   <tr><td>student_id</td>           <td>studentId</td>       <td>int</td>                 <td>No (FK → users)</td></tr>
 *   <tr><td>title</td>                <td>title</td>           <td>String</td>              <td>No</td></tr>
 *   <tr><td>category</td>             <td>category</td>        <td>ComplaintCategory</td>   <td>No</td></tr>
 *   <tr><td>description</td>          <td>description</td>     <td>String</td>              <td>No</td></tr>
 *   <tr><td>location</td>             <td>location</td>        <td>String</td>              <td>Yes</td></tr>
 *   <tr><td>status</td>               <td>status</td>          <td>ComplaintStatus</td>     <td>No (default SUBMITTED)</td></tr>
 *   <tr><td>assigned_admin_id</td>    <td>assignedAdminId</td> <td>Integer (boxed)</td>     <td>Yes (null = unassigned)</td></tr>
 *   <tr><td>admin_remarks</td>        <td>adminRemarks</td>    <td>String</td>              <td>Yes</td></tr>
 *   <tr><td>submitted_at</td>         <td>submittedAt</td>     <td>LocalDateTime</td>       <td>No (auto)</td></tr>
 *   <tr><td>updated_at</td>           <td>updatedAt</td>       <td>LocalDateTime</td>       <td>No (auto)</td></tr>
 *   <tr><td>resolved_at</td>          <td>resolvedAt</td>      <td>LocalDateTime</td>       <td>Yes</td></tr>
 * </table>
 *
 * <h2>Design decisions</h2>
 * <ul>
 *   <li><b>category</b> is typed as {@link ComplaintCategory} (not raw String) so the
 *       compiler prevents invalid categories from ever reaching the database.</li>
 *   <li><b>status</b> is typed as {@link ComplaintStatus} for the same reason.</li>
 *   <li><b>assignedAdminId</b> uses the boxed {@code Integer} (not primitive {@code int})
 *       so it can be {@code null} when no admin has self-assigned yet.</li>
 *   <li><b>resolvedAt</b> is null until the status changes to RESOLVED or REJECTED;
 *       the servlet sets it to the current time at that point.</li>
 *   <li><b>studentId</b> is stored as a plain {@code int} (the FK value) rather than
 *       embedding an entire {@link User} object. This keeps the DAO queries simple and
 *       avoids needing a JOIN every time a complaint is loaded.</li>
 * </ul>
 *
 * <p><b>This model class contains no SQL, no servlet logic, and no display logic.</b>
 */
public class Complaint {

    // -------------------------------------------------------
    //  Fields  (match schema.sql columns exactly)
    // -------------------------------------------------------

    /** Primary key. Corresponds to {@code complaint_id INT UNSIGNED AUTO_INCREMENT}. */
    private int complaintId;

    /**
     * Foreign key referencing the student who filed this complaint.
     * Corresponds to {@code student_id INT UNSIGNED NOT NULL → users(user_id)}.
     *
     * <p>Security: this value is always set from the server-side session ({@code userId}
     * attribute), NEVER from a hidden form field in the HTTP request.
     */
    private int studentId;

    /**
     * Short, descriptive title.
     * Corresponds to {@code title VARCHAR(200) NOT NULL}.
     */
    private String title;

    /**
     * The selected category, represented as a Java enum.
     * Stored in the database as a VARCHAR(60) using {@link ComplaintCategory#toDbValue()}.
     * Loaded from the database using {@link ComplaintCategory#fromDbValue(String)}.
     */
    private ComplaintCategory category;

    /**
     * Detailed description of the issue.
     * Corresponds to {@code description TEXT NOT NULL}.
     */
    private String description;

    /**
     * Optional campus location (e.g., "Seminar Hall B, Block 3").
     * Corresponds to {@code location VARCHAR(200) NULL}.
     * May be {@code null} if the student did not specify a location.
     */
    private String location;

    /**
     * Current workflow status.
     * Corresponds to {@code status ENUM(...) NOT NULL DEFAULT 'SUBMITTED'}.
     * Every new complaint starts as {@link ComplaintStatus#SUBMITTED}.
     */
    private ComplaintStatus status;

    /**
     * The admin who has self-assigned this complaint, or {@code null} if unassigned.
     * Corresponds to {@code assigned_admin_id INT UNSIGNED NULL → users(user_id)}.
     *
     * <p>Uses boxed {@code Integer} (not {@code int}) so it can represent SQL NULL.
     */
    private Integer assignedAdminId;

    /**
     * Optional remarks added by the admin (progress notes, resolution details, rejection reason).
     * Corresponds to {@code admin_remarks TEXT NULL}.
     * Null until an admin writes something.
     */
    private String adminRemarks;

    /**
     * When the student originally submitted this complaint.
     * Corresponds to {@code submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP}.
     * Set by the database; the DAO populates this after INSERT or on SELECT.
     */
    private LocalDateTime submittedAt;

    /**
     * Last modification timestamp, managed by MySQL automatically.
     * Corresponds to {@code updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP}.
     */
    private LocalDateTime updatedAt;

    /**
     * Closure timestamp — set when status becomes RESOLVED or REJECTED.
     * Corresponds to {@code resolved_at DATETIME NULL}.
     * The servlet sets this field to the current time before calling the DAO's update method.
     */
    private LocalDateTime resolvedAt;

    // -------------------------------------------------------
    //  Constructors
    // -------------------------------------------------------

    /**
     * No-argument constructor. Used by the DAO when building a Complaint from a
     * {@code ResultSet} row using individual setters.
     */
    public Complaint() {
    }

    /**
     * Constructor for creating a brand-new complaint before INSERT into the database.
     * The {@code complaintId}, {@code submittedAt}, and {@code updatedAt} are assigned
     * by the database and will be populated later by the DAO.
     *
     * <p>Example use in the servlet:
     * <pre>
     *   int studentId = (Integer) session.getAttribute("userId");
     *   Complaint c = new Complaint(studentId, "Broken projector",
     *                               ComplaintCategory.IT,
     *                               "Projector in Hall B is broken.", "Hall B");
     * </pre>
     *
     * @param studentId   the session userId of the submitting student
     * @param title       short descriptive title (required)
     * @param category    selected category (required)
     * @param description detailed description (required)
     * @param location    campus location (optional — pass null if not provided)
     */
    public Complaint(int studentId, String title, ComplaintCategory category,
                     String description, String location) {
        this.studentId   = studentId;
        this.title       = title;
        this.category    = category;
        this.description = description;
        this.location    = location;
        this.status      = ComplaintStatus.SUBMITTED;  // all new complaints start here
    }

    /**
     * Full constructor used by the DAO when reconstructing a Complaint from a SELECT query.
     *
     * @param complaintId     primary key
     * @param studentId       FK to the student user
     * @param title           complaint title
     * @param category        complaint category enum
     * @param description     full description text
     * @param location        campus location or null
     * @param status          current workflow status
     * @param assignedAdminId admin FK or null if unassigned
     * @param adminRemarks    admin notes or null
     * @param submittedAt     when the complaint was created
     * @param updatedAt       last update timestamp
     * @param resolvedAt      closure timestamp or null
     */
    public Complaint(int complaintId, int studentId, String title,
                     ComplaintCategory category, String description, String location,
                     ComplaintStatus status, Integer assignedAdminId, String adminRemarks,
                     LocalDateTime submittedAt, LocalDateTime updatedAt,
                     LocalDateTime resolvedAt) {
        this.complaintId     = complaintId;
        this.studentId       = studentId;
        this.title           = title;
        this.category        = category;
        this.description     = description;
        this.location        = location;
        this.status          = status;
        this.assignedAdminId = assignedAdminId;
        this.adminRemarks    = adminRemarks;
        this.submittedAt     = submittedAt;
        this.updatedAt       = updatedAt;
        this.resolvedAt      = resolvedAt;
    }

    // -------------------------------------------------------
    //  Getters and setters
    // -------------------------------------------------------

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /** @return campus location string, or null if not provided by the student. */
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    /**
     * @return the admin's user_id if one has self-assigned this complaint, otherwise null.
     * Uses boxed Integer to represent SQL NULL naturally.
     */
    public Integer getAssignedAdminId() { return assignedAdminId; }
    public void setAssignedAdminId(Integer assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    /** @return admin remarks/notes, or null if no remarks have been added yet. */
    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * @return the timestamp when this complaint was closed (RESOLVED or REJECTED),
     *         or null if it is still open.
     */
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    // -------------------------------------------------------
    //  Helper methods
    // -------------------------------------------------------

    /**
     * Returns {@code true} if this complaint is in a closed/terminal state, meaning
     * its status is either {@link ComplaintStatus#RESOLVED} or {@link ComplaintStatus#REJECTED}.
     *
     * <p>Useful in JSP to conditionally render a "Reopen" button.
     *
     * @return true if the complaint is closed
     */
    public boolean isClosed() {
        return status == ComplaintStatus.RESOLVED || status == ComplaintStatus.REJECTED;
    }

    /**
     * Returns a human-readable summary for logging and debugging.
     * Does not contain sensitive data (the description text is excluded to keep
     * log lines short).
     *
     * @return string with complaintId, studentId, category, status, and title
     */
    @Override
    public String toString() {
        return "Complaint{"
                + "complaintId=" + complaintId
                + ", studentId=" + studentId
                + ", category=" + category
                + ", status=" + status
                + ", title='" + title + '\''
                + '}';
    }
}
