package com.campusportal.dao;

import com.campusportal.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserDAO tests")
class UserDAOTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userDAO = new UserDAO(() -> mockConnection);
    }

    @Test
    @DisplayName("findByEmail returns User when record is found")
    void testFindByEmailFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("user_id")).thenReturn(42);
        when(mockResultSet.getString("email")).thenReturn("student@campus.edu");
        when(mockResultSet.getString("password_hash")).thenReturn("$2a$12$somehashvalue");
        when(mockResultSet.getString("full_name")).thenReturn("Alice Fernandes");
        when(mockResultSet.getString("role")).thenReturn("STUDENT");
        when(mockResultSet.getString("identifier")).thenReturn("STU-001");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getBoolean("is_active")).thenReturn(true);

        User user = userDAO.findByEmail("student@campus.edu");

        assertNotNull(user);
        assertEquals(42, user.getUserId());
        assertEquals("student@campus.edu", user.getEmail());
        assertEquals("Alice Fernandes", user.getFullName());
        assertEquals("STUDENT", user.getRole());
        assertEquals("STU-001", user.getIdentifier());
        assertTrue(user.isActive());
        verify(mockStatement).setString(1, "student@campus.edu");
    }

    @Test
    @DisplayName("findByEmail returns null when record is not found")
    void testFindByEmailNotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User user = userDAO.findByEmail("unknown@campus.edu");
        assertNull(user);
    }

    @Test
    @DisplayName("findByEmail returns null on empty or null input without querying DB")
    void testFindByEmailBlank() throws Exception {
        assertNull(userDAO.findByEmail(null));
        assertNull(userDAO.findByEmail("  "));
        verifyNoInteractions(mockConnection);
    }

    @Test
    @DisplayName("findById returns User when record exists")
    void testFindByIdFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("user_id")).thenReturn(10);
        when(mockResultSet.getString("email")).thenReturn("admin@campus.edu");
        when(mockResultSet.getString("password_hash")).thenReturn("$2a$12$adminhash");
        when(mockResultSet.getString("full_name")).thenReturn("Admin User");
        when(mockResultSet.getString("role")).thenReturn("ADMIN");
        when(mockResultSet.getString("identifier")).thenReturn(null);
        when(mockResultSet.getBoolean("is_active")).thenReturn(true);

        User user = userDAO.findById(10);
        assertNotNull(user);
        assertEquals(10, user.getUserId());
        assertTrue(user.isAdmin());
        verify(mockStatement).setInt(1, 10);
    }

    @Test
    @DisplayName("emailExists returns true when row exists")
    void testEmailExistsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        assertTrue(userDAO.emailExists("existing@campus.edu"));
    }

    @Test
    @DisplayName("emailExists returns false when row does not exist")
    void testEmailExistsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertFalse(userDAO.emailExists("new@campus.edu"));
    }

    @Test
    @DisplayName("createUser sets generated ID and returns true on success")
    void testCreateUserSuccess() throws Exception {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        ResultSet mockKeys = mock(ResultSet.class);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockKeys);
        when(mockKeys.next()).thenReturn(true);
        when(mockKeys.getInt(1)).thenReturn(99);

        User newUser = new User("newbie@campus.edu", "$2a$12$newhash", "Newbie Student", "STUDENT", "STU-999");
        boolean created = userDAO.createUser(newUser);

        assertTrue(created);
        assertEquals(99, newUser.getUserId());
        verify(mockStatement).setString(1, "newbie@campus.edu");
        verify(mockStatement).setString(2, "$2a$12$newhash");
        verify(mockStatement).setString(3, "Newbie Student");
        verify(mockStatement).setString(4, "STUDENT");
        verify(mockStatement).setString(5, "STU-999");
        verify(mockStatement).setBoolean(6, true);
    }

    @Test
    @DisplayName("createUser handles SQLException safely and returns false")
    void testCreateUserSQLException() throws Exception {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("Duplicate entry"));

        User newUser = new User("dup@campus.edu", "hash", "Dup User", "STUDENT", "STU-000");
        boolean created = userDAO.createUser(newUser);

        assertFalse(created);
    }
}
