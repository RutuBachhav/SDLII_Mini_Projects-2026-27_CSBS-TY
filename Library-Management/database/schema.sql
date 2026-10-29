-- Library Management System
-- Database: library_management
-- Run this script in MySQL Workbench (or mysql CLI) as a privileged user.

CREATE DATABASE IF NOT EXISTS library_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_management;

-- ---------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS admins (
    admin_id     INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(120) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admins_username (username),
    UNIQUE KEY uk_admins_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS books (
    book_id           INT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    author            VARCHAR(150) NOT NULL,
    isbn              VARCHAR(20)  NOT NULL,
    category          VARCHAR(80)  NOT NULL,
    publisher         VARCHAR(150) NOT NULL,
    publication_year  SMALLINT     NOT NULL,
    total_copies      INT          NOT NULL DEFAULT 1,
    available_copies  INT          NOT NULL DEFAULT 1,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_books_isbn (isbn),
    INDEX idx_books_title (title),
    INDEX idx_books_author (author),
    INDEX idx_books_category (category),
    CONSTRAINT chk_books_copies CHECK (total_copies >= 0 AND available_copies >= 0 AND available_copies <= total_copies),
    CONSTRAINT chk_books_year CHECK (publication_year BETWEEN 1000 AND 2100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS members (
    member_id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name          VARCHAR(120) NOT NULL,
    email              VARCHAR(120) NOT NULL,
    phone              VARCHAR(20)  NOT NULL,
    address            VARCHAR(255) NOT NULL,
    registration_date  DATE         NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_members_email (email),
    INDEX idx_members_name (full_name),
    INDEX idx_members_phone (phone)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS loans (
    loan_id      INT AUTO_INCREMENT PRIMARY KEY,
    book_id      INT          NOT NULL,
    member_id    INT          NOT NULL,
    issue_date   DATE         NOT NULL,
    due_date     DATE         NOT NULL,
    return_date  DATE         NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ISSUED',
    fine_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loans_book (book_id),
    INDEX idx_loans_member (member_id),
    INDEX idx_loans_status (status),
    INDEX idx_loans_due_date (due_date),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (book_id),
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members (member_id),
    CONSTRAINT chk_loans_status CHECK (status IN ('ISSUED', 'RETURNED')),
    CONSTRAINT chk_loans_fine CHECK (fine_amount >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Sample records
-- Default admin login: username = admin, password = admin123
-- Password is PBKDF2-HMAC-SHA256 (120000 iterations). See PasswordUtil.java.
-- ---------------------------------------------------------------------------

INSERT INTO admins (username, password_hash, full_name, email) VALUES
('admin', '120000:a1b2c3d4e5f60718293a4b5c6d7e8f90:75597589dff462e95e8a6ea3c510e7c1d061132e439d05a6fb424bba8599ecc8',
 'Library Administrator', 'admin@library.local'),
('librarian', '120000:a1b2c3d4e5f60718293a4b5c6d7e8f90:75597589dff462e95e8a6ea3c510e7c1d061132e439d05a6fb424bba8599ecc8',
 'Senior Librarian', 'librarian@library.local');

INSERT INTO books (title, author, isbn, category, publisher, publication_year, total_copies, available_copies) VALUES
('Effective Java', 'Joshua Bloch', '9780134685991', 'Programming', 'Addison-Wesley', 2018, 5, 4),
('Clean Code', 'Robert C. Martin', '9780132350884', 'Programming', 'Prentice Hall', 2008, 4, 3),
('Design Patterns', 'Erich Gamma', '9780201633610', 'Software Engineering', 'Addison-Wesley', 1994, 3, 2),
('Introduction to Algorithms', 'Thomas H. Cormen', '9780262046305', 'Computer Science', 'MIT Press', 2022, 6, 5),
('The Pragmatic Programmer', 'Andrew Hunt', '9780135957059', 'Programming', 'Addison-Wesley', 2019, 4, 4),
('Database System Concepts', 'Abraham Silberschatz', '9780078022159', 'Databases', 'McGraw-Hill', 2019, 3, 2),
('Computer Networks', 'Andrew S. Tanenbaum', '9780132126953', 'Networking', 'Pearson', 2010, 3, 3),
('Head First Java', 'Kathy Sierra', '9781491910771', 'Programming', 'O''Reilly Media', 2022, 5, 5),
('Refactoring', 'Martin Fowler', '9780134757599', 'Software Engineering', 'Addison-Wesley', 2018, 2, 2),
('Java Concurrency in Practice', 'Brian Goetz', '9780321349606', 'Programming', 'Addison-Wesley', 2006, 2, 2);

INSERT INTO members (full_name, email, phone, address, registration_date) VALUES
('Aisha Rahman', 'aisha.rahman@example.com', '01711000001', '12 Green Road, Dhaka', CURDATE() - INTERVAL 40 DAY),
('Farhan Ahmed', 'farhan.ahmed@example.com', '01711000002', '45 Lake View, Chattogram', CURDATE() - INTERVAL 30 DAY),
('Nadia Khan', 'nadia.khan@example.com', '01711000003', '8 College Street, Rajshahi', CURDATE() - INTERVAL 20 DAY),
('Omar Hasan', 'omar.hasan@example.com', '01711000004', '22 Station Road, Khulna', CURDATE() - INTERVAL 10 DAY),
('Priya Das', 'priya.das@example.com', '01711000005', '3 University Ave, Sylhet', CURDATE() - INTERVAL 5 DAY);

-- Active, overdue, and returned loans so the dashboard and reports have real data.
INSERT INTO loans (book_id, member_id, issue_date, due_date, return_date, status, fine_amount) VALUES
(1, 1, CURDATE() - INTERVAL 7 DAY,  CURDATE() + INTERVAL 7 DAY,  NULL, 'ISSUED', 0.00),
(2, 2, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 6 DAY,  NULL, 'ISSUED', 0.00),
(3, 3, CURDATE() - INTERVAL 10 DAY, CURDATE() + INTERVAL 4 DAY,  NULL, 'ISSUED', 0.00),
(4, 4, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 11 DAY, NULL, 'ISSUED', 0.00),
(6, 5, CURDATE() - INTERVAL 18 DAY, CURDATE() - INTERVAL 4 DAY,  NULL, 'ISSUED', 0.00),
(9, 1, CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 26 DAY, CURDATE() - INTERVAL 20 DAY, 'RETURNED', 30.00),
(5, 2, CURDATE() - INTERVAL 35 DAY, CURDATE() - INTERVAL 21 DAY, CURDATE() - INTERVAL 22 DAY, 'RETURNED', 0.00);
