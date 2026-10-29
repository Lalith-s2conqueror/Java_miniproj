-- =============================================================
--  Campus Complaint Portal – Sample / Seed Data
--  File: database/sample-data.sql
--
--  PURPOSE: Development and manual testing ONLY.
--  WARNING: Do NOT run this script in a production database.
--
--  HOW TO USE:
--  1. First run database/schema.sql on a clean database.
--  2. Then run this file.
--  3. Use the fictional accounts below for local testing.
--
--  PASSWORD POLICY:
--  Real password hashes cannot be inserted here at initialisation time
--  because the bcrypt hash depends on a random salt generated at runtime.
--
--  PLACEHOLDER APPROACH (safe for now):
--  The password_hash column below is set to a clearly non-functional
--  sentinel string 'PLACEHOLDER_HASH_NOT_USABLE'. Before this seed data
--  can be used to log in, Team Member 2 (Auth) must either:
--    (a) Generate real bcrypt hashes for the test passwords and replace
--        the sentinel values here, OR
--    (b) Provide a separate seeding script that invokes the application's
--        hash utility to generate and insert proper hashes.
--
--  FICTIONAL DATA: All names, emails, and IDs below are invented for
--  development. They are not real students or staff.
-- =============================================================

USE campus_complaint_portal;

-- =====================
--  Sample Users
-- =====================
--
--  BCRYPT HASH GENERATION (REQUIRED BEFORE THIS SEED DATA IS USABLE):
--
--  The password_hash column below contains a sentinel string.
--  To make these accounts usable for local testing, generate real bcrypt hashes:
--
--    Java (using PasswordUtil):
--      String hash = BCrypt.hashpw("DevPass@123", BCrypt.gensalt(12));
--
--  Then replace the sentinel values below with the generated hashes.
--  NEVER commit real hashes of production passwords. Use fictional test passwords only.
--  NEVER commit real database credentials to version control.
--
--  ADMIN ACCOUNT NOTE:
--  Admin accounts are provisioned exclusively via SQL INSERT.
--  There is no public registration endpoint for admins.

INSERT INTO users (email, password_hash, full_name, role, identifier) VALUES
-- Admin account (identifier NULL – no roll number for staff)
('admin.dev@campusportal.test',
 'PLACEHOLDER_HASH_NOT_USABLE',
 'Dev Admin',
 'ADMIN',
 NULL),

-- Student accounts (identifier = roll number – required for all students)
('alice.student@campusportal.test',
 'PLACEHOLDER_HASH_NOT_USABLE',
 'Alice Fernandes',
 'STUDENT',
 'STU-2023-001'),

('bob.student@campusportal.test',
 'PLACEHOLDER_HASH_NOT_USABLE',
 'Bob Sharma',
 'STUDENT',
 'STU-2023-002');

-- =====================
--  Sample Complaints
-- =====================
-- NOTE: student_id values must match the AUTO_INCREMENT IDs assigned above.
--       Adjust if you insert additional users before these rows.
--       user_id 1 = admin, 2 = alice, 3 = bob (typical first-run sequence).

INSERT INTO complaints (student_id, title, category, description, location, status) VALUES
(2,
 'Broken projector in Seminar Hall B',
 'IT',
 'The ceiling projector in Seminar Hall B has been non-functional for two weeks. Lectures are affected.',
 'Seminar Hall B, Block 3',
 'SUBMITTED'),

(2,
 'Water leakage near Library entrance',
 'INFRASTRUCTURE',
 'During rain, water seeps through the ceiling near the main library entrance and creates a slipping hazard.',
 'Central Library, Ground Floor',
 'UNDER_REVIEW'),

(3,
 'Canteen serving stale food items',
 'SANITATION',
 'Multiple students reported stomach issues after eating at the main canteen on 2026-10-20. Health concern.',
 'Main Canteen, Block 1',
 'SUBMITTED');

-- =============================================================
--  Team TODO before sprint 1:
--  [ ] Member 2 (Auth): Replace PLACEHOLDER_HASH_NOT_USABLE with real
--      bcrypt hashes (work factor 12) for dev test passwords.
--      Document the test credentials in a PRIVATE location (NOT in this
--      file or any other committed file).
--  [ ] Member 4 (Admin): Verify admin dashboard correctly lists
--      complaints above and can update their status.
-- =============================================================
