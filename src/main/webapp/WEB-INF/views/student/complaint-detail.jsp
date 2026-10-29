<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Complaint #<c:out value="${complaint.complaintId}" /> Details – Campus Complaint Portal</title>
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
            max-width: 860px;
            margin: 32px auto;
            padding: 0 20px;
        }
        .breadcrumb {
            margin-bottom: 20px;
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
            margin-bottom: 24px;
        }
        .complaint-header {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 16px;
            padding-bottom: 20px;
            border-bottom: 1px solid #e5e7eb;
            margin-bottom: 24px;
        }
        .complaint-header h1 {
            font-size: 1.5rem;
            font-weight: 700;
            color: #111827;
            line-height: 1.3;
        }
        .complaint-id {
            color: #6b7280;
            font-size: 0.95rem;
            margin-top: 4px;
        }

        .badge {
            display: inline-block;
            padding: 6px 14px;
            border-radius: 9999px;
            font-size: 0.82rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            white-space: nowrap;
        }
        .badge-SUBMITTED { background: #fef3c7; color: #92400e; }
        .badge-UNDER_REVIEW { background: #e0e7ff; color: #3730a3; }
        .badge-IN_PROGRESS { background: #e0f2fe; color: #075985; }
        .badge-RESOLVED { background: #def7ec; color: #03543f; }
        .badge-REJECTED { background: #fde8e8; color: #9b1c1c; }

        .meta-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 18px;
            margin-bottom: 28px;
            background: #f9fafb;
            padding: 18px 20px;
            border-radius: 8px;
        }
        .meta-item-label {
            font-size: 0.78rem;
            font-weight: 600;
            color: #6b7280;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }
        .meta-item-value {
            font-size: 0.92rem;
            font-weight: 600;
            color: #1f2937;
            margin-top: 4px;
        }

        .section {
            margin-bottom: 26px;
        }
        .section-title {
            font-size: 0.95rem;
            font-weight: 700;
            color: #374151;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 10px;
        }
        .description-box {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 18px;
            font-size: 0.95rem;
            line-height: 1.6;
            color: #374151;
            white-space: pre-wrap;
        }

        .remarks-box {
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            border-radius: 8px;
            padding: 18px;
            color: #166534;
            font-size: 0.95rem;
            line-height: 1.6;
            white-space: pre-wrap;
        }
        .remarks-box.empty {
            background: #f9fafb;
            border-color: #e5e7eb;
            color: #6b7280;
            font-style: italic;
        }

        .tracking-timeline {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 32px;
            padding: 20px 10px;
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
        }
        .timeline-step {
            flex: 1;
            text-align: center;
            position: relative;
        }
        .timeline-step::after {
            content: '';
            position: absolute;
            top: 14px;
            left: 50%;
            width: 100%;
            height: 3px;
            background: #e5e7eb;
            z-index: 1;
        }
        .timeline-step:last-child::after {
            display: none;
        }
        .timeline-step.active::after {
            background: #1a56db;
        }
        .step-indicator {
            width: 30px;
            height: 30px;
            border-radius: 50%;
            background: #e5e7eb;
            color: #4b5563;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 8px auto;
            font-size: 0.8rem;
            font-weight: 700;
            position: relative;
            z-index: 2;
        }
        .timeline-step.active .step-indicator {
            background: #1a56db;
            color: #ffffff;
        }
        .timeline-step.completed .step-indicator {
            background: #10b981;
            color: #ffffff;
        }
        .step-label {
            font-size: 0.78rem;
            font-weight: 600;
            color: #6b7280;
        }
        .timeline-step.active .step-label {
            color: #1a56db;
            font-weight: 700;
        }

        .btn {
            display: inline-block;
            padding: 10px 22px;
            border-radius: 6px;
            font-size: 0.95rem;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: background 0.2s;
        }
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
            <span> / Complaint #<c:out value="${complaint.complaintId}" /></span>
        </div>

        <div class="card">
            <div class="complaint-header">
                <div>
                    <h1><c:out value="${complaint.title}" /></h1>
                    <div class="complaint-id">Complaint ID: #<strong><c:out value="${complaint.complaintId}" /></strong></div>
                </div>
                <div>
                    <span class="badge badge-${complaint.status.name()}">
                        <c:out value="${complaint.status.name()}" />
                    </span>
                </div>
            </div>

            <!-- Workflow Status Progress Bar -->
            <c:set var="statusName" value="${complaint.status.name()}" />
            <div class="tracking-timeline">
                <div class="timeline-step ${statusName eq 'SUBMITTED' ? 'active' : 'completed'}">
                    <div class="step-indicator">&#10003;</div>
                    <div class="step-label">Submitted</div>
                </div>
                <div class="timeline-step ${statusName eq 'UNDER_REVIEW' ? 'active' : (statusName eq 'IN_PROGRESS' || statusName eq 'RESOLVED' ? 'completed' : '')}">
                    <div class="step-indicator">2</div>
                    <div class="step-label">Under Review</div>
                </div>
                <div class="timeline-step ${statusName eq 'IN_PROGRESS' ? 'active' : (statusName eq 'RESOLVED' ? 'completed' : '')}">
                    <div class="step-indicator">3</div>
                    <div class="step-label">In Progress</div>
                </div>
                <div class="timeline-step ${statusName eq 'RESOLVED' ? 'completed' : (statusName eq 'REJECTED' ? 'active' : '')}">
                    <div class="step-indicator">4</div>
                    <div class="step-label"><c:out value="${statusName eq 'REJECTED' ? 'Rejected' : 'Resolved'}" /></div>
                </div>
            </div>

            <div class="meta-grid">
                <div>
                    <div class="meta-item-label">Category</div>
                    <div class="meta-item-value"><c:out value="${complaint.category.displayLabel}" /></div>
                </div>
                <div>
                    <div class="meta-item-label">Location</div>
                    <div class="meta-item-value">
                        <c:out value="${not empty complaint.location ? complaint.location : 'Not specified'}" />
                    </div>
                </div>
                <div>
                    <div class="meta-item-label">Submitted Date</div>
                    <div class="meta-item-value"><c:out value="${complaint.submittedAt}" /></div>
                </div>
                <div>
                    <div class="meta-item-label">Last Updated</div>
                    <div class="meta-item-value"><c:out value="${complaint.updatedAt}" /></div>
                </div>
                <c:if test="${not empty complaint.resolvedAt}">
                    <div>
                        <div class="meta-item-label">Resolved / Closed Date</div>
                        <div class="meta-item-value"><c:out value="${complaint.resolvedAt}" /></div>
                    </div>
                </c:if>
            </div>

            <div class="section">
                <div class="section-title">Description</div>
                <div class="description-box"><c:out value="${complaint.description}" /></div>
            </div>

            <div class="section">
                <div class="section-title">Administrator Remarks &amp; Resolution Notes</div>
                <c:choose>
                    <c:when test="${not empty complaint.adminRemarks}">
                        <div class="remarks-box"><c:out value="${complaint.adminRemarks}" /></div>
                    </c:when>
                    <c:otherwise>
                        <div class="remarks-box empty">No administrative remarks or updates have been added yet.</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div style="margin-top: 32px; padding-top: 20px; border-top: 1px solid #e5e7eb;">
                <a href="${pageContext.request.contextPath}/student/dashboard" class="btn btn-secondary">&larr; Back to Dashboard</a>
            </div>
        </div>
    </main>
</body>
</html>
