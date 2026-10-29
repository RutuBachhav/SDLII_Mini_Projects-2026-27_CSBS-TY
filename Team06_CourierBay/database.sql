-- CourierBay Database Schema
CREATE DATABASE IF NOT EXISTS courier_bay;
USE courier_bay;

CREATE TABLE IF NOT EXISTS couriers (
    courier_id INT AUTO_INCREMENT PRIMARY KEY,
    tracking_id VARCHAR(50) NOT NULL UNIQUE,
    sender_name VARCHAR(100) NOT NULL,
    sender_phone VARCHAR(20) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    pickup_address TEXT NOT NULL,
    delivery_address TEXT NOT NULL,
    courier_type VARCHAR(50) NOT NULL,
    weight DOUBLE NOT NULL,
    delivery_status VARCHAR(50) DEFAULT 'Booked',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
