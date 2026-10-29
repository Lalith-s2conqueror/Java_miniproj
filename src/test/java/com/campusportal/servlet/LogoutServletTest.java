package com.campusportal.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

@DisplayName("LogoutServlet tests")
class LogoutServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private LogoutServlet servlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LogoutServlet();
        when(request.getContextPath()).thenReturn("/campus-complaint-portal");
    }

    @Test
    @DisplayName("doGet invalidates active session and redirects to login")
    void testDoGetWithActiveSession() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/campus-complaint-portal/auth/login");
    }

    @Test
    @DisplayName("doGet with no existing session redirects to login without error")
    void testDoGetWithoutSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/campus-complaint-portal/auth/login");
    }

    @Test
    @DisplayName("doPost invalidates active session and redirects to login")
    void testDoPostWithActiveSession() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/campus-complaint-portal/auth/login");
    }
}
