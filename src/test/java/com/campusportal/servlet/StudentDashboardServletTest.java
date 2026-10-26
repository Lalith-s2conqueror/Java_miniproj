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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StudentDashboardServlet tests")
class StudentDashboardServletTest {

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

    private StudentDashboardServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new StudentDashboardServlet();
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
    @DisplayName("doGet populates complaints and stats for authenticated student")
    void testDoGetSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(42);

        Complaint complaint = new Complaint(42, "Broken light", ComplaintCategory.INFRASTRUCTURE, "Light broken in Lab 2", "Lab 2");
        List<Complaint> list = Collections.singletonList(complaint);
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 1);
        stats.put("resolved", 0);
        stats.put("pending", 1);

        when(complaintDAO.findByStudentId(42)).thenReturn(list);
        when(complaintDAO.getStatsByStudentId(42)).thenReturn(stats);

        servlet.doGet(request, response);

        verify(request).setAttribute("complaints", list);
        verify(request).setAttribute("stats", stats);
        verify(request).getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet transfers session flash messages to request attributes")
    void testDoGetWithFlashMessages() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(42);
        when(session.getAttribute("successMessage")).thenReturn("Complaint #5 submitted successfully!");
        when(session.getAttribute("errorMessage")).thenReturn("Access denied");

        when(complaintDAO.findByStudentId(42)).thenReturn(Collections.emptyList());
        when(complaintDAO.getStatsByStudentId(42)).thenReturn(Collections.emptyMap());

        servlet.doGet(request, response);

        verify(request).setAttribute("successMessage", "Complaint #5 submitted successfully!");
        verify(request).setAttribute("errorMessage", "Access denied");
        verify(session).removeAttribute("successMessage");
        verify(session).removeAttribute("errorMessage");
        verify(dispatcher).forward(request, response);
    }
}
