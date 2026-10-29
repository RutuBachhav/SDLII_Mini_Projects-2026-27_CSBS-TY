package com.pms.dao;

import com.pms.model.DashboardStats;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aggregate counters and grouped statistics (single aggregate queries). */
public final class StatsDao {

    private StatsDao() {}

    private static long count(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public static DashboardStats dashboard(Connection con) throws SQLException {
        long students = count(con, "SELECT COUNT(*) FROM students");
        long companies = count(con, "SELECT COUNT(*) FROM companies");
        long applications = count(con, "SELECT COUNT(*) FROM applications");
        long selected = count(con, "SELECT COUNT(*) FROM applications WHERE status = 'SELECTED'");
        return new DashboardStats(students, companies, applications, selected);
    }

    /** status -> count across all applications (one GROUP BY). */
    public static Map<String, Long> applicationsByStatus(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT status, COUNT(*) FROM applications GROUP BY status");
                ResultSet rs = ps.executeQuery()) {
            Map<String, Long> out = new LinkedHashMap<>();
            while (rs.next()) {
                out.put(rs.getString(1), rs.getLong(2));
            }
            return out;
        }
    }

    /** Per-company selection counts (one GROUP BY, avoids N+1). */
    public static List<String[]> selectionsByCompany(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT c.company_name, COUNT(*) FROM applications a"
                + " JOIN companies c ON c.company_id = a.company_id"
                + " WHERE a.status = 'SELECTED' GROUP BY c.company_name ORDER BY COUNT(*) DESC");
                ResultSet rs = ps.executeQuery()) {
            List<String[]> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new String[]{rs.getString(1), Long.toString(rs.getLong(2))});
            }
            return out;
        }
    }
}
