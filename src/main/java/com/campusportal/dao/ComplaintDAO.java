package com.campusportal.dao;

import com.campusportal.model.Complaint;
import com.campusportal.model.ComplaintCategory;
import com.campusportal.model.ComplaintStatus;
import com.campusportal.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link Complaint} entities.
 *
 * <p>Handles complaint creation, retrieval, and tracking queries using JDBC
 * {@link PreparedStatement}. Ensures all resources are cleanly managed and parameters
 * safely bound.
 */
public class ComplaintDAO {

    private static final Logger LOGGER = Logger.getLogger(ComplaintDAO.class.getName());

    private final DBConnection.ConnectionProvider connectionProvider;

    /**
     * Default constructor using standard {@link DBConnection}.
     */
    public ComplaintDAO() {
        this(DBConnection::getConnection);
    }

    /**
     * Constructor allowing injection of a custom connection provider (for testing).
     *
     * @param connectionProvider custom connection provider
     */
    public ComplaintDAO(DBConnection.ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }

    /**
     * Inserts a new complaint record into the database.
     *
     * <p>Populates the generated {@code complaint_id} on the passed {@link Complaint} object.
     *
     * @param complaint the complaint to persist; must have valid studentId, title, category, and description
     * @return {@code true} if inserted successfully, {@code false} on failure
     */
    public boolean createComplaint(Complaint complaint) {
        if (complaint == null
                || complaint.getStudentId() <= 0
                || complaint.getTitle() == null
                || complaint.getCategory() == null
                || complaint.getDescription() == null) {
            return false;
        }

        String sql = "INSERT INTO complaints (student_id, title, category, description, location, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, complaint.getStudentId());
            stmt.setString(2, complaint.getTitle().trim());
            stmt.setString(3, complaint.getCategory().toDbValue());
            stmt.setString(4, complaint.getDescription().trim());
            stmt.setString(5, complaint.getLocation() != null ? complaint.getLocation().trim() : null);

            ComplaintStatus status = complaint.getStatus() != null ? complaint.getStatus() : ComplaintStatus.SUBMITTED;
            stmt.setString(6, status.toDbValue());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        complaint.setComplaintId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating complaint for student ID: " + complaint.getStudentId(), e);
        }
        return false;
    }

    /**
     * Finds a complaint by its primary key.
     *
     * @param complaintId complaint ID
     * @return the {@link Complaint} object, or {@code null} if not found
     */
    public Complaint findById(int complaintId) {
        if (complaintId <= 0) {
            return null;
        }

        String sql = "SELECT complaint_id, student_id, title, category, description, location, status, "
                   + "assigned_admin_id, admin_remarks, submitted_at, updated_at, resolved_at "
                   + "FROM complaints WHERE complaint_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, complaintId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error finding complaint by ID: " + complaintId, e);
        }
        return null;
    }

    /**
     * Finds all complaints submitted by a specific student, ordered by submission date descending.
     *
     * @param studentId student user ID
     * @return a list of complaints belonging to the student (empty list if none found)
     */
    public List<Complaint> findByStudentId(int studentId) {
        List<Complaint> list = new ArrayList<>();
        if (studentId <= 0) {
            return list;
        }

        String sql = "SELECT complaint_id, student_id, title, category, description, location, status, "
                   + "assigned_admin_id, admin_remarks, submitted_at, updated_at, resolved_at "
                   + "FROM complaints WHERE student_id = ? "
                   + "ORDER BY submitted_at DESC, complaint_id DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error finding complaints for student ID: " + studentId, e);
        }
        return list;
    }

    /**
     * Finds a complaint ensuring both complaint ID and student ID match.
     * This provides a secure query verification to prevent unauthorized access.
     *
     * @param complaintId complaint ID
     * @param studentId   student user ID
     * @return the {@link Complaint} object if found and belongs to student, otherwise {@code null}
     */
    public Complaint findByIdAndStudentId(int complaintId, int studentId) {
        if (complaintId <= 0 || studentId <= 0) {
            return null;
        }

        String sql = "SELECT complaint_id, student_id, title, category, description, location, status, "
                   + "assigned_admin_id, admin_remarks, submitted_at, updated_at, resolved_at "
                   + "FROM complaints WHERE complaint_id = ? AND student_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, complaintId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error finding complaint " + complaintId + " for student " + studentId, e);
        }
        return null;
    }

    /**
     * Computes complaint statistics for a specific student.
     *
     * @param studentId student user ID
     * @return a map with keys "total", "resolved", and "pending"
     */
    public Map<String, Integer> getStatsByStudentId(int studentId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 0);
        stats.put("resolved", 0);
        stats.put("pending", 0);

        if (studentId <= 0) {
            return stats;
        }

        String sql = "SELECT status, COUNT(*) AS cnt FROM complaints WHERE student_id = ? GROUP BY status";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                int total = 0;
                int resolved = 0;
                int pending = 0;

                while (rs.next()) {
                    String statusStr = rs.getString("status");
                    int count = rs.getInt("cnt");
                    total += count;

                    if ("RESOLVED".equalsIgnoreCase(statusStr)) {
                        resolved += count;
                    } else if ("SUBMITTED".equalsIgnoreCase(statusStr)
                            || "UNDER_REVIEW".equalsIgnoreCase(statusStr)
                            || "IN_PROGRESS".equalsIgnoreCase(statusStr)) {
                        pending += count;
                    }
                }

                stats.put("total", total);
                stats.put("resolved", resolved);
                stats.put("pending", pending);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error retrieving complaint stats for student ID: " + studentId, e);
        }

        return stats;
    }

    private Complaint mapRow(ResultSet rs) throws SQLException {
        int complaintId = rs.getInt("complaint_id");
        int studentId = rs.getInt("student_id");
        String title = rs.getString("title");
        String categoryStr = rs.getString("category");
        ComplaintCategory category = ComplaintCategory.fromDbValue(categoryStr);
        String description = rs.getString("description");
        String location = rs.getString("location");
        String statusStr = rs.getString("status");
        ComplaintStatus status = ComplaintStatus.fromDbValue(statusStr);

        int adminId = rs.getInt("assigned_admin_id");
        Integer assignedAdminId = rs.wasNull() ? null : adminId;

        String adminRemarks = rs.getString("admin_remarks");

        Timestamp submittedTs = rs.getTimestamp("submitted_at");
        LocalDateTime submittedAt = (submittedTs != null) ? submittedTs.toLocalDateTime() : null;

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        Timestamp resolvedTs = rs.getTimestamp("resolved_at");
        LocalDateTime resolvedAt = (resolvedTs != null) ? resolvedTs.toLocalDateTime() : null;

        return new Complaint(complaintId, studentId, title, category, description, location,
                status, assignedAdminId, adminRemarks, submittedAt, updatedAt, resolvedAt);
    }
}
