<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard – Campus Complaint Portal</title>
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
            position: sticky;
            top: 0;
            z-index: 100;
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
        .user-name { font-weight: 600; color: #374151; }
        .btn-logout {
            color: #dc2626;
            text-decoration: none;
            font-weight: 500;
            padding: 6px 12px;
            border-radius: 6px;
            transition: background 0.2s;
        }
        .btn-logout:hover { background: #fee2e2; }

        .container {
            max-width: 1100px;
            margin: 32px auto;
            padding: 0 20px;
        }
        .page-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;
        }
        .page-header h1 { font-size: 1.6rem; font-weight: 700; color: #111827; }
        .page-header p { color: #6b7280; font-size: 0.9rem; margin-top: 4px; }

        .btn-primary {
            display: inline-block;
            background: #1a56db;
            color: #ffffff;
            text-decoration: none;
            padding: 10px 20px;
            border-radius: 6px;
            font-size: 0.9rem;
            font-weight: 600;
            transition: background 0.2s;
            border: none;
            cursor: pointer;
        }
        .btn-primary:hover { background: #1648c0; }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-bottom: 28px;
        }
        .stat-card {
            background: #ffffff;
            border-radius: 8px;
            padding: 20px 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
            border-left: 4px solid #1a56db;
        }
        .stat-card.stat-pending { border-left-color: #f59e0b; }
        .stat-card.stat-resolved { border-left-color: #10b981; }
        .stat-title { font-size: 0.85rem; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; }
        .stat-value { font-size: 1.8rem; font-weight: 700; color: #111827; margin-top: 6px; }

        .alert {
            padding: 12px 16px;
            border-radius: 6px;
            font-size: 0.9rem;
            margin-bottom: 20px;
            line-height: 1.4;
        }
        .alert-danger { background: #fde8e8; color: #9b1c1c; border: 1px solid #f8b4b4; }
        .alert-success { background: #def7ec; color: #03543f; border: 1px solid #84e1bc; }

        .card {
            background: #ffffff;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
            overflow: hidden;
        }
        .card-header {
            padding: 18px 24px;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .card-title { font-size: 1.1rem; font-weight: 600; color: #111827; }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
            font-size: 0.9rem;
        }
        thead th {
            background: #f9fafb;
            color: #4b5563;
            font-weight: 600;
            padding: 12px 20px;
            border-bottom: 1px solid #e5e7eb;
            text-transform: uppercase;
            font-size: 0.75rem;
            letter-spacing: 0.05em;
        }
        tbody td {
            padding: 14px 20px;
            border-bottom: 1px solid #f3f4f6;
            vertical-align: middle;
        }
        tbody tr:hover { background: #f9fafb; }

        .badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        .badge-SUBMITTED { background: #fef3c7; color: #92400e; }
        .badge-UNDER_REVIEW { background: #e0e7ff; color: #3730a3; }
        .badge-IN_PROGRESS { background: #e0f2fe; color: #075985; }
        .badge-RESOLVED { background: #def7ec; color: #03543f; }
        .badge-REJECTED { background: #fde8e8; color: #9b1c1c; }

        .category-tag {
            font-size: 0.8rem;
            color: #4b5563;
            background: #f3f4f6;
            padding: 2px 8px;
            border-radius: 4px;
            font-weight: 500;
        }
        .btn-action {
            color: #1a56db;
            text-decoration: none;
            font-weight: 600;
            font-size: 0.85rem;
        }
        .btn-action:hover { text-decoration: underline; }

        .empty-state {
            padding: 48px 24px;
            text-align: center;
        }
        .empty-state h3 { font-size: 1.15rem; color: #374151; margin-bottom: 8px; font-weight: 600; }
        .empty-state p { color: #6b7280; font-size: 0.9rem; margin-bottom: 20px; }
    </style>
</head>
<body>
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/student/dashboard" class="navbar-brand">Campus Complaint Portal</a>
        <div class="navbar-user">
            <span class="user-name"><c:out value="${sessionScope.userName != null ? sessionScope.userName : 'Student'}" /></span>
            <a href="${pageContext.request.contextPath}/auth/logout" class="btn-logout">Logout</a>
        </div>
    </header>

    <main class="container">
        <div class="page-header">
            <div>
                <h1>Student Dashboard</h1>
                <p>Track your submitted complaints and check resolution progress</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/student/complaints/new" class="btn-primary">+ New Complaint</a>
            </div>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success">
                <c:out value="${successMessage}" />
            </div>
        </c:if>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-title">Total Submitted</div>
                <div class="stat-value"><c:out value="${stats.total != null ? stats.total : 0}" /></div>
            </div>
            <div class="stat-card stat-pending">
                <div class="stat-title">Pending Resolution</div>
                <div class="stat-value"><c:out value="${stats.pending != null ? stats.pending : 0}" /></div>
            </div>
            <div class="stat-card stat-resolved">
                <div class="stat-title">Resolved</div>
                <div class="stat-value"><c:out value="${stats.resolved != null ? stats.resolved : 0}" /></div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <h2 class="card-title">My Complaints</h2>
            </div>

            <c:choose>
                <c:when test="${not empty complaints}">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 80px;">ID</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Status</th>
                                <th>Submitted Date</th>
                                <th style="text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${complaints}">
                                <tr>
                                    <td><strong>#<c:out value="${c.complaintId}" /></strong></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/student/complaints/view?id=${c.complaintId}" class="btn-action">
                                            <c:out value="${c.title}" />
                                        </a>
                                    </td>
                                    <td>
                                        <span class="category-tag"><c:out value="${c.category.displayLabel}" /></span>
                                    </td>
                                    <td>
                                        <span class="badge badge-${c.status.name()}">
                                            <c:out value="${c.status.name()}" />
                                        </span>
                                    </td>
                                    <td><c:out value="${c.submittedAt}" /></td>
                                    <td style="text-align: right;">
                                        <a href="${pageContext.request.contextPath}/student/complaints/view?id=${c.complaintId}" class="btn-action">
                                            View Details &rarr;
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        <h3>No Complaints Found</h3>
                        <p>You haven't filed any complaints yet. Need assistance with an issue on campus?</p>
                        <a href="${pageContext.request.contextPath}/student/complaints/new" class="btn-primary">Submit a Complaint</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</body>
</html>
