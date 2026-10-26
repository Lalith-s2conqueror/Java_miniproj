<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Submit Complaint – Campus Complaint Portal</title>
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #f4f6f9;
            color: #2d2d2d;
            min-height: 100vh;
        }
        .navbar {
            background: #ffffff;
            border-bottom: 1px solid #e5e7eb;
            padding: 14px 28px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .navbar-brand {
            font-size: 1.25rem;
            font-weight: 700;
            color: #1a56db;
            text-decoration: none;
        }
        .navbar-user {
            display: flex;
            align-items: center;
            gap: 16px;
            font-size: 0.9rem;
        }
        .btn-logout {
            color: #dc2626;
            text-decoration: none;
            font-weight: 500;
            padding: 6px 12px;
            border-radius: 6px;
        }
        .btn-logout:hover { background: #fee2e2; }

        .container {
            max-width: 720px;
            margin: 36px auto;
            padding: 0 20px;
        }
        .breadcrumb {
            margin-bottom: 18px;
            font-size: 0.85rem;
            color: #6b7280;
        }
        .breadcrumb a {
            color: #1a56db;
            text-decoration: none;
            font-weight: 500;
        }
        .breadcrumb a:hover { text-decoration: underline; }

        .card {
            background: #ffffff;
            border-radius: 10px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            padding: 36px;
        }
        .card-header {
            margin-bottom: 24px;
        }
        .card-header h1 {
            font-size: 1.5rem;
            font-weight: 700;
            color: #111827;
            margin-bottom: 6px;
        }
        .card-header p {
            color: #6b7280;
            font-size: 0.9rem;
        }

        .alert {
            padding: 12px 16px;
            border-radius: 6px;
            font-size: 0.9rem;
            margin-bottom: 22px;
            line-height: 1.4;
        }
        .alert-danger { background: #fde8e8; color: #9b1c1c; border: 1px solid #f8b4b4; }

        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            font-size: 0.88rem;
            font-weight: 600;
            margin-bottom: 6px;
            color: #374151;
        }
        .required { color: #dc2626; margin-left: 2px; }
        .help-text {
            font-size: 0.78rem;
            color: #6b7280;
            margin-top: 4px;
        }

        input[type="text"], select, textarea {
            width: 100%;
            padding: 10px 14px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
            font-size: 0.95rem;
            font-family: inherit;
            color: #1f2937;
            transition: border-color 0.2s, box-shadow 0.2s;
            background: #ffffff;
        }
        input[type="text"]:focus, select:focus, textarea:focus {
            outline: none;
            border-color: #1a56db;
            box-shadow: 0 0 0 3px rgba(26, 86, 219, 0.15);
        }
        textarea {
            resize: vertical;
            min-height: 120px;
        }

        .form-actions {
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 12px;
            margin-top: 28px;
            padding-top: 20px;
            border-top: 1px solid #e5e7eb;
        }
        .btn {
            padding: 10px 22px;
            border-radius: 6px;
            font-size: 0.95rem;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s;
            text-decoration: none;
            display: inline-block;
        }
        .btn-primary {
            background: #1a56db;
            color: #ffffff;
            border: none;
        }
        .btn-primary:hover { background: #1648c0; }
        .btn-secondary {
            background: #ffffff;
            color: #4b5563;
            border: 1px solid #d1d5db;
        }
        .btn-secondary:hover { background: #f3f4f6; }
    </style>
</head>
<body>
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/student/dashboard" class="navbar-brand">Campus Complaint Portal</a>
        <div class="navbar-user">
            <span><c:out value="${sessionScope.userName != null ? sessionScope.userName : 'Student'}" /></span>
            <a href="${pageContext.request.contextPath}/auth/logout" class="btn-logout">Logout</a>
        </div>
    </header>

    <main class="container">
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/student/dashboard">&larr; Back to Dashboard</a>
        </div>

        <div class="card">
            <div class="card-header">
                <h1>Submit a New Complaint</h1>
                <p>Provide detailed information so the campus administrators can resolve the issue promptly.</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">
                    <c:out value="${errorMessage}" />
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/student/complaints/submit" method="POST">
                <%-- Security: student identity is derived exclusively from server-side session. NO hidden student_id field! --%>

                <div class="form-group">
                    <label for="title">Complaint Title <span class="required">*</span></label>
                    <input type="text" id="title" name="title"
                           value="<c:out value='${title}' />"
                           placeholder="e.g., Broken ceiling fan in Classroom 201"
                           maxlength="200" required>
                    <div class="help-text">Concise summary of the problem (3–200 characters).</div>
                </div>

                <div class="form-group">
                    <label for="category">Category <span class="required">*</span></label>
                    <select id="category" name="category" required>
                        <option value="" disabled ${empty category ? 'selected' : ''}>-- Select a Category --</option>
                        <option value="INFRASTRUCTURE" ${category eq 'INFRASTRUCTURE' ? 'selected' : ''}>Infrastructure &amp; Facilities</option>
                        <option value="SANITATION" ${category eq 'SANITATION' ? 'selected' : ''}>Sanitation &amp; Cleanliness</option>
                        <option value="ACADEMICS" ${category eq 'ACADEMICS' ? 'selected' : ''}>Academics &amp; Classroom Issues</option>
                        <option value="IT" ${category eq 'IT' ? 'selected' : ''}>IT &amp; Technology</option>
                        <option value="OTHER" ${category eq 'OTHER' ? 'selected' : ''}>Other</option>
                    </select>
                    <div class="help-text">Choose the department or facility responsible for handling this matter.</div>
                </div>

                <div class="form-group">
                    <label for="location">Location / Building <span style="font-size: 0.8rem; color: #6b7280; font-weight: normal;">(Optional)</span></label>
                    <input type="text" id="location" name="location"
                           value="<c:out value='${location}' />"
                           placeholder="e.g., Block B, 2nd Floor, Room 201"
                           maxlength="200">
                    <div class="help-text">Specific campus location to assist staff in locating the issue.</div>
                </div>

                <div class="form-group">
                    <label for="description">Detailed Description <span class="required">*</span></label>
                    <textarea id="description" name="description"
                              placeholder="Please describe the issue in detail, including when it was first observed and how it affects campus activities..."
                              maxlength="5000" required><c:out value="${description}" /></textarea>
                    <div class="help-text">Minimum 10 characters, maximum 5000 characters.</div>
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/student/dashboard" class="btn btn-secondary">Cancel</a>
                    <button type="submit" class="btn btn-primary">Submit Complaint</button>
                </div>
            </form>
        </div>
    </main>
</body>
</html>
