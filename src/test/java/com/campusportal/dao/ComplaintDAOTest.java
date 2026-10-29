package com.campusportal.dao;

import com.campusportal.model.Complaint;
import com.campusportal.model.ComplaintCategory;
import com.campusportal.model.ComplaintStatus;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ComplaintDAO tests")
class ComplaintDAOTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSet mockGeneratedKeys;

    private ComplaintDAO complaintDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        complaintDAO = new ComplaintDAO(() -> mockConnection);
    }

    @Test
    @DisplayName("createComplaint successfully inserts and populates generated ID")
    void testCreateComplaintSuccess() throws Exception {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(101);

        Complaint complaint = new Complaint(5, "Wi-Fi not connecting", ComplaintCategory.IT,
                "Unable to connect to campus Wi-Fi in Library 2nd floor", "Library 2nd Floor");

        boolean result = complaintDAO.createComplaint(complaint);

        assertTrue(result);
        assertEquals(101, complaint.getComplaintId());
        verify(mockStatement).setInt(1, 5);
        verify(mockStatement).setString(2, "Wi-Fi not connecting");
        verify(mockStatement).setString(3, "IT");
        verify(mockStatement).setString(4, "Unable to connect to campus Wi-Fi in Library 2nd floor");
        verify(mockStatement).setString(5, "Library 2nd Floor");
        verify(mockStatement).setString(6, "SUBMITTED");
    }

    @Test
    @DisplayName("createComplaint rejects invalid inputs without querying DB")
    void testCreateComplaintInvalidInput() throws Exception {
        assertFalse(complaintDAO.createComplaint(null));

        Complaint invalidStudentId = new Complaint(0, "Title", ComplaintCategory.IT, "Description", "Location");
        assertFalse(complaintDAO.createComplaint(invalidStudentId));

        Complaint nullTitle = new Complaint(5, null, ComplaintCategory.IT, "Description", "Location");
        assertFalse(complaintDAO.createComplaint(nullTitle));

        Complaint nullCategory = new Complaint(5, "Title", null, "Description", "Location");
        assertFalse(complaintDAO.createComplaint(nullCategory));

        Complaint nullDesc = new Complaint(5, "Title", ComplaintCategory.IT, null, "Location");
        assertFalse(complaintDAO.createComplaint(nullDesc));

        verifyNoInteractions(mockConnection);
    }

    @Test
    @DisplayName("createComplaint handles executeUpdate failure")
    void testCreateComplaintDbFailure() throws Exception {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        Complaint complaint = new Complaint(5, "Water leaking", ComplaintCategory.INFRASTRUCTURE,
                "Water dripping from pipe near washroom", "Block B");

        boolean result = complaintDAO.createComplaint(complaint);
        assertFalse(result);
    }

    @Test
    @DisplayName("createComplaint handles SQLException gracefully")
    void testCreateComplaintSQLException() throws Exception {
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("DB connection broken"));

        Complaint complaint = new Complaint(5, "Water leaking", ComplaintCategory.INFRASTRUCTURE,
                "Water dripping from pipe near washroom", "Block B");

        boolean result = complaintDAO.createComplaint(complaint);
        assertFalse(result);
    }

    @Test
    @DisplayName("findById returns Complaint when found")
    void testFindByIdFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("complaint_id")).thenReturn(42);
        when(mockResultSet.getInt("student_id")).thenReturn(10);
        when(mockResultSet.getString("title")).thenReturn("Broken Desk");
        when(mockResultSet.getString("category")).thenReturn("INFRASTRUCTURE");
        when(mockResultSet.getString("description")).thenReturn("Desk leg broken in Room 301");
        when(mockResultSet.getString("location")).thenReturn("Room 301");
        when(mockResultSet.getString("status")).thenReturn("SUBMITTED");
        when(mockResultSet.getInt("assigned_admin_id")).thenReturn(0);
        when(mockResultSet.wasNull()).thenReturn(true);
        when(mockResultSet.getString("admin_remarks")).thenReturn(null);
        when(mockResultSet.getTimestamp("submitted_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("resolved_at")).thenReturn(null);

        Complaint complaint = complaintDAO.findById(42);

        assertNotNull(complaint);
        assertEquals(42, complaint.getComplaintId());
        assertEquals(10, complaint.getStudentId());
        assertEquals("Broken Desk", complaint.getTitle());
        assertEquals(ComplaintCategory.INFRASTRUCTURE, complaint.getCategory());
        assertEquals(ComplaintStatus.SUBMITTED, complaint.getStatus());
        assertNull(complaint.getAssignedAdminId());
        assertNull(complaint.getAdminRemarks());
        verify(mockStatement).setInt(1, 42);
    }

    @Test
    @DisplayName("findById returns null when not found")
    void testFindByIdNotFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertNull(complaintDAO.findById(999));
    }

    @Test
    @DisplayName("findById returns null on invalid ID without DB query")
    void testFindByIdInvalidId() throws Exception {
        assertNull(complaintDAO.findById(0));
        assertNull(complaintDAO.findById(-5));
        verifyNoInteractions(mockConnection);
    }

    @Test
    @DisplayName("findByStudentId returns list of complaints")
    void testFindByStudentIdFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getInt("complaint_id")).thenReturn(1, 2);
        when(mockResultSet.getInt("student_id")).thenReturn(7, 7);
        when(mockResultSet.getString("title")).thenReturn("Issue 1", "Issue 2");
        when(mockResultSet.getString("category")).thenReturn("IT", "SANITATION");
        when(mockResultSet.getString("description")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getString("location")).thenReturn("Loc 1", "Loc 2");
        when(mockResultSet.getString("status")).thenReturn("SUBMITTED", "IN_PROGRESS");
        when(mockResultSet.getInt("assigned_admin_id")).thenReturn(0, 1);
        when(mockResultSet.wasNull()).thenReturn(true, false);
        when(mockResultSet.getString("admin_remarks")).thenReturn(null, "Working on it");
        when(mockResultSet.getTimestamp("submitted_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("resolved_at")).thenReturn(null);

        List<Complaint> list = complaintDAO.findByStudentId(7);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getComplaintId());
        assertEquals(2, list.get(1).getComplaintId());
        verify(mockStatement).setInt(1, 7);
    }

    @Test
    @DisplayName("findByStudentId returns empty list on invalid student ID")
    void testFindByStudentIdInvalidId() throws Exception {
        List<Complaint> list = complaintDAO.findByStudentId(0);
        assertTrue(list.isEmpty());
        verifyNoInteractions(mockConnection);
    }

    @Test
    @DisplayName("findByIdAndStudentId returns Complaint when found")
    void testFindByIdAndStudentIdFound() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        when(mockResultSet.getInt("complaint_id")).thenReturn(50);
        when(mockResultSet.getInt("student_id")).thenReturn(12);
        when(mockResultSet.getString("title")).thenReturn("Classroom Fan Not Working");
        when(mockResultSet.getString("category")).thenReturn("INFRASTRUCTURE");
        when(mockResultSet.getString("description")).thenReturn("Fan regulator broken in Hall 1");
        when(mockResultSet.getString("location")).thenReturn("Hall 1");
        when(mockResultSet.getString("status")).thenReturn("UNDER_REVIEW");
        when(mockResultSet.getInt("assigned_admin_id")).thenReturn(2);
        when(mockResultSet.wasNull()).thenReturn(false);
        when(mockResultSet.getString("admin_remarks")).thenReturn("Inspection scheduled");
        when(mockResultSet.getTimestamp("submitted_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("resolved_at")).thenReturn(null);

        Complaint complaint = complaintDAO.findByIdAndStudentId(50, 12);

        assertNotNull(complaint);
        assertEquals(50, complaint.getComplaintId());
        assertEquals(12, complaint.getStudentId());
        verify(mockStatement).setInt(1, 50);
        verify(mockStatement).setInt(2, 12);
    }

    @Test
    @DisplayName("findByIdAndStudentId returns null when mismatch or invalid args")
    void testFindByIdAndStudentIdNotFoundOrInvalid() throws Exception {
        assertNull(complaintDAO.findByIdAndStudentId(0, 10));
        assertNull(complaintDAO.findByIdAndStudentId(10, 0));

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertNull(complaintDAO.findByIdAndStudentId(50, 99));
    }

    @Test
    @DisplayName("getStatsByStudentId correctly aggregates counts")
    void testGetStatsByStudentId() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, false);

        when(mockResultSet.getString("status")).thenReturn("SUBMITTED", "UNDER_REVIEW", "RESOLVED", "REJECTED");
        when(mockResultSet.getInt("cnt")).thenReturn(2, 1, 3, 1);

        Map<String, Integer> stats = complaintDAO.getStatsByStudentId(8);

        assertNotNull(stats);
        assertEquals(7, stats.get("total")); // 2 + 1 + 3 + 1
        assertEquals(3, stats.get("resolved")); // 3
        assertEquals(3, stats.get("pending")); // 2 (SUBMITTED) + 1 (UNDER_REVIEW)
        verify(mockStatement).setInt(1, 8);
    }

    @Test
    @DisplayName("getStatsByStudentId returns zeroed stats on invalid student ID")
    void testGetStatsByStudentIdInvalid() throws Exception {
        Map<String, Integer> stats = complaintDAO.getStatsByStudentId(-1);
        assertEquals(0, stats.get("total"));
        assertEquals(0, stats.get("resolved"));
        assertEquals(0, stats.get("pending"));
        verifyNoInteractions(mockConnection);
    }
}
