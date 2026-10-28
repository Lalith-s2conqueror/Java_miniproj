package com.campusportal.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminComplaintServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/complaint-detail.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String complaintId = request.getParameter("complaintId");
        String newStatus = request.getParameter("newStatus");
        String adminRemarks = request.getParameter("adminRemarks");

        // TODO: Update complaint status

        response.sendRedirect(
                request.getContextPath()
                        + "/admin/dashboard");
    }
}
