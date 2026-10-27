package com.pms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** users table access. Password hashes leave this class only for verification. */
public final class UserDao {

    public record UserRow(long userId, String email, String passwordHash, String role) {}

    private UserDao() {}

    public static UserRow findByEmail(Connection con, String email) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT user_id, email, password_hash, role FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserRow(rs.getLong("user_id"), rs.getString("email"),
                        rs.getString("password_hash"), rs.getString("role"));
            }
        }
    }

    public static long countAdmins(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
