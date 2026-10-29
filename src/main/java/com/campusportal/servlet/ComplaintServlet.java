package com.campusportal.servlet;

import com.campusportal.dao.ComplaintDAO;
import com.campusportal.model.Complaint;
import com.campusportal.model.ComplaintCategory;
import com.campusportal.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for student complaint submission, retrieval, and tracking.
 *
 * <p>Handles:
 * <ul>
 *   <li>GET {@code /student/complaints/new} – Displays the complaint submission form.</li>
 *   <li>GET {@code /student/complaints/view?id=<id>} – Displays details for a single complaint
 *       after strict student ownership verification.</li>
 *   <li>POST {@code /student/complaints/submit} – Validates and creates a new complaint.</li>
 * </ul>
 */
@WebServlet(name = "ComplaintServlet", urlPatterns = {
        "/student/complaints/new",
        "/student/complaints/submit",
        "/student/complaints/view"
})
public class ComplaintServlet extends HttpServlet {

    private ComplaintDAO complaintDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        if (complaintDAO == null) {
            complaintDAO = new ComplaintDAO();
        }
    }

    /**
     * Package-private setter for unit testing dependency injection.
     */
    void setComplaintDAO(ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?reason=session_expired");
            return;
        }

        int studentId = (Integer) session.getAttribute("userId");
        String servletPath = request.getServletPath();

        if ("/student/complaints/new".equals(servletPath)) {
            request.getRequestDispatcher("/WEB-INF/views/student/submit-complaint.jsp").forward(request, response);
            return;
        }

        if ("/student/complaints/view".equals(servletPath)) {
            String idParam = request.getParameter("id");
            if (!ValidationUtil.isNotBlank(idParam)) {
                session.setAttribute("errorMessage", "Invalid complaint ID.");
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }

            int complaintId;
            try {
                complaintId = Integer.parseInt(idParam.trim());
                if (complaintId <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "Invalid complaint ID format.");
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }

            Complaint complaint = complaintDAO.findById(complaintId);
            if (complaint == null) {
                session.setAttribute("errorMessage", "Complaint not found.");
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }

            // Security: verify that the authenticated student owns this complaint
            if (complaint.getStudentId() != studentId) {
                session.setAttribute("errorMessage", "Access denied: You are not authorized to view this complaint.");
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            }

            request.setAttribute("complaint", complaint);
            request.getRequestDispatcher("/WEB-INF/views/student/complaint-detail.jsp").forward(request, response);
            return;
        }

        // Default fallback to dashboard
        response.sendRedirect(request.getContextPath() + "/student/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?reason=session_expired");
            return;
        }

        // Never read student identity from form parameters; derive strictly from server-side session
        int studentId = (Integer) session.getAttribute("userId");

        String title = ValidationUtil.safeTrim(request.getParameter("title"));
        String categoryRaw = ValidationUtil.safeTrim(request.getParameter("category"));
        String description = ValidationUtil.safeTrim(request.getParameter("description"));
        String location = ValidationUtil.safeTrim(request.getParameter("location"));

        // Server-side input validation
        String errorMessage = null;
        ComplaintCategory category = null;

        if (!ValidationUtil.isValidComplaintTitle(title)) {
            errorMessage = "Title is required and must be between "
                    + ValidationUtil.MIN_TITLE_LENGTH + " and "
                    + ValidationUtil.MAX_TITLE_LENGTH + " characters.";
        } else if (categoryRaw == null || categoryRaw.isEmpty()) {
            errorMessage = "Please select a valid complaint category.";
        } else {
            try {
                category = ComplaintCategory.fromDbValue(categoryRaw);
            } catch (IllegalArgumentException e) {
                errorMessage = "Invalid category selected. Please choose from the available categories.";
            }
        }

        if (errorMessage == null && !ValidationUtil.isValidComplaintDescription(description)) {
            errorMessage = "Description is required and must be between "
                    + ValidationUtil.MIN_DESCRIPTION_LENGTH + " and "
                    + ValidationUtil.MAX_DESCRIPTION_LENGTH + " characters.";
        }

        if (errorMessage == null && !ValidationUtil.isValidComplaintLocation(location)) {
            errorMessage = "Location must not exceed " + ValidationUtil.MAX_LOCATION_LENGTH + " characters.";
        }

        if (errorMessage != null) {
            // Preserve user input and display error message
            request.setAttribute("errorMessage", errorMessage);
            request.setAttribute("title", title);
            request.setAttribute("category", categoryRaw);
            request.setAttribute("description", description);
            request.setAttribute("location", location);
            request.getRequestDispatcher("/WEB-INF/views/student/submit-complaint.jsp").forward(request, response);
            return;
        }

        Complaint complaint = new Complaint(studentId, title, category, description, location);
        boolean success = complaintDAO.createComplaint(complaint);

        if (success) {
            session.setAttribute("successMessage", "Complaint #" + complaint.getComplaintId() + " submitted successfully.");
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        } else {
            request.setAttribute("errorMessage", "A database error occurred while submitting your complaint. Please try again.");
            request.setAttribute("title", title);
            request.setAttribute("category", categoryRaw);
            request.setAttribute("description", description);
            request.setAttribute("location", location);
            request.getRequestDispatcher("/WEB-INF/views/student/submit-complaint.jsp").forward(request, response);
        }
    }
}
