package com.campusportal.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link User} and {@link Complaint} model classes.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Constructors set the correct fields.</li>
 *   <li>Getters/setters work correctly.</li>
 *   <li>Helper methods ({@code isAdmin()}, {@code isStudent()}, {@code isClosed()}) behave as expected.</li>
 *   <li>{@code toString()} never exposes passwordHash.</li>
 *   <li>Default status for new complaints is SUBMITTED.</li>
 *   <li>Nullable fields behave correctly.</li>
 * </ul>
 */
@DisplayName("User and Complaint model tests")
class UserComplaintTest {

    // =======================================================
    //  User tests
    // =======================================================

    @Nested
    @DisplayName("User")
    class UserTests {

        @Test
        @DisplayName("No-arg constructor creates an empty object without throwing")
        void noArgConstructor() {
            User u = new User();
            // Nothing set — all reference fields are null, primitives are 0/false
            assertNull(u.getEmail());
            assertNull(u.getFullName());
            assertNull(u.getRole());
            assertNull(u.getIdentifier());
            assertEquals(0, u.getUserId());
            assertFalse(u.isActive());
        }

        @Test
        @DisplayName("New-user constructor sets all fields correctly, active=true by default")
        void newUserConstructor() {
            User u = new User(
                    "alice@campus.test",
                    "$2a$12$hashedvalue",
                    "Alice Fernandes",
                    "STUDENT",
                    "STU-2023-001"
            );
            assertEquals("alice@campus.test",  u.getEmail());
            assertEquals("$2a$12$hashedvalue",  u.getPasswordHash());
            assertEquals("Alice Fernandes",     u.getFullName());
            assertEquals("STUDENT",             u.getRole());
            assertEquals("STU-2023-001",        u.getIdentifier());
            assertTrue(u.isActive(), "New accounts should be active by default.");
        }

        @Test
        @DisplayName("Full constructor sets all nine fields including timestamps")
        void fullConstructor() {
            LocalDateTime now = LocalDateTime.of(2026, 10, 25, 10, 0);
            User u = new User(
                    7,
                    "bob@campus.test",
                    "$2a$12$anotherHash",
                    "Bob Sharma",
                    "STUDENT",
                    "STU-2023-002",
                    now, now, true
            );
            assertEquals(7,                  u.getUserId());
            assertEquals("bob@campus.test",  u.getEmail());
            assertEquals("$2a$12$anotherHash", u.getPasswordHash());
            assertEquals("Bob Sharma",       u.getFullName());
            assertEquals("STUDENT",          u.getRole());
            assertEquals("STU-2023-002",     u.getIdentifier());
            assertEquals(now, u.getCreatedAt());
            assertEquals(now, u.getUpdatedAt());
            assertTrue(u.isActive());
        }

        @Test
        @DisplayName("Admin user has null identifier (no roll number)")
        void adminHasNullIdentifier() {
            // Admin accounts are seeded via SQL with identifier = NULL
            User admin = new User("admin@campus.test", "$2a$12$hash", "Dev Admin", "ADMIN", null);
            assertNull(admin.getIdentifier(),
                    "Admin accounts legitimately have a null identifier (no roll number).");
        }

        // --- isAdmin() and isStudent() ---

        @Test
        @DisplayName("isAdmin() returns true for ADMIN role only")
        void isAdminRole() {
            User admin   = new User("a@c.test", "hash", "Admin",   "ADMIN",   null);
            User student = new User("s@c.test", "hash", "Student", "STUDENT", "S001");
            assertTrue(admin.isAdmin());
            assertFalse(student.isAdmin());
        }

        @Test
        @DisplayName("isStudent() returns true for STUDENT role only")
        void isStudentRole() {
            User admin   = new User("a@c.test", "hash", "Admin",   "ADMIN",   null);
            User student = new User("s@c.test", "hash", "Student", "STUDENT", "S001");
            assertTrue(student.isStudent());
            assertFalse(admin.isStudent());
        }

        @Test
        @DisplayName("isAdmin() returns false when role is null")
        void isAdminReturnsFalseForNull() {
            User u = new User(); // role not set
            assertFalse(u.isAdmin());
            assertFalse(u.isStudent());
        }

        // --- Setters ---

        @Test
        @DisplayName("Setters update fields correctly")
        void settersWork() {
            User u = new User();
            u.setUserId(42);
            u.setEmail("test@campus.test");
            u.setFullName("Test User");
            u.setRole("STUDENT");
            u.setIdentifier("STU-999");
            u.setActive(true);
            LocalDateTime ts = LocalDateTime.now();
            u.setCreatedAt(ts);
            u.setUpdatedAt(ts);

            assertEquals(42,              u.getUserId());
            assertEquals("test@campus.test", u.getEmail());
            assertEquals("Test User",     u.getFullName());
            assertEquals("STUDENT",       u.getRole());
            assertEquals("STU-999",       u.getIdentifier());
            assertTrue(u.isActive());
            assertEquals(ts, u.getCreatedAt());
            assertEquals(ts, u.getUpdatedAt());
        }

        // --- toString() security ---

        @Test
        @DisplayName("toString() does NOT expose the password hash")
        void toStringExcludesPasswordHash() {
            User u = new User("x@c.test", "$2a$12$secrethash", "Test", "STUDENT", "S001");
            String result = u.toString();
            assertFalse(result.contains("secrethash"),
                    "toString() must never include the password hash to prevent log leaks.");
            // Should still contain useful debug info
            assertTrue(result.contains("x@c.test"));
            assertTrue(result.contains("STUDENT"));
        }
    }

    // =======================================================
    //  Complaint tests
    // =======================================================

    @Nested
    @DisplayName("Complaint")
    class ComplaintTests {

        @Test
        @DisplayName("No-arg constructor creates an empty object without throwing")
        void noArgConstructor() {
            Complaint c = new Complaint();
            assertNull(c.getTitle());
            assertNull(c.getCategory());
            assertNull(c.getStatus());
            assertNull(c.getLocation());
            assertNull(c.getAdminRemarks());
            assertNull(c.getAssignedAdminId());
        }

        @Test
        @DisplayName("New-complaint constructor sets status to SUBMITTED by default")
        void newComplaintHasSubmittedStatus() {
            Complaint c = new Complaint(
                    2,
                    "Broken projector",
                    ComplaintCategory.IT,
                    "Projector in Hall B not working.",
                    "Hall B, Block 3"
            );
            assertEquals(ComplaintStatus.SUBMITTED, c.getStatus(),
                    "Every new complaint must start in SUBMITTED status.");
        }

        @Test
        @DisplayName("New-complaint constructor maps all fields correctly")
        void newComplaintConstructorMapsFields() {
            Complaint c = new Complaint(3, "Canteen hygiene issue",
                    ComplaintCategory.SANITATION,
                    "Stale food found.", "Main Canteen");
            assertEquals(3,                           c.getStudentId());
            assertEquals("Canteen hygiene issue",     c.getTitle());
            assertEquals(ComplaintCategory.SANITATION, c.getCategory());
            assertEquals("Stale food found.",         c.getDescription());
            assertEquals("Main Canteen",              c.getLocation());
        }

        @Test
        @DisplayName("Location can be null (optional field per schema)")
        void locationCanBeNull() {
            Complaint c = new Complaint(2, "No title", ComplaintCategory.OTHER, "Desc", null);
            assertNull(c.getLocation(),
                    "location is an optional VARCHAR(200) NULL column in the schema.");
        }

        @Test
        @DisplayName("Full constructor sets all twelve fields")
        void fullConstructor() {
            LocalDateTime now = LocalDateTime.of(2026, 10, 25, 12, 0);
            Complaint c = new Complaint(
                    10, 2, "Leaking roof",
                    ComplaintCategory.INFRASTRUCTURE, "Water leaks near library.", "Library",
                    ComplaintStatus.UNDER_REVIEW, 1, "Being investigated.", now, now, null
            );
            assertEquals(10,                               c.getComplaintId());
            assertEquals(2,                                c.getStudentId());
            assertEquals("Leaking roof",                   c.getTitle());
            assertEquals(ComplaintCategory.INFRASTRUCTURE, c.getCategory());
            assertEquals("Water leaks near library.",      c.getDescription());
            assertEquals("Library",                        c.getLocation());
            assertEquals(ComplaintStatus.UNDER_REVIEW,     c.getStatus());
            assertEquals(1,                                c.getAssignedAdminId());
            assertEquals("Being investigated.",            c.getAdminRemarks());
            assertEquals(now,                              c.getSubmittedAt());
            assertEquals(now,                              c.getUpdatedAt());
            assertNull(c.getResolvedAt());
        }

        @Test
        @DisplayName("assignedAdminId can be null (unassigned complaint)")
        void unassignedComplaintHasNullAdminId() {
            // New complaints have no assigned admin (schema: assigned_admin_id INT UNSIGNED NULL)
            Complaint c = new Complaint(2, "Title", ComplaintCategory.IT, "Desc", null);
            assertNull(c.getAssignedAdminId(),
                    "A complaint has no admin assigned until one self-assigns.");
        }

        // --- isClosed() ---

        @Test
        @DisplayName("isClosed() returns false for open statuses")
        void isClosedFalseForOpenStatuses() {
            Complaint c = new Complaint();
            for (ComplaintStatus status : new ComplaintStatus[]{
                    ComplaintStatus.SUBMITTED,
                    ComplaintStatus.UNDER_REVIEW,
                    ComplaintStatus.IN_PROGRESS
            }) {
                c.setStatus(status);
                assertFalse(c.isClosed(), status + " should be considered an open complaint.");
            }
        }

        @Test
        @DisplayName("isClosed() returns true for RESOLVED")
        void isClosedTrueForResolved() {
            Complaint c = new Complaint();
            c.setStatus(ComplaintStatus.RESOLVED);
            assertTrue(c.isClosed());
        }

        @Test
        @DisplayName("isClosed() returns true for REJECTED")
        void isClosedTrueForRejected() {
            Complaint c = new Complaint();
            c.setStatus(ComplaintStatus.REJECTED);
            assertTrue(c.isClosed());
        }

        // --- Setters ---

        @Test
        @DisplayName("Setters update all fields correctly")
        void settersWork() {
            LocalDateTime ts = LocalDateTime.of(2026, 10, 25, 9, 0);
            Complaint c = new Complaint();
            c.setComplaintId(5);
            c.setStudentId(2);
            c.setTitle("Wi-Fi down");
            c.setCategory(ComplaintCategory.IT);
            c.setDescription("No internet in Lab 3.");
            c.setLocation("Lab 3, Block B");
            c.setStatus(ComplaintStatus.IN_PROGRESS);
            c.setAssignedAdminId(1);
            c.setAdminRemarks("Working on it.");
            c.setSubmittedAt(ts);
            c.setUpdatedAt(ts);
            c.setResolvedAt(null);

            assertEquals(5,                       c.getComplaintId());
            assertEquals(2,                       c.getStudentId());
            assertEquals("Wi-Fi down",            c.getTitle());
            assertEquals(ComplaintCategory.IT,    c.getCategory());
            assertEquals("No internet in Lab 3.", c.getDescription());
            assertEquals("Lab 3, Block B",        c.getLocation());
            assertEquals(ComplaintStatus.IN_PROGRESS, c.getStatus());
            assertEquals(1,                       c.getAssignedAdminId());
            assertEquals("Working on it.",        c.getAdminRemarks());
            assertEquals(ts, c.getSubmittedAt());
            assertNull(c.getResolvedAt());
        }

        // --- toString() ---

        @Test
        @DisplayName("toString() contains id, studentId, category, status, title")
        void toStringContainsUsefulInfo() {
            Complaint c = new Complaint(1, 2, "Bad Wi-Fi",
                    ComplaintCategory.IT, "Slow internet.", "Lab",
                    ComplaintStatus.SUBMITTED, null, null,
                    null, null, null);
            String result = c.toString();
            assertTrue(result.contains("1"));           // complaintId
            assertTrue(result.contains("2"));           // studentId
            assertTrue(result.contains("IT"));          // category
            assertTrue(result.contains("SUBMITTED"));   // status
            assertTrue(result.contains("Bad Wi-Fi"));   // title
        }
    }
}
