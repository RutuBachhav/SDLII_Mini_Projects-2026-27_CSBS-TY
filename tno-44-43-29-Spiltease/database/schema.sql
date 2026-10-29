-- =====================================================================
--  SplitEase : Group Expense Splitter & Settlement System
--  Database schema for MySQL 8.x
--  Run this once in MySQL Workbench / mysql CLI before deploying the WAR.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS splitease
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE splitease;

-- Least-privilege application user (the app never connects as root)
CREATE USER IF NOT EXISTS 'splitease_app'@'localhost' IDENTIFIED BY 'Split@2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON splitease.* TO 'splitease_app'@'localhost';
FLUSH PRIVILEGES;

DROP TABLE IF EXISTS settlements;
DROP TABLE IF EXISTS expense_splits;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS group_members;
DROP TABLE IF EXISTS expense_groups;
DROP TABLE IF EXISTS users;

-- 1. People who share expenses
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    phone       VARCHAR(15),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. A group = a trip, a flat, a club event ...
--    ("groups" is a reserved word in MySQL 8, hence expense_groups)
CREATE TABLE expense_groups (
    group_id     INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Many-to-many: which users belong to which group
CREATE TABLE group_members (
    group_id  INT NOT NULL,
    user_id   INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(user_id)
);

-- 4. One row per bill. Amounts are DECIMAL (never FLOAT) to avoid rounding errors
CREATE TABLE expenses (
    expense_id    INT AUTO_INCREMENT PRIMARY KEY,
    group_id      INT NOT NULL,
    description   VARCHAR(150) NOT NULL,
    amount        DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    paid_by       INT NOT NULL,
    split_type    ENUM('EQUAL','EXACT','PERCENT') NOT NULL,
    category      VARCHAR(30) DEFAULT 'General',
    expense_date  DATE NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (paid_by)  REFERENCES users(user_id)
);

-- 5. How each bill is divided: one row per participant
CREATE TABLE expense_splits (
    split_id      INT AUTO_INCREMENT PRIMARY KEY,
    expense_id    INT NOT NULL,
    user_id       INT NOT NULL,
    share_amount  DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(user_id)
);

-- 6. Payments made to clear dues ("Rahil paid Aditi Rs 500")
CREATE TABLE settlements (
    settlement_id  INT AUTO_INCREMENT PRIMARY KEY,
    group_id       INT NOT NULL,
    from_user      INT NOT NULL,
    to_user        INT NOT NULL,
    amount         DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    note           VARCHAR(150),
    settled_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id)  REFERENCES expense_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (from_user) REFERENCES users(user_id),
    FOREIGN KEY (to_user)   REFERENCES users(user_id)
);

CREATE INDEX idx_expense_group ON expenses(group_id);
CREATE INDEX idx_split_expense ON expense_splits(expense_id);
CREATE INDEX idx_settle_group  ON settlements(group_id);
