package com.campusportal.servlet;

import com.campusportal.dao.UserDAO;
import com.campusportal.model.User;
import com.campusportal.util.PasswordUtil;
import com.campusportal.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for user authentication (login).
 *
 * <p>Handles GET requests to display the login form and POST requests to authenticate credentials.
 * Implements session renewal to prevent session fixation and enforces role-based redirection.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/auth/login"})
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        if (userDAO == null) {
            userDAO = new UserDAO();
        }
    }

    /**
     * Package-private setter for unit testing dependency injection.
     */
    void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is already logged in
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null && existingSession.getAttribute("userId") != null) {
            String role = (String) existingSession.getAttribute("userRole");
            if ("ADMIN".equalsIgnoreCase(role)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }
        }

        String reason = request.getParameter("reason");
        if ("session_expired".equalsIgnoreCase(reason)) {
            request.setAttribute("infoMessage", "Your session has expired. Please log in again.");
        }

        String success = request.getParameter("success");
        if ("registered".equalsIgnoreCase(success)) {
            request.setAttribute("successMessage", "Registration successful! You may now log in.");
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = ValidationUtil.safeTrim(request.getParameter("email"));
        String password = request.getParameter("password");

        // Input validation
        if (!ValidationUtil.isNotBlank(email) || !ValidationUtil.isNotBlank(password)) {
            request.setAttribute("errorMessage", "Email and password are required.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.findByEmail(email);

        // Verify credentials safely (constant-time / BCrypt comparison; avoid user enumeration)
        if (user == null || !PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            request.setAttribute("errorMessage", "Invalid email or password.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        if (!user.isActive()) {
            request.setAttribute("errorMessage", "This account has been deactivated. Please contact an administrator.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        // Renew session to prevent session fixation attacks
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = request.getSession(true);

        // Populate session attributes as specified in API contract §7
        newSession.setAttribute("userId", user.getUserId());
        newSession.setAttribute("userRole", user.getRole());
        newSession.setAttribute("userName", user.getFullName());

        // Role-based redirection
        if (user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
    }
}
