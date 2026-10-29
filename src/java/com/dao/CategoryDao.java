package com.dao;

import com.util.Db;
import com.entity.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public List<Category> listActive() throws SQLException {
        return query("SELECT c.*, 0 AS n FROM categories c WHERE c.active = 1 ORDER BY c.name");
    }

    /** With usage counts, so an admin can see what retiring a category affects. */
    public List<Category> listAllWithCounts() throws SQLException {
        return query("SELECT c.*, (SELECT COUNT(*) FROM items i WHERE i.category_id = c.id) AS n "
                + "FROM categories c ORDER BY c.active DESC, c.name");
    }

    public Category findById(int id) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT c.*, 0 AS n FROM categories c WHERE c.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean exists(int id) throws SQLException {
        Category c = findById(id);
        return c != null && c.isActive();
    }

    public void create(String name) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO categories (name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public void rename(int id, String name) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE categories SET name = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Retiring rather than deleting. Existing reports keep pointing at the
     * category, they just stop being offered on new report forms.
     */
    public void setActive(int id, boolean active) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE categories SET active = ? WHERE id = ?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private List<Category> query(String sql) throws SQLException {
        List<Category> out = new ArrayList<>();
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    private static Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setActive(rs.getBoolean("active"));
        c.setItemCount(rs.getInt("n"));
        return c;
    }
}
