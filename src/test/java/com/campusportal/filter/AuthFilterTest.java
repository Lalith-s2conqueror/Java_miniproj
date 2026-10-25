package com.campusportal.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

@DisplayName("AuthFilter security tests")
class AuthFilterTest {

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
    private FilterChain chain;

    private AuthFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new AuthFilter();
        when(request.getContextPath()).thenReturn("/campus-complaint-portal");
    }

    @Test
    @DisplayName("Unauthenticated request to /student/dashboard redirects to login with reason=session_expired")
    void testUnauthenticatedStudentRoute() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/campus-complaint-portal/auth/login?reason=session_expired");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Unauthenticated request to /admin/dashboard redirects to login with reason=session_expired")
    void testUnauthenticatedAdminRoute() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response).sendRedirect("/campus-complaint-portal/auth/login?reason=session_expired");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Authenticated student accessing /student/dashboard is allowed through")
    void testAuthenticatedStudentAllowed() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(101);
        when(session.getAttribute("userRole")).thenReturn("STUDENT");
        when(request.getRequestURI()).thenReturn("/campus-complaint-portal/student/dashboard");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Authenticated student accessing /admin/dashboard is blocked and redirected to student dashboard")
    void testStudentBlockedFromAdminRoute() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(101);
        when(session.getAttribute("userRole")).thenReturn("STUDENT");
        when(request.getRequestURI()).thenReturn("/campus-complaint-portal/admin/dashboard");

        filter.doFilter(request, response, chain);

        verify(session).setAttribute(eq("errorMessage"), contains("Access denied"));
        verify(response).sendRedirect("/campus-complaint-portal/student/dashboard");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Authenticated admin accessing /admin/dashboard is allowed through")
    void testAdminAllowedOnAdminRoute() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("userRole")).thenReturn("ADMIN");
        when(request.getRequestURI()).thenReturn("/campus-complaint-portal/admin/dashboard");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("Authenticated admin accessing /student/dashboard is allowed through")
    void testAdminAllowedOnStudentRoute() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("userRole")).thenReturn("ADMIN");
        when(request.getRequestURI()).thenReturn("/campus-complaint-portal/student/dashboard");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
