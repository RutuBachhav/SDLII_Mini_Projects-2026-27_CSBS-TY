-- Hotel Room Booking System : MySQL database setup
-- Run once in MySQL Workbench / mysql client (user: root).
-- Tables are auto-created by JPA (EclipseLink ddl-generation),
-- so only the database itself must exist.

CREATE DATABASE IF NOT EXISTS hotel_booking_db
  CHARACTER SET utf8mb4;

USE hotel_booking_db;

-- Expected tables (auto-created by JPA, shown for viva reference):
-- room    (id PK, room_number UNIQUE, room_type, price_per_night, available)
-- booking (id PK, customer_name, email, phone, check_in_date,
--          check_out_date, number_of_guests, room_id FK -> room.id)
