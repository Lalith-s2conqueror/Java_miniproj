# Campus Complaint Portal – Requirements Document

**Version:** 0.1 (Draft)
**Status:** Pending team review
**Date:** 2026-10-24
**Authors:** All team members (initial draft by Architect)

---

## 1. Project Overview

The Campus Complaint Portal is a Java-based web application that gives students
a structured, transparent way to submit and track campus complaints, and gives
administrators the tools to manage and resolve them efficiently.

This document describes the planned workflows, functional requirements, and
non-functional constraints that will guide the team through feature development.

---

## 2. Actors

| Actor   | Description |
|---------|-------------|
| Student | A registered campus student who can submit and track their own complaints |
| Admin   | A staff member with elevated access who can view all complaints and update their status |

---

## 3. Student Workflows

### 3.1 Registration

1. Student visits the portal's home page and clicks **Register**.
2. Student fills in: Full Name, Roll Number, Email Address, Password, Confirm Password.
3. System validates that the email is unique and the passwords match.
4. System hashes the password (bcrypt) and stores the user record with `role = STUDENT`.
5. Student is redirected to the login page with a success message.

### 3.2 Login

1. Student enters Email and Password on the login page.
2. System retrieves the record by email and compares the provided password against the stored bcrypt hash.
3. On success: a session is created (`userId`, `role`, `name` stored in `HttpSession`), and the student is redirected to the **Student Dashboard**.
4. On failure: an error message is shown; the password is **never** echoed back.

### 3.3 Student Dashboard

- Displays a summary: total complaints submitted, number resolved, number pending.
- Lists the student's own complaints with their current status.
- Provides a button to submit a new complaint.

### 3.4 Submit a Complaint

1. Student clicks **New Complaint**.
2. Student fills in: Title, Category (dropdown), Description, Location (optional).
3. System validates all required fields.
4. System creates a complaint record with `status = SUBMITTED` and assigns a unique `complaint_id`.
5. Student is shown a confirmation with the complaint ID and redirected to the dashboard.

### 3.5 View Complaint Detail

1. Student clicks on a complaint from the dashboard list.
2. System verifies that `complaint.student_id == session.userId` before rendering.
   - **Security rule:** A student must never see another student's complaint.
3. Detail page shows: title, category, description, status, submitted date, and any admin remarks.

### 3.6 Logout

1. Student clicks **Logout**.
2. System calls `session.invalidate()` and redirects to the login page.

---

## 4. Admin Workflows

### 4.1 Admin Login

- Admins use the same login page as students.
- After password verification, if `role == ADMIN`, the session is created and the admin is redirected to the **Admin Dashboard**.
- Students cannot access admin pages; this must be enforced server-side by a filter.

### 4.2 Admin Dashboard

- Displays an overview: total complaints, complaints by status, complaints by category.
- Lists all complaints (not restricted by student) with filters for:
  - Status: SUBMITTED, UNDER_REVIEW, IN_PROGRESS, RESOLVED, REJECTED
  - Category: INFRASTRUCTURE, SANITATION, ACADEMICS, IT, OTHER
- Supports search by complaint ID or title keyword.

### 4.3 Update Complaint Status

1. Admin clicks on a complaint to open the detail view.
2. Admin selects a new status from a dropdown and optionally adds a remark.
3. System updates the complaint record and records `updated_at`.
4. If status is RESOLVED or REJECTED, system also sets `resolved_at` to the current timestamp.

### 4.4 Assign a Complaint

- Admin can optionally assign a complaint to themselves (`assigned_admin_id = session.userId`), indicating ownership of the resolution.

---

## 5. Complaint Status Workflow

```
                         ┌─────────────┐
                         │             │ (admin reopens)
SUBMITTED → UNDER_REVIEW → IN_PROGRESS → RESOLVED ──────────┐
              │                       ↘                      │
              └──────────────────→ REJECTED (terminal)       │
                                                             │
                    ◄────────────────────────────────────────┘
                             UNDER_REVIEW
```

Valid transitions (all others are rejected by the application):

| From | To | Meaning |
|------|----|---------|
| SUBMITTED | UNDER_REVIEW | Admin begins review |
| UNDER_REVIEW | IN_PROGRESS | Admin starts working |
| UNDER_REVIEW | REJECTED | Admin rejects |
| IN_PROGRESS | RESOLVED | Admin resolves |
| IN_PROGRESS | REJECTED | Admin rejects in-progress |
| RESOLVED | UNDER_REVIEW | Admin **reopens** a closed complaint |
| REJECTED | *(none)* | Terminal — no further transitions |

Status transitions are controlled by the admin only. A student cannot change the
status of their own complaint.

---

## 6. Security Requirements

| Requirement | Detail |
|-------------|--------|
| Password storage | Passwords hashed with bcrypt (work factor 12) using `org.mindrot:jbcrypt:0.4`. Plaintext passwords must never appear in logs, responses, or the database |
| Session isolation | A student session must only access their own data; enforced server-side |
| Admin gate | All `/admin/*` URLs protected by `AuthFilter`; unauthenticated requests redirect to login |
| Student gate | All `/student/*` URLs require an active session; enforced by `AuthFilter` |
| HTTPS | Session cookie must be `Secure` in production deployment |
| Input validation | All user inputs validated and sanitised before database insertion |

---

## 7. Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| Language | Java 17 (bytecode target) |
| Runtime  | Apache Tomcat 10.1+ (Jakarta EE 10 / Servlet 6.0) |
| Database | MySQL 8.x |
| Build    | Apache Maven 3.9+ |
| Browser support | Modern evergreen browsers (Chrome, Firefox, Edge) |
| Responsiveness | Pages should be usable on mobile screens (basic responsive layout) |

---

## 8. Out of Scope (for this academic project)

- Email notifications
- File / image attachments for complaints
- Real-time updates (WebSockets)
- OAuth or SSO login
- Deployment to a public server

---

## 9. Decisions Log (all open questions resolved — 2026-10-25)

| # | Topic | Decision |
|---|-------|----------|
| 1 | Complaint categories | **INFRASTRUCTURE, SANITATION, ACADEMICS, IT, OTHER** |
| 2 | Admin provisioning | **SQL INSERT only — no public registration path for admins** |
| 3 | Password reset | **Out of scope for this version** |
| 4 | Identifier / roll number | **Mandatory at student registration** |
| 5 | File attachments | **Out of scope for this version** |
