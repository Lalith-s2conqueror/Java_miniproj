<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Campus Complaint Portal</title>
    <style>
        /* Minimal inline styles – Team Member 1 will replace this with proper CSS */
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #f4f6f9;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            color: #2d2d2d;
        }
        .card {
            background: #ffffff;
            border-radius: 10px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.10);
            padding: 48px 56px;
            max-width: 480px;
            width: 100%;
            text-align: center;
        }
        .badge {
            display: inline-block;
            background: #e8f0fe;
            color: #1a56db;
            font-size: 0.75rem;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            border-radius: 20px;
            padding: 4px 14px;
            margin-bottom: 20px;
        }
        h1 { font-size: 1.8rem; font-weight: 700; margin-bottom: 12px; }
        p  { color: #555; line-height: 1.6; margin-bottom: 28px; }
        .btn {
            display: inline-block;
            background: #1a56db;
            color: #fff;
            text-decoration: none;
            padding: 12px 32px;
            border-radius: 6px;
            font-size: 0.95rem;
            font-weight: 600;
            margin: 6px;
            transition: background 0.2s;
        }
        .btn:hover { background: #1648c0; }
        .btn-secondary {
            background: #fff;
            color: #1a56db;
            border: 2px solid #1a56db;
        }
        .btn-secondary:hover { background: #e8f0fe; }
        .note {
            margin-top: 32px;
            font-size: 0.78rem;
            color: #aaa;
        }
    </style>
</head>
<body>
    <div class="card">
        <span class="badge">Campus Complaint Portal</span>
        <h1>Welcome</h1>
        <p>
            A centralized platform for submitting, tracking, and managing
            campus complaints — from students to administrators.
        </p>

        <!--
            TODO (Team Member 1 – Frontend):
            Replace these placeholder hrefs with the real login/register URLs
            once the authentication servlets are in place.
        -->
        <div>
            <a href="${pageContext.request.contextPath}/auth/login" class="btn">Student Login</a>
            <a href="${pageContext.request.contextPath}/auth/register" class="btn btn-secondary">Register</a>
        </div>

        <p class="note">
            Academic project &mdash; Second-Year Engineering &nbsp;|&nbsp;
            Built with Java Servlets, JSP &amp; MySQL
        </p>
    </div>
</body>
</html>
