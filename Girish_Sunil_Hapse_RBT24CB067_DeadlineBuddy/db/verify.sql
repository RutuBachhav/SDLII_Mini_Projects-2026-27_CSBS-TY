-- ---------------------------------------------------------------------------
-- DeadlineBuddy - check that this database matches the one the project was
-- built and tested against.
--
--   mysql -u root -p < db/verify.sql
--
-- Run it after schema.sql and seed.sql. Every row it prints ends in OK. If any
-- row says MISMATCH, the database is not in the expected state - re-run
-- schema.sql then seed.sql and try again.
--
-- Note on dates: the seed writes due dates relative to CURDATE(), so the
-- absolute values in due_date differ from one install to the next, on purpose.
-- What is identical is the shape: the same number of overdue, due-today,
-- upcoming and completed tasks, on whatever day you install it. That is what
-- this script checks, and it is what makes the demo look the same for everyone.
-- ---------------------------------------------------------------------------

USE deadlinebuddy_db;

SELECT '--- structure ---' AS '';

SELECT 'tables' AS checked, COUNT(*) AS found, 3 AS expected,
       IF(COUNT(*) = 3, 'OK', 'MISMATCH') AS result
FROM information_schema.tables
WHERE table_schema = 'deadlinebuddy_db'
  AND table_name IN ('users', 'subjects', 'tasks');

SELECT 'foreign keys' AS checked, COUNT(*) AS found, 3 AS expected,
       IF(COUNT(*) = 3, 'OK', 'MISMATCH') AS result
FROM information_schema.referential_constraints
WHERE constraint_schema = 'deadlinebuddy_db';

SELECT 'check constraints' AS checked, COUNT(*) AS found, 5 AS expected,
       IF(COUNT(*) = 5, 'OK', 'MISMATCH') AS result
FROM information_schema.check_constraints
WHERE constraint_schema = 'deadlinebuddy_db'
  AND constraint_name IN ('ck_subjects_color', 'ck_tasks_type', 'ck_tasks_priority',
                          'ck_tasks_status', 'ck_tasks_completed_at');

SELECT 'composite FK (subject_id, user_id)' AS checked,
       COUNT(*) AS found, 2 AS expected,
       IF(COUNT(*) = 2, 'OK', 'MISMATCH') AS result
FROM information_schema.key_column_usage
WHERE constraint_schema = 'deadlinebuddy_db'
  AND constraint_name = 'fk_tasks_subject_owner';

SELECT '--- row counts ---' AS '';

SELECT 'users'    AS checked, COUNT(*) AS found,  2 AS expected, IF(COUNT(*) =  2,'OK','MISMATCH') AS result FROM users
UNION ALL
SELECT 'subjects',            COUNT(*),           7,             IF(COUNT(*) =  7,'OK','MISMATCH')           FROM subjects
UNION ALL
SELECT 'tasks',               COUNT(*),          20,             IF(COUNT(*) = 20,'OK','MISMATCH')           FROM tasks;

-- A fresh seed gives exactly 2 users, 7 subjects and 20 tasks. Higher numbers
-- just mean somebody has been using the application since - re-run schema.sql
-- and seed.sql to get back to the starting state.

SELECT '--- the demo account: day-invariant figures ---' AS '';

-- These do not depend on what day it is, so they are hard checks.
SELECT u.email,
       COUNT(t.id)                                           AS total,
       SUM(t.status = 'COMPLETED')                           AS completed,
       SUM(t.status = 'IN_PROGRESS')                         AS in_progress,
       SUM(t.status = 'PENDING')                             AS pending,
       ROUND(SUM(t.status = 'COMPLETED') / COUNT(t.id) * 100) AS pct,
       IF((u.email = 'ananya.pawar@student.edu'
             AND COUNT(t.id) = 16 AND SUM(t.status='COMPLETED') = 6
             AND SUM(t.status='IN_PROGRESS') = 2 AND SUM(t.status='PENDING') = 8)
          OR (u.email = 'rohan.mehta@student.edu'
             AND COUNT(t.id) = 4 AND SUM(t.status='COMPLETED') = 1
             AND SUM(t.status='IN_PROGRESS') = 1 AND SUM(t.status='PENDING') = 2),
          'OK', 'MISMATCH') AS result
FROM users u
JOIN tasks t ON t.user_id = u.id
GROUP BY u.id, u.email
ORDER BY u.id;

-- Expected: ananya 16 / 6 / 2 / 8 / 38,  rohan 4 / 1 / 1 / 2 / 25.

SELECT '--- the deadline split: depends on TODAY ---' AS '';

-- The seed writes due dates relative to CURDATE() at the moment you run it, so
-- this split is only "2 overdue, 2 due today" on the day you seeded. Seed on
-- Monday and demo on Wednesday and those two tasks will have become overdue -
-- which is the application working correctly, not a fault.
--
-- The fix is to re-run seed.sql on the day you demo. It is safe to re-run.
SELECT u.email,
       SUM(t.status <> 'COMPLETED' AND t.due_date <  CURDATE()) AS overdue,
       SUM(t.status <> 'COMPLETED' AND t.due_date =  CURDATE()) AS due_today,
       SUM(t.status <> 'COMPLETED' AND t.due_date >  CURDATE()) AS upcoming,
       CASE
         WHEN u.email <> 'ananya.pawar@student.edu' THEN '-'
         WHEN SUM(t.status <> 'COMPLETED' AND t.due_date < CURDATE()) = 2
          AND SUM(t.status <> 'COMPLETED' AND t.due_date = CURDATE()) = 2
              THEN 'as seeded (2 overdue, 2 due today)'
         ELSE 'seeded on an earlier day - re-run seed.sql before demoing'
       END AS note
FROM users u
JOIN tasks t ON t.user_id = u.id
GROUP BY u.id, u.email
ORDER BY u.id;

SELECT '--- passwords are hashed ---' AS '';

SELECT 'all passwords are PBKDF2' AS checked,
       SUM(password_hash LIKE 'pbkdf2\_sha256$%') AS found,
       COUNT(*) AS expected,
       IF(SUM(password_hash LIKE 'pbkdf2\_sha256$%') = COUNT(*), 'OK', 'MISMATCH') AS result
FROM users;

SELECT '--- integrity invariants (both must be 0) ---' AS '';

SELECT 'orphaned tasks' AS checked, COUNT(*) AS found, 0 AS expected,
       IF(COUNT(*) = 0, 'OK', 'MISMATCH') AS result
FROM tasks t LEFT JOIN subjects s ON s.id = t.subject_id
WHERE s.id IS NULL;

SELECT 'tasks under another user''s subject' AS checked, COUNT(*) AS found, 0 AS expected,
       IF(COUNT(*) = 0, 'OK', 'MISMATCH') AS result
FROM tasks t JOIN subjects s ON s.id = t.subject_id
WHERE t.user_id <> s.user_id;

SELECT 'completed_at matches status' AS checked, COUNT(*) AS found, 0 AS expected,
       IF(COUNT(*) = 0, 'OK', 'MISMATCH') AS result
FROM tasks
WHERE (status = 'COMPLETED' AND completed_at IS NULL)
   OR (status <> 'COMPLETED' AND completed_at IS NOT NULL);
