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
    
    <h1>Admin Dashboard</h1>

<a href="${pageContext.request.contextPath}/auth/logout">
    Logout
</a>

<hr>

<h2>Complaint Filters</h2>

<form method="get" action="${pageContext.request.contextPath}/admin/dashboard">

    Status:
    <input type="text" name="status">

    Category:
    <input type="text" name="category">

    Search:
    <input type="text" name="search">

    <button type="submit">Filter</button>

</form>

<hr>

<h2>All Complaints</h2>

<table border="1">

<tr>
    <th>Complaint ID</th>
    <th>Student ID</th>
    <th>Category</th>
    <th>Status</th>
    <th>Title</th>
    <th>Action</th>
</tr>

<c:forEach items="${complaints}" var="complaint">

<tr>
    <td>${complaint.complaintId}</td>
    <td>${complaint.studentId}</td>
    <td>${complaint.category}</td>
    <td>${complaint.status}</td>
    <td>${complaint.title}</td>

    <td>
        <a href="${pageContext.request.contextPath}/admin/complaints/view?id=${complaint.complaintId}">
            View
        </a>
    </td>
</tr>

</c:forEach>

</table>
</body>
</html>
