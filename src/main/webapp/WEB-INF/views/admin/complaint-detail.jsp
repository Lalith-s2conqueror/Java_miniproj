<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Complaint Detail (Admin) – Campus Complaint Portal</title>
</head>
<body>
    <%-- TODO (Member 4 – Admin): Implement admin complaint detail and update form.
         Request attributes available:
           ${complaint}  – Complaint object
         Update form fields: complaintId (hidden), newStatus (dropdown), adminRemarks (textarea)
         POST action: ${pageContext.request.contextPath}/admin/complaints/update
         Valid status transitions (enforce in dropdown options):
           SUBMITTED     → UNDER_REVIEW
           UNDER_REVIEW  → IN_PROGRESS, REJECTED
           IN_PROGRESS   → RESOLVED, REJECTED
           RESOLVED      → UNDER_REVIEW  (reopen)
           REJECTED      → (no options – terminal)
         Display ${errorMessage} or ${successMessage} if present.
    --%>
    <p>Admin complaint detail – to be implemented.</p>
    <p><a href="${pageContext.request.contextPath}/admin/dashboard">Back to Dashboard</a></p>
</body>
</html>
