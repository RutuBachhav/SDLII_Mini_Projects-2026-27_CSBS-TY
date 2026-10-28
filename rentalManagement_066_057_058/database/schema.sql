CREATE DATABASE IF NOT EXISTS vehicle_rental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vehicle_rental_db;

CREATE TABLE IF NOT EXISTS users (
 user_id INT PRIMARY KEY AUTO_INCREMENT,
 username VARCHAR(50) NOT NULL UNIQUE,
 password_text VARCHAR(100) NOT NULL,
 display_name VARCHAR(100) NOT NULL,
 role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
 active BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT IGNORE INTO users(username,password_text,display_name,role) VALUES('admin','admin123','System Administrator','ADMIN');

CREATE TABLE IF NOT EXISTS vehicles (
 vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
 registration_no VARCHAR(20) NOT NULL UNIQUE,
 vehicle_name VARCHAR(100) NOT NULL,
 brand VARCHAR(60) NOT NULL,
 vehicle_type VARCHAR(30) NOT NULL,
 model_year SMALLINT NULL,
 daily_rate DECIMAL(10,2) NOT NULL,
 status ENUM('AVAILABLE','RENTED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT chk_vehicle_rate CHECK (daily_rate >= 0)
);
CREATE TABLE IF NOT EXISTS customers (
 customer_id INT PRIMARY KEY AUTO_INCREMENT,
 full_name VARCHAR(100) NOT NULL,
 phone VARCHAR(20) NOT NULL,
 email VARCHAR(120) NULL,
 address VARCHAR(255) NULL,
 license_no VARCHAR(60) NOT NULL UNIQUE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS rentals (
 rental_id INT PRIMARY KEY AUTO_INCREMENT,
 customer_id INT NOT NULL,
 vehicle_id INT NOT NULL,
 rental_date DATE NOT NULL,
 expected_return_date DATE NOT NULL,
 actual_return_date DATE NULL,
 daily_rate_at_booking DECIMAL(10,2) NOT NULL,
 total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
 late_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
 status ENUM('ACTIVE','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_rental_customer FOREIGN KEY(customer_id) REFERENCES customers(customer_id),
 CONSTRAINT fk_rental_vehicle FOREIGN KEY(vehicle_id) REFERENCES vehicles(vehicle_id),
 CONSTRAINT chk_rental_dates CHECK(expected_return_date >= rental_date)
);
CREATE TABLE IF NOT EXISTS payments (
 payment_id INT PRIMARY KEY AUTO_INCREMENT,
 rental_id INT NOT NULL,
 amount DECIMAL(10,2) NOT NULL,
 payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 payment_method ENUM('CASH','UPI','CARD','BANK_TRANSFER') NOT NULL,
 payment_status ENUM('PAID','PENDING','REFUNDED') NOT NULL DEFAULT 'PAID',
 reference_note VARCHAR(150) NULL,
 CONSTRAINT fk_payment_rental FOREIGN KEY(rental_id) REFERENCES rentals(rental_id)
);
-- Indexes are optional for this college-scale dataset; omit explicit CREATE INDEX statements
-- so this schema can be rerun on the existing project database without duplicate-index errors.

INSERT IGNORE INTO vehicles(registration_no,vehicle_name,brand,vehicle_type,model_year,daily_rate,status)
VALUES ('MH12AB1234','Swift Dzire','Maruti Suzuki','Car',2023,1800.00,'AVAILABLE'),
       ('MH14CD5678','Activa 6G','Honda','Bike',2024,500.00,'AVAILABLE'),
       ('MH12EF9012','Creta','Hyundai','SUV',2022,2800.00,'AVAILABLE');
INSERT IGNORE INTO customers(full_name,phone,email,address,license_no)
VALUES ('Aarav Sharma','9876543210','aarav@example.com','Pune','DL-DEMO-1001'),
       ('Priya Patil','9876501234','priya@example.com','Pimpri-Chinchwad','DL-DEMO-1002');
