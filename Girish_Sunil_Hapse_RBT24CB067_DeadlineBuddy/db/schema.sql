-- ---------------------------------------------------------------------------
-- DeadlineBuddy - schema
--
-- Drops and recreates everything, so this file doubles as a reset script.
--
--   mysql -u root -p < db/schema.sql
--   mysql -u root -p < db/seed.sql
-- ---------------------------------------------------------------------------

DROP DATABASE IF EXISTS deadlinebuddy_db;
CREATE DATABASE deadlinebuddy_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE deadlinebuddy_db;

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- subjects - every subject belongs to exactly one user
--
-- uq_subjects_id_user looks redundant next to the primary key, but it is what
-- lets the tasks table carry a composite foreign key on (subject_id, user_id).
-- That makes "a task's subject must belong to the same user as the task" a
-- database guarantee rather than only a service-layer check.
-- ---------------------------------------------------------------------------
CREATE TABLE subjects (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    name       VARCHAR(80) NOT NULL,
    color      VARCHAR(7)  NULL,
    created_at DATETIME    NOT NULL,
    updated_at DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_subjects_user_name UNIQUE (user_id, name),
    CONSTRAINT uq_subjects_id_user   UNIQUE (id, user_id),
    CONSTRAINT fk_subjects_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT ck_subjects_color CHECK (color IS NULL OR color REGEXP '^#[0-9a-fA-F]{6}$')
) ENGINE = InnoDB;

CREATE INDEX ix_subjects_user ON subjects (user_id);

-- ---------------------------------------------------------------------------
-- tasks
--
-- ON DELETE RESTRICT for the subject: deleting a subject that still has tasks
-- is refused by the database. The application asks the user to reassign or
-- delete those tasks first (see SubjectService.delete).
-- ---------------------------------------------------------------------------
CREATE TABLE tasks (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    subject_id   BIGINT       NOT NULL,
    title        VARCHAR(150) NOT NULL,
    description  TEXT         NULL,
    task_type    VARCHAR(16)  NOT NULL,
    priority     VARCHAR(8)   NOT NULL,
    status       VARCHAR(12)  NOT NULL,
    due_date     DATE         NOT NULL,
    due_time     TIME         NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    completed_at DATETIME     NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tasks_subject_owner FOREIGN KEY (subject_id, user_id)
        REFERENCES subjects (id, user_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_tasks_type     CHECK (task_type IN ('ASSIGNMENT','PRACTICAL','SUBMISSION','EXAM','OTHER')),
    CONSTRAINT ck_tasks_priority CHECK (priority  IN ('LOW','MEDIUM','HIGH')),
    CONSTRAINT ck_tasks_status   CHECK (status    IN ('PENDING','IN_PROGRESS','COMPLETED')),
    CONSTRAINT ck_tasks_completed_at CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
        OR (status <> 'COMPLETED' AND completed_at IS NULL))
) ENGINE = InnoDB;

CREATE INDEX ix_tasks_user_status   ON tasks (user_id, status);
CREATE INDEX ix_tasks_user_due      ON tasks (user_id, due_date);
CREATE INDEX ix_tasks_user_priority ON tasks (user_id, priority);
CREATE INDEX ix_tasks_subject       ON tasks (subject_id);

-- ---------------------------------------------------------------------------
-- The application connects as this user. Change the password here and in
-- src/java/deadlinebuddy.properties together, or drop these two statements and
-- keep using root (the default in the properties file).
-- ---------------------------------------------------------------------------
-- CREATE USER IF NOT EXISTS 'deadlinebuddy'@'localhost' IDENTIFIED BY 'change-me';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON deadlinebuddy_db.* TO 'deadlinebuddy'@'localhost';

SELECT 'deadlinebuddy_db created' AS status;
