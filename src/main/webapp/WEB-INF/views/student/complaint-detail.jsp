<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Complaint Detail – Campus Complaint Portal</title>
</head>
<body>
    <%-- TODO (Member 1 – Frontend + Member 3 – Complaints): Implement complaint detail view.
         Request attributes available:
           ${complaint}  – Complaint object (complaint_id, title, category, description,
                           location, status, adminRemarks, submittedAt, updatedAt)
         SECURITY: Servlet already verified complaint.studentId == session.userId before forwarding.
    --%>
    <p>Complaint detail – to be implemented.</p>
    <p><a href="${pageContext.request.contextPath}/student/dashboard">Back to Dashboard</a></p>
</body>
</html>
