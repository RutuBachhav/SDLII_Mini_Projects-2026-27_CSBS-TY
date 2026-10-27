package com.parkease.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

@WebListener
public class DatabaseInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        String createSlots = "CREATE TABLE IF NOT EXISTS parking_slots (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, slot_code VARCHAR(10) NOT NULL UNIQUE, " +
            "slot_type ENUM('Car','Bike') NOT NULL DEFAULT 'Car', " +
            "status ENUM('Available','Occupied') NOT NULL DEFAULT 'Available')";
        String createSessions = "CREATE TABLE IF NOT EXISTS parking_sessions (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, vehicle_number VARCHAR(20) NOT NULL, " +
            "owner_name VARCHAR(100) NOT NULL, vehicle_type ENUM('Car','Bike') NOT NULL, " +
            "slot_id INT NOT NULL, entry_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "exit_time DATETIME NULL, fee DECIMAL(10,2) NULL DEFAULT NULL, " +
            "status ENUM('Parked','Completed') NOT NULL DEFAULT 'Parked', " +
            "CONSTRAINT fk_session_slot FOREIGN KEY (slot_id) REFERENCES parking_slots(id))";
        try (Connection con = DBUtil.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate(createSlots);
            st.executeUpdate(createSessions);
            try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO parking_slots (slot_code, slot_type, status) VALUES (?, ?, 'Available')")) {
                for (int i = 1; i <= 6; i++) {
                    ps.setString(1, String.format("C-%02d", i)); ps.setString(2, "Car"); ps.addBatch();
                }
                for (int i = 1; i <= 6; i++) {
                    ps.setString(1, String.format("B-%02d", i)); ps.setString(2, "Bike"); ps.addBatch();
                }
                ps.executeBatch();
            }
            event.getServletContext().log("ParkEase database tables are ready.");
        } catch (Exception ex) {
            event.getServletContext().log("ParkEase database initialization failed. Check DBUtil.java and run database/schema.sql.", ex);
        }
    }
}
