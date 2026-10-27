package com.splitease.dao;

import com.splitease.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** CRUD for table "users". Every query uses PreparedStatement (no SQL injection). */
public class UserDAO {

    public List<User> findAll() {
        String sql = "SELECT user_id, name, email, phone FROM users ORDER BY name";
        List<User> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
        return list;
    }

    public User findById(int id) {
        String sql = "SELECT user_id, name, email, phone FROM users WHERE user_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    public User insert(User u) {
        String sql = "INSERT INTO users (name, email, phone) VALUES (?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setId(keys.getInt(1));
                }
            }
            return u;
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("user_id"), rs.getString("name"),
                rs.getString("email"), rs.getString("phone"));
    }
}
