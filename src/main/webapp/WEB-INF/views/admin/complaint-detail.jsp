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
    
    <h1>Complaint Detail</h1>

<c:if test="${not empty successMessage}">
    <p>${successMessage}</p>
</c:if>

<c:if test="${not empty errorMessage}">
    <p>${errorMessage}</p>
</c:if>

<h2>Complaint Information</h2>

<p><strong>ID:</strong> ${complaint.complaintId}</p>
<p><strong>Student:</strong> ${complaint.studentId}</p>
<p><strong>Category:</strong> ${complaint.category}</p>
<p><strong>Status:</strong> ${complaint.status}</p>
<p><strong>Title:</strong> ${complaint.title}</p>

<hr>

<h2>Update Status</h2>

<form action="${pageContext.request.contextPath}/admin/complaints/update"
      method="post">

    <input type="hidden"
           name="complaintId"
           value="${complaint.complaintId}">

    <label>New Status:</label>

    <select name="newStatus">

        <option value="UNDER_REVIEW">UNDER_REVIEW</option>

        <option value="IN_PROGRESS">IN_PROGRESS</option>

        <option value="RESOLVED">RESOLVED</option>

        <option value="REJECTED">REJECTED</option>

    </select>

    <br><br>

    <label>Admin Remarks:</label>

    <br>

    <textarea name="adminRemarks"
              rows="5"
              cols="40"></textarea>

    <br><br>

    <button type="submit">
        Update Status
    </button>

</form>
    <p><a href="${pageContext.request.contextPath}/admin/dashboard">Back to Dashboard</a></p>
</body>
</html>
