CREATE DATABASE IF NOT EXISTS hostel_db;
USE hostel_db;

DROP TABLE IF EXISTS complaint;
DROP TABLE IF EXISTS fee_payment;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE room (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_no VARCHAR(20) NOT NULL UNIQUE,
    room_type VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    current_occupancy INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
);

CREATE TABLE student (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(20),
    guardian_contact VARCHAR(20),
    admission_date DATE,
    room_id INT,
    user_id INT UNIQUE,
    CONSTRAINT fk_student_room
        FOREIGN KEY (room_id) REFERENCES room(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE fee_payment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    month VARCHAR(30) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date DATE,
    CONSTRAINT fk_fee_student
        FOREIGN KEY (student_id) REFERENCES student(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_fee_student_month (student_id, month)
);

CREATE TABLE complaint (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    date_raised DATE,
    date_resolved DATE,
    CONSTRAINT fk_complaint_student
        FOREIGN KEY (student_id) REFERENCES student(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO users (username, password, role)
VALUES ('admin', 'admin123', 'ADMIN');

INSERT INTO room (room_no, room_type, capacity, current_occupancy, status)
VALUES
('101', 'SINGLE', 1, 0, 'AVAILABLE'),
('102', 'DOUBLE', 2, 0, 'AVAILABLE'),
('103', 'DORM', 4, 0, 'AVAILABLE');
