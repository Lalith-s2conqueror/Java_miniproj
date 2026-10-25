<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register – Campus Complaint Portal</title>
    <%-- TODO (Member 1): Link shared stylesheet once created --%>
</head>
<body>
    <%-- TODO (Member 2 – Auth): Implement registration form.
         Required form fields: fullName, email, password, confirmPassword, rollNumber
         POST action: ${pageContext.request.contextPath}/auth/register
         Display ${errorMessage} if present.
         Note: rollNumber maps to the `identifier` column; mandatory for students.
    --%>
    <p>Registration form – to be implemented.</p>
    <p><a href="${pageContext.request.contextPath}/auth/login">Back to Login</a></p>
</body>
</html>
