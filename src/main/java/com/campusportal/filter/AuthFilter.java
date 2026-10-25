package com.campusportal.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Security filter for protecting private student and administrator pages.
 *
 * <p>Registered deterministically in {@code web.xml} for {@code /student/*} and {@code /admin/*}.
 * Enforces:
 * <ul>
 *   <li>Active session check: Unauthenticated requests redirect to login with {@code reason=session_expired}.</li>
 *   <li>Role check: Non-admin users attempting to access {@code /admin/*} are blocked and redirected to the student dashboard.</li>
 * </ul>
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        Object userId = (session != null) ? session.getAttribute("userId") : null;

        // 1. Session check: if no active session or userId missing, redirect to login
        if (userId == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?reason=session_expired");
            return;
        }

        // 2. Role authorization check for admin routes
        String requestUri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = requestUri.substring(contextPath.length());

        if (relativePath.startsWith("/admin/")) {
            Object role = session.getAttribute("userRole");
            if (!"ADMIN".equalsIgnoreCase(String.valueOf(role))) {
                // Non-admin attempting to access admin route: deny access and redirect to student dashboard
                session.setAttribute("errorMessage", "Access denied: Administrator privileges required.");
                httpResponse.sendRedirect(contextPath + "/student/dashboard");
                return;
            }
        }

        // Authorized: continue down the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
