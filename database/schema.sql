-- =============================================================
--  Campus Complaint Portal – Database Schema
--  File: database/schema.sql
--  Target RDBMS: MySQL 8.x
--
--  IMPORTANT NOTES FOR TEAM:
--  * Run this script ONCE on a fresh MySQL instance (development or CI).
--  * If you change the schema, write a new numbered migration file
--    (e.g. database/migration_002_add_column.sql) rather than
--    re-running this script on an existing database.
--  * Passwords MUST be stored as bcrypt hashes. NEVER insert or log
--    plaintext passwords. The application layer (Member 2 – Auth)
--    is responsible for hashing before INSERT and comparing during login.
-- =============================================================

-- Use a dedicated database so the portal is self-contained.
CREATE DATABASE IF NOT EXISTS campus_complaint_portal
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_complaint_portal;

-- =============================================================
--  Table: users
--  Stores all portal users. Both students and admins share this
--  table; the `role` column distinguishes them.
-- =============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id        INT UNSIGNED     NOT NULL AUTO_INCREMENT,

    -- Login credentials
    email          VARCHAR(255)     NOT NULL,
    -- Hashed password (bcrypt or similar). NEVER store plaintext here.
    password_hash  VARCHAR(255)     NOT NULL,

    -- Profile information
    full_name      VARCHAR(150)     NOT NULL,

    -- 'STUDENT' or 'ADMIN'
    -- ENUM enforced at DB level; application layer must validate too.
    role           ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',

    -- Roll number (student – required by application layer) or staff ID (admin – optional).
    -- The NULL constraint at DB level accommodates admin rows that have no roll number.
    -- RegisterServlet enforces NOT NULL for STUDENT registrations.
    identifier     VARCHAR(60)      NULL COMMENT 'Roll number (student – required) or staff ID (admin – optional)',

    -- Timestamps
    created_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    -- Soft-delete flag (TRUE = account deactivated, not physically deleted)
    is_active      TINYINT(1)       NOT NULL DEFAULT 1,

    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='All portal users – students and admins';

-- =============================================================
--  Table: complaints
--  One row per complaint submitted by a student.
-- =============================================================
CREATE TABLE IF NOT EXISTS complaints (
    complaint_id   INT UNSIGNED     NOT NULL AUTO_INCREMENT,

    -- The student who filed the complaint
    student_id     INT UNSIGNED     NOT NULL,

    -- Complaint metadata
    title          VARCHAR(200)     NOT NULL,

    -- Category the student selected.
    -- Approved values (closed set enforced at application layer by ComplaintCategory enum):
    --   INFRASTRUCTURE, SANITATION, ACADEMICS, IT, OTHER
    category       VARCHAR(60)      NOT NULL,

    description    TEXT             NOT NULL,
    location       VARCHAR(200)     NULL COMMENT 'Campus location/building, if applicable',

    -- Workflow status
    -- Submitted → Under Review → In Progress → Resolved (or Rejected)
    status         ENUM(
                       'SUBMITTED',
                       'UNDER_REVIEW',
                       'IN_PROGRESS',
                       'RESOLVED',
                       'REJECTED'
                   ) NOT NULL DEFAULT 'SUBMITTED',

    -- Admin who is handling/handled this complaint (NULL until assigned)
    assigned_admin_id  INT UNSIGNED NULL,

    -- Admin remarks or resolution notes (NULL until admin adds them)
    admin_remarks  TEXT             NULL,

    -- Timestamps
    submitted_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    -- If status is RESOLVED or REJECTED, record when it was closed
    resolved_at    DATETIME         NULL,

    PRIMARY KEY (complaint_id),

    -- Referential integrity
    CONSTRAINT fk_complaints_student
        FOREIGN KEY (student_id)
        REFERENCES users (user_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_complaints_admin
        FOREIGN KEY (assigned_admin_id)
        REFERENCES users (user_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    -- Index for common queries
    INDEX idx_complaints_student   (student_id),
    INDEX idx_complaints_status    (status),
    INDEX idx_complaints_category  (category),
    INDEX idx_complaints_submitted (submitted_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Campus complaints filed by students';

-- =============================================================
--  Schema version tracking (optional but recommended)
--  Lets the team see which migrations have been applied.
-- =============================================================
CREATE TABLE IF NOT EXISTS schema_version (
    version_id     INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    version_label  VARCHAR(60)      NOT NULL,
    applied_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes          TEXT             NULL,
    PRIMARY KEY (version_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- Record that this baseline schema was applied
INSERT INTO schema_version (version_label, notes)
VALUES ('001_baseline', 'Initial schema: users and complaints tables');

-- =============================================================
--  Schema Notes (updated 2026-10-25):
--
--  1. Complaint categories are enforced at the application layer via the
--     ComplaintCategory enum: INFRASTRUCTURE, SANITATION, ACADEMICS, IT, OTHER.
--  2. File/image attachments are OUT OF SCOPE for this version.
--  3. Email notifications are OUT OF SCOPE for this version.
--  4. Password hashing algorithm: bcrypt, work factor 12 (jbcrypt 0.4).
--  5. assigned_admin_id is set manually by the admin (self-assign).
-- =============================================================
