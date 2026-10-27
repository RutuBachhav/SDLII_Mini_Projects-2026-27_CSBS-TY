package com.util;

import java.sql.*;
import java.util.*;

/** Minimal JDBC helper. Edit URL/USER/PASS for your MySQL. */
public class Db {
    private static final String URL = "jdbc:mysql://localhost:3306/ems_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String USER = "root";
    private static final String PASS = "123456789";

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new ExceptionInInitializerError(e); }
    }

    public interface Work { void run(Connection c) throws SQLException; }

    public static Connection conn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static List<Map<String, Object>> query(String sql, Object... p) throws SQLException {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        Object v = rs.getObject(i);
                        if (v instanceof java.util.Date || v instanceof java.time.temporal.Temporal)
                            v = rs.getString(i); // DATE/TIME/DATETIME -> text
                        m.put(md.getColumnLabel(i), v);
                    }
                    rows.add(m);
                }
                return rows;
            }
        }
    }

    public static Map<String, Object> one(String sql, Object... p) throws SQLException {
        List<Map<String, Object>> rows = query(sql, p);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static int update(String sql, Object... p) throws SQLException {
        try (Connection c = conn()) { return update(c, sql, p); }
    }

    public static int update(Connection c, String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            return ps.executeUpdate();
        }
    }

    /** Runs several statements atomically. */
    public static void tx(Work w) throws SQLException {
        try (Connection c = conn()) {
            c.setAutoCommit(false);
            try { w.run(c); c.commit(); }
            catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    private static void bind(PreparedStatement ps, Object[] p) throws SQLException {
        for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
    }
}
