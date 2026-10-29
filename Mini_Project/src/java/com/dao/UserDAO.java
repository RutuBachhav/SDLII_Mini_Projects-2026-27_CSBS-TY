package com.dao;

import com.entity.User;
import com.util.DBConnection;
import com.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT user_id, name, email, phone, role, password_hash FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email == null ? "" : email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!PasswordUtil.matches(password, rs.getString("password_hash"))) {
                    return null;
                }
                return map(rs);
            }
        }
    }

    public User register(String name, String email, String phone, String password) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, phone, role) VALUES (?, ?, ?, ?, 'USER')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, phone == null || phone.isBlank() ? null : phone.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    User user = new User();
                    user.setId(keys.getInt(1));
                    user.setName(name.trim());
                    user.setEmail(email.trim().toLowerCase());
                    user.setPhone(phone);
                    user.setRole("USER");
                    return user;
                }
            }
            return null;
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        return user;
    }
}
