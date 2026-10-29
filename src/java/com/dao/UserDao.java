package com.dao;

import com.util.Db;
import com.entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    /**
     * The hash is returned only by this method, which exists purely for the
     * login check. Nothing else reads the column.
     */
    public String passwordHashFor(String email) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT password_hash FROM users WHERE email = ? AND active = 1")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Always creates a USER. The role is not a parameter, so no registration
     * form, however it is tampered with, can create an administrator.
     */
    public int create(String name, String email, String passwordHash) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES (?,?,?,'USER')",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<User> list(String keyword) throws SQLException {
        String sql = "SELECT u.*, (SELECT COUNT(*) FROM items i WHERE i.reporter_id = u.id) AS reports "
                + "FROM users u WHERE (? = '' OR u.name LIKE ? OR u.email LIKE ?) "
                + "ORDER BY u.created_at DESC";
        try (Connection c = Db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            String k = keyword == null ? "" : keyword.trim();
            ps.setString(1, k);
            ps.setString(2, "%" + k + "%");
            ps.setString(3, "%" + k + "%");
            List<User> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = map(rs);
                    u.setReportCount(rs.getInt("reports"));
                    out.add(u);
                }
            }
            return out;
        }
    }

    /**
     * Deactivation instead of deletion, so a user's reports and claims keep
     * their history and the foreign keys stay valid.
     */
    public void setActive(int id, boolean active) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE users SET active = ? WHERE id = ? AND role <> 'ADMIN'")) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int countAll() throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("active"));
        Timestamp t = rs.getTimestamp("created_at");
        u.setCreatedAt(t == null ? null : t.toLocalDateTime());
        return u;
    }
}
