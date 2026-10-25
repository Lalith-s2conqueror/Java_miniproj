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
 * Controller for student registration.
 *
 * <p>Handles GET requests to display the registration form and POST requests to validate input,
 * prevent duplicate accounts, hash passwords using BCrypt, and persist new student users.
 * Public registrations are strictly assigned the "STUDENT" role.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/auth/register"})
public class RegisterServlet extends HttpServlet {

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

        // If already logged in, redirect to appropriate dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            String role = (String) session.getAttribute("userRole");
            if ("ADMIN".equalsIgnoreCase(role)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = ValidationUtil.safeTrim(request.getParameter("fullName"));
        String email = ValidationUtil.safeTrim(request.getParameter("email"));
        String rollNumber = ValidationUtil.safeTrim(request.getParameter("rollNumber"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Helper to preserve input data on error (never passwords)
        Runnable preserveInputs = () -> {
            request.setAttribute("fullName", fullName);
            request.setAttribute("email", email);
            request.setAttribute("rollNumber", rollNumber);
        };

        // 1. Validate required fields
        if (!ValidationUtil.isValidFullName(fullName)) {
            preserveInputs.run();
            request.setAttribute("errorMessage", "Full name is required (maximum 150 characters).");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            preserveInputs.run();
            request.setAttribute("errorMessage", "A valid email address is required.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!ValidationUtil.isValidIdentifier(rollNumber)) {
            preserveInputs.run();
            request.setAttribute("errorMessage", "Roll number is mandatory for student registration.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            preserveInputs.run();
            request.setAttribute("errorMessage", "Password must be at least " + ValidationUtil.MIN_PASSWORD_LENGTH + " characters.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        if (!ValidationUtil.doPasswordsMatch(password, confirmPassword)) {
            preserveInputs.run();
            request.setAttribute("errorMessage", "Passwords do not match.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 2. Prevent duplicate email accounts
        if (userDAO.emailExists(email)) {
            preserveInputs.run();
            request.setAttribute("errorMessage", "An account with this email address already exists.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 3. Security: Public registration MUST ALWAYS assign STUDENT role
        String passwordHash = PasswordUtil.hashPassword(password);
        User newUser = new User(email, passwordHash, fullName, "STUDENT", rollNumber);

        boolean created = userDAO.createUser(newUser);
        if (!created) {
            preserveInputs.run();
            request.setAttribute("errorMessage", "Registration failed due to a system error. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 4. Redirect on success to login page with success flag
        response.sendRedirect(request.getContextPath() + "/auth/login?success=registered");
    }
}
