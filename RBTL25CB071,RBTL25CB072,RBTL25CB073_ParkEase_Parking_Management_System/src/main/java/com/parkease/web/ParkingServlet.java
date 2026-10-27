package com.parkease.web;

import com.parkease.util.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

@WebServlet("/parking")
public class ParkingServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        try (Connection con = DBUtil.getConnection()) {
            if ("checkin".equals(action)) {
                String vehicleNo = clean(request.getParameter("vehicleNumber")).toUpperCase();
                String owner = clean(request.getParameter("ownerName"));
                String type = request.getParameter("vehicleType");
                int slotId = Integer.parseInt(request.getParameter("slotId"));
                if (vehicleNo.isBlank() || owner.isBlank() || !("Car".equals(type) || "Bike".equals(type))) {
                    response.sendRedirect(request.getContextPath() + "/entry.jsp?error=invalid");
                    return;
                }
                con.setAutoCommit(false);
                try (PreparedStatement check = con.prepareStatement(
                        "SELECT status, slot_type FROM parking_slots WHERE id=? FOR UPDATE")) {
                    check.setInt(1, slotId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next() || !"Available".equals(rs.getString("status")) ||
                                !type.equals(rs.getString("slot_type"))) {
                            con.rollback();
                            response.sendRedirect(request.getContextPath() + "/entry.jsp?error=slot");
                            return;
                        }
                    }
                }
                try (PreparedStatement duplicate = con.prepareStatement(
                        "SELECT id FROM parking_sessions WHERE vehicle_number=? AND status='Parked'")) {
                    duplicate.setString(1, vehicleNo);
                    try (ResultSet rs = duplicate.executeQuery()) {
                        if (rs.next()) {
                            con.rollback();
                            response.sendRedirect(request.getContextPath() + "/entry.jsp?error=duplicate");
                            return;
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO parking_sessions(vehicle_number, owner_name, vehicle_type, slot_id) VALUES(?,?,?,?)");
                     PreparedStatement update = con.prepareStatement(
                        "UPDATE parking_slots SET status='Occupied' WHERE id=?")) {
                    ps.setString(1, vehicleNo); ps.setString(2, owner); ps.setString(3, type); ps.setInt(4, slotId);
                    ps.executeUpdate();
                    update.setInt(1, slotId); update.executeUpdate();
                }
                con.commit();
                response.sendRedirect(request.getContextPath() + "/slots.jsp?success=checkin");
                return;
            } else if ("checkout".equals(action)) {
                long sessionId = Long.parseLong(request.getParameter("sessionId"));
                con.setAutoCommit(false);
                int slotId;
                Timestamp entry;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT slot_id, entry_time FROM parking_sessions WHERE id=? AND status='Parked' FOR UPDATE")) {
                    ps.setLong(1, sessionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            response.sendRedirect(request.getContextPath() + "/slots.jsp?error=notfound");
                            return;
                        }
                        slotId = rs.getInt("slot_id"); entry = rs.getTimestamp("entry_time");
                    }
                }
                LocalDateTime in = entry.toLocalDateTime();
                long minutes = Math.max(1, Duration.between(in, LocalDateTime.now()).toMinutes());
                long hours = Math.max(1, (minutes + 59) / 60);
                // Demo tariff: ₹40 for the first started hour, then ₹20 per additional started hour.
                BigDecimal fee = BigDecimal.valueOf(40 + Math.max(0, hours - 1) * 20);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE parking_sessions SET exit_time=NOW(), fee=?, status='Completed' WHERE id=?");
                     PreparedStatement update = con.prepareStatement(
                        "UPDATE parking_slots SET status='Available' WHERE id=?")) {
                    ps.setBigDecimal(1, fee); ps.setLong(2, sessionId); ps.executeUpdate();
                    update.setInt(1, slotId); update.executeUpdate();
                }
                con.commit();
                response.sendRedirect(request.getContextPath() + "/history.jsp?success=checkout");
                return;
            }
        } catch (Exception ex) {
            getServletContext().log("Parking operation failed", ex);
            response.sendRedirect(request.getContextPath() + "/entry.jsp?error=database");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
