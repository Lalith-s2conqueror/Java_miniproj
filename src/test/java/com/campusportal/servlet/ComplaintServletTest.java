package com.campusportal.servlet;

import com.campusportal.dao.ComplaintDAO;
import com.campusportal.model.Complaint;
import com.campusportal.model.ComplaintCategory;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ComplaintServlet tests")
class ComplaintServletTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private ComplaintDAO complaintDAO;

    private ComplaintServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ComplaintServlet();
        servlet.setComplaintDAO(complaintDAO);
        when(request.getContextPath()).thenReturn("/campus-complaint-portal");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet redirects to login when unauthenticated")
    void testDoGetUnauthenticated() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/campus-complaint-portal/auth/login?reason=session_expired");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("doGet /student/complaints/new forwards to submit-complaint.jsp")
    void testDoGetNewComplaint() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(15);
        when(request.getServletPath()).thenReturn("/student/complaints/new");

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/student/submit-complaint.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet /student/complaints/view forwards to detail view when student owns complaint")
    void testDoGetViewComplaintSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(15);
        when(request.getServletPath()).thenReturn("/student/complaints/view");
        when(request.getParameter("id")).thenReturn("42");

        Complaint complaint = new Complaint(15, "Broken projector", ComplaintCategory.IT,
                "Projector lamp failed in room 204", "Room 204");
        complaint.setComplaintId(42);

        when(complaintDAO.findById(42)).thenReturn(complaint);

        servlet.doGet(request, response);

        verify(request).setAttribute("complaint", complaint);
        verify(request).getRequestDispatcher("/WEB-INF/views/student/complaint-detail.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet /student/complaints/view blocks access if complaint belongs to another student")
    void testDoGetViewComplaintUnauthorized() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(15); // Current student is 15
        when(request.getServletPath()).thenReturn("/student/complaints/view");
        when(request.getParameter("id")).thenReturn("42");

        Complaint complaint = new Complaint(99, "Secret complaint", ComplaintCategory.ACADEMICS,
                "Belongs to student 99", "Dept");
        complaint.setComplaintId(42);

        when(complaintDAO.findById(42)).thenReturn(complaint);

        servlet.doGet(request, response);

        // Security check: Access denied must be recorded and user redirected to dashboard
        verify(session).setAttribute(eq("errorMessage"), contains("Access denied"));
        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("doGet /student/complaints/view redirects to dashboard when complaint not found")
    void testDoGetViewComplaintNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(15);
        when(request.getServletPath()).thenReturn("/student/complaints/view");
        when(request.getParameter("id")).thenReturn("999");

        when(complaintDAO.findById(999)).thenReturn(null);

        servlet.doGet(request, response);

        verify(session).setAttribute(eq("errorMessage"), contains("Complaint not found"));
        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
    }

    @Test
    @DisplayName("doGet /student/complaints/view redirects on invalid format complaint ID")
    void testDoGetViewComplaintInvalidFormat() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(15);
        when(request.getServletPath()).thenReturn("/student/complaints/view");
        when(request.getParameter("id")).thenReturn("abc");

        servlet.doGet(request, response);

        verify(session).setAttribute(eq("errorMessage"), contains("Invalid complaint ID format"));
        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
    }

    @Test
    @DisplayName("doGet /student/complaints/view redirects on missing or empty complaint ID")
    void testDoGetViewComplaintMissingId() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(15);
        when(request.getServletPath()).thenReturn("/student/complaints/view");
        when(request.getParameter("id")).thenReturn("   ");

        servlet.doGet(request, response);

        verify(session).setAttribute(eq("errorMessage"), contains("Invalid complaint ID"));
        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
    }

    @Test
    @DisplayName("doPost submit succeeds and redirects to dashboard with flash message")
    void testDoPostSubmitSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(20);

        when(request.getParameter("title")).thenReturn("Water cooler not chilling");
        when(request.getParameter("category")).thenReturn("SANITATION");
        when(request.getParameter("description")).thenReturn("Water cooler on 3rd floor is dispensing warm water.");
        when(request.getParameter("location")).thenReturn("Block C, 3rd Floor");

        doAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setComplaintId(105);
            return true;
        }).when(complaintDAO).createComplaint(any(Complaint.class));

        servlet.doPost(request, response);

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintDAO).createComplaint(captor.capture());
        Complaint created = captor.getValue();

        assertEquals(20, created.getStudentId()); // Identity from session
        assertEquals("Water cooler not chilling", created.getTitle());
        assertEquals(ComplaintCategory.SANITATION, created.getCategory());
        assertEquals("Water cooler on 3rd floor is dispensing warm water.", created.getDescription());
        assertEquals("Block C, 3rd Floor", created.getLocation());

        verify(session).setAttribute(eq("successMessage"), contains("#105"));
        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("doPost fails validation on blank title and preserves form input")
    void testDoPostValidationFailureTitleMissing() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(20);

        when(request.getParameter("title")).thenReturn("   ");
        when(request.getParameter("category")).thenReturn("IT");
        when(request.getParameter("description")).thenReturn("Long enough description here.");
        when(request.getParameter("location")).thenReturn("Lab 1");

        servlet.doPost(request, response);

        verify(complaintDAO, never()).createComplaint(any());
        verify(request).setAttribute(eq("errorMessage"), contains("Title is required"));
        verify(request).setAttribute("category", "IT");
        verify(request).setAttribute("location", "Lab 1");
        verify(request).getRequestDispatcher("/WEB-INF/views/student/submit-complaint.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost fails validation on invalid category")
    void testDoPostValidationFailureCategoryInvalid() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(20);

        when(request.getParameter("title")).thenReturn("Valid Complaint Title");
        when(request.getParameter("category")).thenReturn("INVALID_CATEGORY_NAME");
        when(request.getParameter("description")).thenReturn("Valid long description of the complaint.");

        servlet.doPost(request, response);

        verify(complaintDAO, never()).createComplaint(any());
        verify(request).setAttribute(eq("errorMessage"), contains("Invalid category"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost fails validation on description shorter than minimum length")
    void testDoPostValidationFailureDescriptionTooShort() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(20);

        when(request.getParameter("title")).thenReturn("Valid Complaint Title");
        when(request.getParameter("category")).thenReturn("ACADEMICS");
        when(request.getParameter("description")).thenReturn("Short"); // < 10 chars

        servlet.doPost(request, response);

        verify(complaintDAO, never()).createComplaint(any());
        verify(request).setAttribute(eq("errorMessage"), contains("Description is required"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost handles database creation failure gracefully")
    void testDoPostDatabaseError() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(20);

        when(request.getParameter("title")).thenReturn("Valid Title");
        when(request.getParameter("category")).thenReturn("OTHER");
        when(request.getParameter("description")).thenReturn("Valid description longer than 10 chars.");

        when(complaintDAO.createComplaint(any(Complaint.class))).thenReturn(false);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("database error"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost redirects unauthenticated user")
    void testDoPostUnauthenticated() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/campus-complaint-portal/auth/login?reason=session_expired");
        verify(complaintDAO, never()).createComplaint(any());
    }
}
