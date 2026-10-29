package com.campusportal.dao;

import com.campusportal.model.User;
import com.campusportal.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link User} entities.
 *
 * <p>Handles user lookup, verification, and registration operations using JDBC
 * {@link PreparedStatement}. Never stores or logs plaintext passwords.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private final DBConnection.ConnectionProvider connectionProvider;

    /**
     * Default constructor using standard {@link DBConnection}.
     */
    public UserDAO() {
        this(DBConnection::getConnection);
    }

    /**
     * Constructor allowing injection of a custom connection provider (for testing).
     *
     * @param connectionProvider custom connection provider
     */
    public UserDAO(DBConnection.ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }

    /**
     * Finds an active or inactive user by their unique email address.
     *
     * @param email user email address (case-insensitive search trimmed)
     * @return the {@link User} object, or {@code null} if no user was found
     */
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, email, password_hash, full_name, role, identifier, created_at, updated_at, is_active "
                   + "FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error finding user by email", e);
        }
        return null;
    }

    /**
     * Finds a user by their primary key.
     *
     * @param userId user ID
     * @return the {@link User} object, or {@code null} if not found
     */
    public User findById(int userId) {
        if (userId <= 0) {
            return null;
        }

        String sql = "SELECT user_id, email, password_hash, full_name, role, identifier, created_at, updated_at, is_active "
                   + "FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error finding user by ID: " + userId, e);
        }
        return null;
    }

    /**
     * Checks whether an account with the given email already exists.
     *
     * @param email the email to check
     * @return {@code true} if a user with this email exists, {@code false} otherwise
     */
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error checking email existence", e);
        }
        return false;
    }

    /**
     * Creates a new user record in the database using a {@link PreparedStatement}.
     *
     * <p>Populates the generated {@code user_id} on the passed {@link User} object.
     *
     * @param user the user to persist; must have passwordHash set (never plaintext)
     * @return {@code true} if inserted successfully, {@code false} on failure or duplicate
     */
    public boolean createUser(User user) {
        if (user == null || user.getEmail() == null || user.getPasswordHash() == null) {
            return false;
        }

        String sql = "INSERT INTO users (email, password_hash, full_name, role, identifier, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getEmail().trim().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName().trim());
            // Role enforcement: default to STUDENT if null or not ADMIN
            String role = "ADMIN".equalsIgnoreCase(user.getRole()) ? "ADMIN" : "STUDENT";
            stmt.setString(4, role);
            stmt.setString(5, user.getIdentifier() != null ? user.getIdentifier().trim() : null);
            stmt.setBoolean(6, user.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to create user: " + e.getMessage(), e);
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String role = rs.getString("role");
        String identifier = rs.getString("identifier");

        Timestamp createdTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = (updatedTs != null) ? updatedTs.toLocalDateTime() : null;

        boolean active = rs.getBoolean("is_active");

        return new User(userId, email, passwordHash, fullName, role, identifier, createdAt, updatedAt, active);
    }
}
