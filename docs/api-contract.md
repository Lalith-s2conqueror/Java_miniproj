# Campus Complaint Portal – API / URL Contract

**Version:** 1.0 (Locked)
**Status:** ✅ All decisions finalised — 2026-10-25
**Date:** 2026-10-25

---

## Overview

This document defines the URL routes, HTTP methods, expected request parameters,
and response behaviours for the Campus Complaint Portal's server-side layer.

All routes are handled by Java Servlets. JSP pages are views only — they must
not contain business logic or direct JDBC calls. All JSP files live under
`WEB-INF/views/` and are never accessible directly by URL.

**Base context path:** `/campus-complaint-portal` (as configured in Tomcat)

All paths below are relative to this context root.

---

## 1. Authentication Routes

Handled by: **Team Member 2 (Auth)**

### POST `/auth/login`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.LoginServlet` |
| Method | POST |
| Request params | `email` (string, required), `password` (string, required) |
| On success – STUDENT | Redirect to `/student/dashboard`; session contains `userId`, `userRole`, `userName` |
| On success – ADMIN | Redirect to `/admin/dashboard`; same session attributes |
| On failure | Forward to `/WEB-INF/views/auth/login.jsp` with `errorMessage` attribute |
| Notes | Passwords are compared against bcrypt hash (work factor 12) using `PasswordUtil.checkPassword()`. Raw password must NEVER be logged or stored. |

### GET `/auth/login`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.LoginServlet` (doGet) |
| Method | GET |
| Action | Forward to `/WEB-INF/views/auth/login.jsp` |
| Optional query param | `reason=session_expired` — display a session-expiry notice |
| Optional query param | `success=registered` — display registration success message |

### POST `/auth/register`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.RegisterServlet` |
| Method | POST |
| Request params | `fullName` (required), `email` (required), `password` (required), `confirmPassword` (required), `rollNumber` (required — student field, maps to `identifier` column) |
| On success | Redirect to `/auth/login?success=registered` |
| On failure | Forward to `/WEB-INF/views/auth/register.jsp` with `errorMessage` |
| Notes | `rollNumber` is mandatory. Registrations always create a `STUDENT` role account. Admin accounts are provisioned via SQL only (no public registration path). |

### GET `/auth/register`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.RegisterServlet` (doGet) |
| Method | GET |
| Action | Forward to `/WEB-INF/views/auth/register.jsp` |

### GET `/auth/logout`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.LogoutServlet` |
| Method | GET |
| Action | Invalidate `HttpSession` and redirect to `/auth/login` |

---

## 2. Student Routes

Handled by: **Team Member 3 (Complaint / JDBC)**
Protected by: `AuthFilter` – requires valid session with any role

### GET `/student/dashboard`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.StudentDashboardServlet` |
| Method | GET |
| Session check | `userId` must be present in session (enforced by AuthFilter) |
| Request attributes set | `complaints` (List of complaint objects for this student), `stats` (summary counts: total, resolved, pending) |
| Forward to | `/WEB-INF/views/student/dashboard.jsp` |

### GET `/student/complaints/new`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.ComplaintServlet` (doGet, no `id` param) |
| Method | GET |
| Action | Forward to `/WEB-INF/views/student/submit-complaint.jsp` |

### POST `/student/complaints/submit`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.ComplaintServlet` (doPost) |
| Method | POST |
| Request params | `title` (string, required), `category` (enum, required), `description` (text, required), `location` (string, optional) |
| On success | Redirect to `/student/dashboard` with `successMessage` attribute showing complaint ID |
| On failure | Forward to `/WEB-INF/views/student/submit-complaint.jsp` with `errorMessage` |
| Notes | `student_id` must be read from the session `userId` attribute — NEVER from a hidden form field. |

### GET `/student/complaints/view`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.ComplaintServlet` (doGet, `?id=<complaintId>`) |
| Method | GET |
| Query param | `id` (int, required) — the complaint ID |
| Security check | Servlet must verify `complaint.student_id == session.userId` before sending data |
| On success | Forward to `/WEB-INF/views/student/complaint-detail.jsp` |
| On unauthorised | Redirect to `/student/dashboard` with `errorMessage` = "Access denied" |

---

## 3. Admin Routes

Handled by: **Team Member 4 (Admin Dashboard)**
Protected by: `AuthFilter` – requires valid session with `userRole == "ADMIN"`

### GET `/admin/dashboard`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.AdminDashboardServlet` |
| Method | GET |
| Request attributes set | `complaints` (all complaints, filtered/sorted), `stats` (aggregate counts by status and category) |
| Forward to | `/WEB-INF/views/admin/dashboard.jsp` |
| Query params (optional) | `status`, `category`, `search` — for filtering |

### GET `/admin/complaints/view`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.AdminComplaintServlet` (doGet) |
| Method | GET |
| Query param | `id` (int, required) |
| Action | Load complaint and forward to `/WEB-INF/views/admin/complaint-detail.jsp` |

### POST `/admin/complaints/update`

| Item | Detail |
|------|--------|
| Status | ✅ Agreed |
| Handler class | `com.campusportal.servlet.AdminComplaintServlet` (doPost) |
| Method | POST |
| Request params | `complaintId` (int), `newStatus` (enum), `adminRemarks` (text, optional) |
| On success | Redirect to `/admin/complaints/view?id=<complaintId>` with `successMessage` |
| On failure | Forward back to `/WEB-INF/views/admin/complaint-detail.jsp` with `errorMessage` |
| Notes | Only valid status transitions (see §6) will be accepted. Invalid transitions are rejected with an error. |

---

## 4. Server-Side Filters

Handled by: **Team Member 2 (Auth)**
Registered in: `web.xml` (NOT via `@WebFilter`) for deterministic ordering.

### `AuthFilter` → `com.campusportal.filter.AuthFilter`

| Item | Detail |
|------|--------|
| URL patterns | `/student/*`, `/admin/*` |
| Logic | If no valid session (`userId` absent) → redirect to `/auth/login?reason=session_expired` |
| Additional logic for `/admin/*` | If `userRole != "ADMIN"` → redirect to `/student/dashboard` with `errorMessage` = "Access denied" |

---

## 5. Complaint Categories (Enum)

✅ Finalised — `com.campusportal.model.ComplaintCategory`

| Value | Display Label |
|-------|--------------|
| `INFRASTRUCTURE` | Infrastructure & Facilities |
| `SANITATION` | Sanitation & Cleanliness |
| `ACADEMICS` | Academics & Classroom Issues |
| `IT` | IT & Technology |
| `OTHER` | Other |

---

## 6. Complaint Status Workflow

✅ Finalised — `com.campusportal.model.ComplaintStatus`

Valid transitions (all others must be rejected by `ComplaintStatus.isValidTransition()`):

| From | To | Notes |
|------|----|-------|
| `SUBMITTED` | `UNDER_REVIEW` | Admin begins review |
| `UNDER_REVIEW` | `IN_PROGRESS` | Admin starts working |
| `UNDER_REVIEW` | `REJECTED` | Admin rejects |
| `IN_PROGRESS` | `RESOLVED` | Admin resolves |
| `IN_PROGRESS` | `REJECTED` | Admin rejects in progress |
| `RESOLVED` | `UNDER_REVIEW` | Admin **reopens** a resolved complaint |
| `REJECTED` | *(none)* | Terminal state — no further transitions permitted |

When the new status is `RESOLVED` or `REJECTED`, the servlet must also set `resolved_at` to the current timestamp.

---

## 7. Session Attributes (Contract)

These attributes are set by `LoginServlet` and read by all servlets, filters, and JSPs.
**Changing these names requires updating every consumer.**

| Attribute Key | Java Type | Set by | Description |
|---------------|-----------|--------|-------------|
| `userId` | `Integer` | `LoginServlet` | Primary key of the logged-in user |
| `userRole` | `String` | `LoginServlet` | `"STUDENT"` or `"ADMIN"` |
| `userName` | `String` | `LoginServlet` | Full name for display |

---

## 8. Decision Log

All items from the original "Team Approval Required" list have been resolved:

| # | Topic | Decision | Date |
|---|-------|----------|------|
| 1 | `identifier` (roll number) mandatory? | **Yes — required at student registration** | 2026-10-25 |
| 2 | Admin provisioning | **SQL-only INSERT; no public registration path** | 2026-10-25 |
| 3 | Complaint categories | **INFRASTRUCTURE, SANITATION, ACADEMICS, IT, OTHER** | 2026-10-25 |
| 4 | Valid status transitions | **Defined in §6 above** | 2026-10-25 |
| 5 | JSP path naming | **All JSPs under `/WEB-INF/views/`; servlet route naming confirmed** | 2026-10-25 |
