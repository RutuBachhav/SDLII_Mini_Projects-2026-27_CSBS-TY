package com.mycompany.rentalmanagement.dao;

import com.mycompany.rentalmanagement.DBConnection;
import java.sql.*;
import java.time.*;
import java.math.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RentalDAO {

    public List<Map<String, Object>> list() throws SQLException {
        List<Map<String, Object>> a = new ArrayList<>();
        String sql = "SELECT r.*,c.full_name customer_name,v.registration_number,v.make,v.model,COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.rental_id=r.id),0) paid FROM rentals r JOIN customers c ON c.id=r.customer_id JOIN vehicles v ON v.id=r.vehicle_id ORDER BY r.id DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                for (String k : new String[]{"id", "customer_id", "vehicle_id", "customer_name", "registration_number", "make", "model", "start_date", "expected_return_date", "actual_return_date", "estimated_total", "final_total", "status", "paid", "daily_rate"}) {
                    m.put(k, r.getObject(k));
                }
                a.add(m);
            }
        }
        return a;
    }

    public List<Map<String, Object>> customers() throws SQLException {
        return simple("SELECT id,full_name label FROM customers WHERE active=1 ORDER BY full_name");
    }

    public List<Map<String, Object>> vehicles() throws SQLException {
        return simple("SELECT id,CONCAT(registration_number,' — ',make,' ',model,' (₹',daily_rate,'/day)') label FROM vehicles WHERE status='AVAILABLE' ORDER BY make,model");
    }

    private List<Map<String, Object>> simple(String sql) throws SQLException {
        List<Map<String, Object>> a = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getLong("id"));
                m.put("label", r.getString("label"));
                a.add(m);
            }
        }
        return a;
    }

    public void book(long customer, long vehicle, LocalDate start, LocalDate end, String notes) throws Exception {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Expected return date cannot be before start date.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal rate;
                try (PreparedStatement p = c.prepareStatement("SELECT daily_rate,status FROM vehicles WHERE id=? FOR UPDATE")) {
                    p.setLong(1, vehicle);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !"AVAILABLE".equals(r.getString("status"))) {
                            throw new IllegalArgumentException("Vehicle is not available.");
                        }
                        rate = r.getBigDecimal("daily_rate");
                    }
                }
                try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM rentals WHERE vehicle_id=? AND status IN ('BOOKED','ACTIVE') AND NOT(expected_return_date < ? OR start_date > ?)")) {
                    p.setLong(1, vehicle);
                        p.setLong(1, vehicle);
                        p.setDate(2, java.sql.Date.valueOf(start));
                        p.setDate(3, java.sql.Date.valueOf(end));
                    try (ResultSet r = p.executeQuery()) {
                        r.next();
                        if (r.getInt(1) > 0) {
                            throw new IllegalArgumentException("Vehicle already has an overlapping booking.");
                        }
                    }
                }
                long days = ChronoUnit.DAYS.between(start, end) + 1;
                BigDecimal total = rate.multiply(BigDecimal.valueOf(days));
                try (PreparedStatement p = c.prepareStatement("INSERT INTO rentals(customer_id,vehicle_id,start_date,expected_return_date,daily_rate,estimated_total,status,notes) VALUES(?,?,?,?,?,?,?,?)")) {
                    p.setLong(1, customer);
                    p.setLong(2, vehicle);
                    p.setDate(3, java.sql.Date.valueOf(start));
                    p.setDate(4, java.sql.Date.valueOf(end));
                    p.setBigDecimal(5, rate);
                    p.setBigDecimal(6, total);
                    p.setString(7, start.equals(LocalDate.now()) ? "ACTIVE" : "BOOKED");
                    p.setString(8, notes);
                    p.executeUpdate();
                }
                if (!start.isAfter(LocalDate.now()))try (PreparedStatement p = c.prepareStatement("UPDATE vehicles SET status='RENTED' WHERE id=?")) {
                    p.setLong(1, vehicle);
                    p.executeUpdate();
                }
                c.commit();
            } catch (IllegalArgumentException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void returnVehicle(long id, LocalDate actual) throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                long vehicle;
                LocalDate start;
                BigDecimal rate;
                String status;
                try (PreparedStatement p = c.prepareStatement("SELECT vehicle_id,start_date,daily_rate,status FROM rentals WHERE id=? FOR UPDATE")) {
                    p.setLong(1, id);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) {
                            throw new IllegalArgumentException("Rental not found.");
                        }
                        vehicle = r.getLong(1);
                        start = r.getDate(2).toLocalDate();
                        rate = r.getBigDecimal(3);
                        status = r.getString(4);
                    }
                }
                if (!"ACTIVE".equals(status) && !"BOOKED".equals(status)) {
                    throw new IllegalArgumentException("Only booked or active rentals can be returned.");
                }
                if (actual.isBefore(start)) {
                    throw new IllegalArgumentException("Return date cannot be before rental start date.");
                }
                BigDecimal total = rate.multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(start, actual) + 1));
                try (PreparedStatement p = c.prepareStatement("UPDATE rentals SET actual_return_date=?,final_total=?,status='COMPLETED' WHERE id=?")) {
                    p.setDate(1, java.sql.Date.valueOf(actual));
                    p.setBigDecimal(2, total);
                    p.setLong(3, id);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE vehicles SET status='AVAILABLE' WHERE id=? AND status='RENTED'")) {
                    p.setLong(1, vehicle);
                    p.executeUpdate();
                }
                c.commit();
            } catch (IllegalArgumentException | SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
