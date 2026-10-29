<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Registration – Campus Complaint Portal</title>
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #f4f6f9;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            color: #2d2d2d;
            padding: 24px;
        }
        .card {
            background: #ffffff;
            border-radius: 10px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.10);
            padding: 40px;
            max-width: 480px;
            width: 100%;
        }
        .header { text-align: center; margin-bottom: 24px; }
        .header h1 { font-size: 1.6rem; font-weight: 700; margin-bottom: 6px; }
        .header p { color: #666; font-size: 0.9rem; }
        .form-group { margin-bottom: 16px; }
        label { display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: #444; }
        input[type="text"], input[type="email"], input[type="password"] {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 0.95rem;
            transition: border-color 0.2s;
        }
        input[type="text"]:focus, input[type="email"]:focus, input[type="password"]:focus {
            outline: none;
            border-color: #1a56db;
            box-shadow: 0 0 0 3px rgba(26, 86, 219, 0.15);
        }
        .field-hint { font-size: 0.75rem; color: #777; margin-top: 4px; }
        .btn {
            display: block;
            width: 100%;
            background: #1a56db;
            color: #fff;
            border: none;
            border-radius: 6px;
            padding: 12px;
            font-size: 0.95rem;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s;
            margin-top: 14px;
        }
        .btn:hover { background: #1648c0; }
        .alert {
            padding: 12px 14px;
            border-radius: 6px;
            font-size: 0.85rem;
            margin-bottom: 20px;
            line-height: 1.4;
        }
        .alert-danger { background: #fde8e8; color: #9b1c1c; border: 1px solid #f8b4b4; }
        .footer-links {
            text-align: center;
            margin-top: 20px;
            font-size: 0.85rem;
            color: #666;
        }
        .footer-links a { color: #1a56db; text-decoration: none; font-weight: 600; }
        .footer-links a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="card">
        <div class="header">
            <h1>Create Account</h1>
            <p>Register as a student to submit and track campus complaints</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">${errorMessage}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/register" method="post" novalidate>
            <div class="form-group">
                <label for="fullName">Full Name</label>
                <input type="text" id="fullName" name="fullName"
                       value="<c:out value='${not empty fullName ? fullName : param.fullName}' />"
                       placeholder="e.g. Alice Fernandes" required autocomplete="name">
            </div>

            <div class="form-group">
                <label for="rollNumber">Roll / Student ID Number</label>
                <input type="text" id="rollNumber" name="rollNumber"
                       value="<c:out value='${not empty rollNumber ? rollNumber : param.rollNumber}' />"
                       placeholder="e.g. STU-2023-001" required>
                <div class="field-hint">Mandatory for student verification.</div>
            </div>

            <div class="form-group">
                <label for="email">Campus Email</label>
                <input type="email" id="email" name="email"
                       value="<c:out value='${not empty email ? email : param.email}' />"
                       placeholder="you@campus.edu" required autocomplete="email">
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password"
                       placeholder="At least 6 characters" required autocomplete="new-password">
            </div>

            <div class="form-group">
                <label for="confirmPassword">Confirm Password</label>
                <input type="password" id="confirmPassword" name="confirmPassword"
                       placeholder="Re-type your password" required autocomplete="new-password">
            </div>

            <button type="submit" class="btn">Register Account</button>
        </form>

        <div class="footer-links">
            Already registered? <a href="${pageContext.request.contextPath}/auth/login">Back to Sign In</a>
            <br><br>
            <a href="${pageContext.request.contextPath}/index.jsp">&larr; Back to Home</a>
        </div>
    </div>
</body>
</html>
