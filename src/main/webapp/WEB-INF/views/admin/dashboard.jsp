<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard – Campus Complaint Portal</title>
</head>
<body>
    <%-- TODO (Member 4 – Admin): Implement admin dashboard.
         Request attributes available:
           ${complaints}  – List<Complaint> (all, possibly filtered)
           ${stats}       – Map with aggregate counts by status and category
         Supported filter query params: ?status=...&category=...&search=...
         Actions:
           View complaint: ${pageContext.request.contextPath}/admin/complaints/view?id=<id>
           Logout:         ${pageContext.request.contextPath}/auth/logout
    --%>
    <p>Admin dashboard – to be implemented.</p>
</body>
</html>
