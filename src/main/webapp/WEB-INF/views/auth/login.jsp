<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login – Campus Complaint Portal</title>
    <%-- TODO (Member 1): Link shared stylesheet once created --%>
</head>
<body>
    <%-- TODO (Member 2 – Auth): Implement login form.
         Required form fields: email, password
         POST action: ${pageContext.request.contextPath}/auth/login
         Display ${errorMessage} if present.
         Display success notice if ?success=registered param is present.
    --%>
    <p>Login form – to be implemented.</p>
    <p><a href="${pageContext.request.contextPath}/auth/register">Register</a></p>
</body>
</html>
