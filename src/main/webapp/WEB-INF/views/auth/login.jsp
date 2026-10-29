<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login – Campus Complaint Portal</title>
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
            padding: 20px;
        }
        .card {
            background: #ffffff;
            border-radius: 10px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.10);
            padding: 40px;
            max-width: 420px;
            width: 100%;
        }
        .header { text-align: center; margin-bottom: 24px; }
        .header h1 { font-size: 1.6rem; font-weight: 700; margin-bottom: 6px; }
        .header p { color: #666; font-size: 0.9rem; }
        .form-group { margin-bottom: 18px; }
        label { display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 6px; color: #444; }
        input[type="email"], input[type="password"] {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 0.95rem;
            transition: border-color 0.2s;
        }
        input[type="email"]:focus, input[type="password"]:focus {
            outline: none;
            border-color: #1a56db;
            box-shadow: 0 0 0 3px rgba(26, 86, 219, 0.15);
        }
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
            margin-top: 10px;
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
        .alert-success { background: #def7ec; color: #03543f; border: 1px solid #84e1bc; }
        .alert-warning { background: #fef08a; color: #713f12; border: 1px solid #fde047; }
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
            <h1>Sign In</h1>
            <p>Access your Campus Complaint Portal account</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">${errorMessage}</div>
        </c:if>

        <c:if test="${not empty infoMessage || param.reason eq 'session_expired'}">
            <div class="alert alert-warning">
                <c:out value="${not empty infoMessage ? infoMessage : 'Your session has expired. Please log in again.'}" />
            </div>
        </c:if>

        <c:if test="${not empty successMessage || param.success eq 'registered'}">
            <div class="alert alert-success">
                <c:out value="${not empty successMessage ? successMessage : 'Registration successful! You may now sign in.'}" />
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login" method="post" novalidate>
            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email" id="email" name="email"
                       value="<c:out value='${not empty email ? email : param.email}' />"
                       placeholder="you@campus.edu" required autocomplete="email">
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password"
                       placeholder="Enter your password" required autocomplete="current-password">
            </div>

            <button type="submit" class="btn">Sign In</button>
        </form>

        <div class="footer-links">
            Don't have an account? <a href="${pageContext.request.contextPath}/auth/register">Register</a>
            <br><br>
            <a href="${pageContext.request.contextPath}/index.jsp">&larr; Back to Home</a>
        </div>
    </div>
</body>
</html>
