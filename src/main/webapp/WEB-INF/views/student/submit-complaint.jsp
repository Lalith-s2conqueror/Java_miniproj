<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Submit Complaint – Campus Complaint Portal</title>
</head>
<body>
    <%-- TODO (Member 1 – Frontend + Member 3 – Complaints): Implement complaint form.
         Required form fields: title, category (dropdown), description, location (optional)
         POST action: ${pageContext.request.contextPath}/student/complaints/submit
         Category options: INFRASTRUCTURE, SANITATION, ACADEMICS, IT, OTHER
         Display ${errorMessage} if present.
         SECURITY: Do NOT include student_id as a hidden field. It is read from the session.
    --%>
    <p>Submit complaint form – to be implemented.</p>
    <p><a href="${pageContext.request.contextPath}/student/dashboard">Back to Dashboard</a></p>
</body>
</html>
