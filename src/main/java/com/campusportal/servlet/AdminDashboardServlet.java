package com.campusportal.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

                request.setAttribute(
        "successMessage",
        "Complaint status updated successfully.");

        request.getRequestDispatcher(
                "/WEB-INF/views/admin/dashboard.jsp")
                .forward(request, response);
    }
}
