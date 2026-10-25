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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RegisterServlet tests")
class RegisterServletTest {

    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private UserDAO userDAO;

    private RegisterServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new RegisterServlet();
        servlet.setUserDAO(userDAO);
        when(request.getContextPath()).thenReturn("/campus-complaint-portal");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet forwards to register.jsp when not logged in")
    void testDoGet() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(request).getRequestDispatcher("/WEB-INF/views/auth/register.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet redirects to dashboard when already logged in")
    void testDoGetAlreadyLoggedIn() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(session.getAttribute("userRole")).thenReturn("STUDENT");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("doPost fails when full name is blank")
    void testDoPostBlankFullName() throws Exception {
        when(request.getParameter("fullName")).thenReturn("   ");
        when(request.getParameter("email")).thenReturn("alice@campus.edu");
        when(request.getParameter("rollNumber")).thenReturn("STU-001");
        when(request.getParameter("password")).thenReturn("Pass123");
        when(request.getParameter("confirmPassword")).thenReturn("Pass123");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Full name is required"));
        verify(dispatcher).forward(request, response);
        verify(userDAO, never()).createUser(any());
    }

    @Test
    @DisplayName("doPost fails when email format is invalid")
    void testDoPostInvalidEmail() throws Exception {
        when(request.getParameter("fullName")).thenReturn("Alice Fernandes");
        when(request.getParameter("email")).thenReturn("not-an-email");
        when(request.getParameter("rollNumber")).thenReturn("STU-001");
        when(request.getParameter("password")).thenReturn("Pass123");
        when(request.getParameter("confirmPassword")).thenReturn("Pass123");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("valid email address"));
        verify(dispatcher).forward(request, response);
        verify(userDAO, never()).createUser(any());
    }

    @Test
    @DisplayName("doPost fails when roll number is missing")
    void testDoPostMissingRollNumber() throws Exception {
        when(request.getParameter("fullName")).thenReturn("Alice Fernandes");
        when(request.getParameter("email")).thenReturn("alice@campus.edu");
        when(request.getParameter("rollNumber")).thenReturn("  ");
        when(request.getParameter("password")).thenReturn("Pass123");
        when(request.getParameter("confirmPassword")).thenReturn("Pass123");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Roll number is mandatory"));
        verify(dispatcher).forward(request, response);
        verify(userDAO, never()).createUser(any());
    }

    @Test
    @DisplayName("doPost fails when password is shorter than 6 characters")
    void testDoPostShortPassword() throws Exception {
        when(request.getParameter("fullName")).thenReturn("Alice Fernandes");
        when(request.getParameter("email")).thenReturn("alice@campus.edu");
        when(request.getParameter("rollNumber")).thenReturn("STU-001");
        when(request.getParameter("password")).thenReturn("12345");
        when(request.getParameter("confirmPassword")).thenReturn("12345");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("at least 6 characters"));
        verify(dispatcher).forward(request, response);
        verify(userDAO, never()).createUser(any());
    }

    @Test
    @DisplayName("doPost fails when passwords do not match")
    void testDoPostMismatchedPasswords() throws Exception {
        when(request.getParameter("fullName")).thenReturn("Alice Fernandes");
        when(request.getParameter("email")).thenReturn("alice@campus.edu");
        when(request.getParameter("rollNumber")).thenReturn("STU-001");
        when(request.getParameter("password")).thenReturn("SecretPass123");
        when(request.getParameter("confirmPassword")).thenReturn("DifferentPass");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Passwords do not match"));
        verify(dispatcher).forward(request, response);
        verify(userDAO, never()).createUser(any());
    }

    @Test
    @DisplayName("doPost fails when email is already registered")
    void testDoPostDuplicateEmail() throws Exception {
        when(request.getParameter("fullName")).thenReturn("Alice Fernandes");
        when(request.getParameter("email")).thenReturn("existing@campus.edu");
        when(request.getParameter("rollNumber")).thenReturn("STU-001");
        when(request.getParameter("password")).thenReturn("SecretPass123");
        when(request.getParameter("confirmPassword")).thenReturn("SecretPass123");
        when(userDAO.emailExists("existing@campus.edu")).thenReturn(true);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("already exists"));
        verify(dispatcher).forward(request, response);
        verify(userDAO, never()).createUser(any());
    }

    @Test
    @DisplayName("doPost succeeds with valid inputs: hashes password with BCrypt, forces role STUDENT, redirects to login")
    void testDoPostSuccess() throws Exception {
        when(request.getParameter("fullName")).thenReturn("Bob Sharma");
        when(request.getParameter("email")).thenReturn("bob@campus.edu");
        when(request.getParameter("rollNumber")).thenReturn("STU-2023-002");
        when(request.getParameter("password")).thenReturn("BobSecretPass123");
        when(request.getParameter("confirmPassword")).thenReturn("BobSecretPass123");
        when(userDAO.emailExists("bob@campus.edu")).thenReturn(false);
        when(userDAO.createUser(any(User.class))).thenReturn(true);

        servlet.doPost(request, response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).createUser(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("bob@campus.edu", capturedUser.getEmail());
        assertEquals("Bob Sharma", capturedUser.getFullName());
        assertEquals("STU-2023-002", capturedUser.getIdentifier());
        // Security check: Must always be STUDENT role
        assertEquals("STUDENT", capturedUser.getRole());
        // Password must be hashed with BCrypt, not plaintext
        assertNotEquals("BobSecretPass123", capturedUser.getPasswordHash());
        assertTrue(PasswordUtil.checkPassword("BobSecretPass123", capturedUser.getPasswordHash()));

        verify(response).sendRedirect("/campus-complaint-portal/auth/login?success=registered");
    }
}
