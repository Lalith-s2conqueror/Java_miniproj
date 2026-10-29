package com.campusportal.servlet;

import com.campusportal.dao.ComplaintDAO;
import com.campusportal.model.Complaint;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for the student dashboard.
 *
 * <p>Handles GET requests to display the authenticated student's summary stats
 * and list of submitted complaints.
 */
@WebServlet(name = "StudentDashboardServlet", urlPatterns = {"/student/dashboard"})
public class StudentDashboardServlet extends HttpServlet {

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

        // Handle flash messages stored in session from redirects
        Object flashSuccess = session.getAttribute("successMessage");
        if (flashSuccess != null) {
            request.setAttribute("successMessage", flashSuccess);
            session.removeAttribute("successMessage");
        }

        Object flashError = session.getAttribute("errorMessage");
        if (flashError != null) {
            request.setAttribute("errorMessage", flashError);
            session.removeAttribute("errorMessage");
        }

        // Retrieve complaints and stats strictly for the authenticated student
        List<Complaint> complaints = complaintDAO.findByStudentId(studentId);
        Map<String, Integer> stats = complaintDAO.getStatsByStudentId(studentId);

        request.setAttribute("complaints", complaints);
        request.setAttribute("stats", stats);

        request.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(request, response);
    }
}
