CREATE DATABASE IF NOT EXISTS parkease_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parkease_db;

CREATE TABLE IF NOT EXISTS parking_slots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    slot_code VARCHAR(10) NOT NULL UNIQUE,
    slot_type ENUM('Car','Bike') NOT NULL DEFAULT 'Car',
    status ENUM('Available','Occupied') NOT NULL DEFAULT 'Available'
);

CREATE TABLE IF NOT EXISTS parking_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    vehicle_type ENUM('Car','Bike') NOT NULL,
    slot_id INT NOT NULL,
    entry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time DATETIME NULL,
    fee DECIMAL(10,2) NULL DEFAULT NULL,
    status ENUM('Parked','Completed') NOT NULL DEFAULT 'Parked',
    CONSTRAINT fk_session_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(id)
);

INSERT IGNORE INTO parking_slots (slot_code, slot_type, status) VALUES
('C-01','Car','Available'),('C-02','Car','Available'),('C-03','Car','Available'),
('C-04','Car','Available'),('C-05','Car','Available'),('C-06','Car','Available'),
('B-01','Bike','Available'),('B-02','Bike','Available'),('B-03','Bike','Available'),
('B-04','Bike','Available'),('B-05','Bike','Available'),('B-06','Bike','Available');
