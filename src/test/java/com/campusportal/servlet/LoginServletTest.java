package com.campusportal.servlet;

import com.campusportal.dao.UserDAO;
import com.campusportal.model.User;
import com.campusportal.util.PasswordUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("LoginServlet tests")
class LoginServletTest {

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
    private UserDAO userDAO;

    private LoginServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new LoginServlet();
        servlet.setUserDAO(userDAO);
        when(request.getContextPath()).thenReturn("/campus-complaint-portal");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet forwards to login.jsp when not logged in")
    void testDoGetUnauthenticated() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/auth/login.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet with reason=session_expired sets infoMessage")
    void testDoGetSessionExpired() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("reason")).thenReturn("session_expired");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("infoMessage"), contains("session has expired"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet with success=registered sets successMessage")
    void testDoGetRegisteredSuccess() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("success")).thenReturn("registered");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("successMessage"), contains("Registration successful"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet redirects to student dashboard when student is already logged in")
    void testDoGetAlreadyLoggedInStudent() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(42);
        when(session.getAttribute("userRole")).thenReturn("STUDENT");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("doPost with blank email or password shows error")
    void testDoPostBlankInputs() throws Exception {
        when(request.getParameter("email")).thenReturn("   ");
        when(request.getParameter("password")).thenReturn("");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Email and password are required"));
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("doPost with non-existent email fails with generic error")
    void testDoPostUserNotFound() throws Exception {
        when(request.getParameter("email")).thenReturn("nonexistent@campus.edu");
        when(request.getParameter("password")).thenReturn("password123");
        when(userDAO.findByEmail("nonexistent@campus.edu")).thenReturn(null);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Invalid email or password"));
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("doPost with wrong password fails with generic error")
    void testDoPostWrongPassword() throws Exception {
        String correctHash = PasswordUtil.hashPassword("CorrectPass123");
        User user = new User("alice@campus.edu", correctHash, "Alice Fernandes", "STUDENT", "STU-001");
        user.setUserId(10);
        user.setActive(true);

        when(request.getParameter("email")).thenReturn("alice@campus.edu");
        when(request.getParameter("password")).thenReturn("WrongPassword");
        when(userDAO.findByEmail("alice@campus.edu")).thenReturn(user);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Invalid email or password"));
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("doPost with inactive account fails with deactivation message")
    void testDoPostInactiveAccount() throws Exception {
        String hash = PasswordUtil.hashPassword("ValidPass123");
        User user = new User("inactive@campus.edu", hash, "Inactive Student", "STUDENT", "STU-999");
        user.setUserId(20);
        user.setActive(false);

        when(request.getParameter("email")).thenReturn("inactive@campus.edu");
        when(request.getParameter("password")).thenReturn("ValidPass123");
        when(userDAO.findByEmail("inactive@campus.edu")).thenReturn(user);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("deactivated"));
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("doPost with valid student credentials renews session and redirects to student dashboard")
    void testDoPostStudentSuccess() throws Exception {
        String hash = PasswordUtil.hashPassword("ValidPass123");
        User student = new User("alice@campus.edu", hash, "Alice Fernandes", "STUDENT", "STU-001");
        student.setUserId(5);
        student.setActive(true);

        when(request.getParameter("email")).thenReturn("alice@campus.edu");
        when(request.getParameter("password")).thenReturn("ValidPass123");
        when(userDAO.findByEmail("alice@campus.edu")).thenReturn(student);

        HttpSession oldSession = mock(HttpSession.class);
        HttpSession freshSession = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(freshSession);

        servlet.doPost(request, response);

        // Verify session fixation protection: old session invalidated
        verify(oldSession).invalidate();
        // Verify session attributes populated
        verify(freshSession).setAttribute("userId", 5);
        verify(freshSession).setAttribute("userRole", "STUDENT");
        verify(freshSession).setAttribute("userName", "Alice Fernandes");
        // Verify redirect
        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
    }

    @Test
    @DisplayName("doPost with valid admin credentials redirects to admin dashboard")
    void testDoPostAdminSuccess() throws Exception {
        String hash = PasswordUtil.hashPassword("AdminPass#2026");
        User admin = new User("admin@campus.edu", hash, "Admin Dev", "ADMIN", null);
        admin.setUserId(1);
        admin.setActive(true);

        when(request.getParameter("email")).thenReturn("admin@campus.edu");
        when(request.getParameter("password")).thenReturn("AdminPass#2026");
        when(userDAO.findByEmail("admin@campus.edu")).thenReturn(admin);

        HttpSession freshSession = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(freshSession);

        servlet.doPost(request, response);

        verify(freshSession).setAttribute("userId", 1);
        verify(freshSession).setAttribute("userRole", "ADMIN");
        verify(freshSession).setAttribute("userName", "Admin Dev");
        verify(response).sendRedirect("/campus-complaint-portal/admin/dashboard");
    }
}
