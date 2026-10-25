<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard – Campus Complaint Portal</title>
</head>
<body>
    <%-- TODO (Member 1 – Frontend + Member 3 – Complaints): Implement student dashboard.
         Request attributes available:
           ${complaints}  – List<Complaint> for this student
           ${stats}       – Map with keys: total, resolved, pending
           ${userName}    – from session
         Actions:
           New complaint: ${pageContext.request.contextPath}/student/complaints/new
           View detail:   ${pageContext.request.contextPath}/student/complaints/view?id=<id>
           Logout:        ${pageContext.request.contextPath}/auth/logout
    --%>
    <p>Student dashboard – to be implemented.</p>
</body>
</html>
