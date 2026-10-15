package com.dao;

import com.util.Db;
import com.entity.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    /**
     * Insert-if-new. The unique dedupe_key means a page reload, a repeated form
     * submission or a re-run of matching cannot produce a second copy; the
     * database refuses it and this method quietly does nothing.
     */
    public void add(int userId, String kind, String message, String link, String dedupeKey)
            throws SQLException {
        try (Connection c = Db.open()) {
            add(c, userId, kind, message, link, dedupeKey);
        }
    }

    /** Same thing on a caller's connection, so it joins their transaction. */
    public void add(Connection c, int userId, String kind, String message, String link,
                    String dedupeKey) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO notifications (user_id, kind, message, link, dedupe_key) "
                + "VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE id = id")) {
            ps.setInt(1, userId);
            ps.setString(2, kind);
            ps.setString(3, message);
            ps.setString(4, link);
            ps.setString(5, dedupeKey);
            ps.executeUpdate();
        }
    }

    public List<Notification> listForUser(int userId, int limit) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM notifications WHERE user_id = ? "
                     + "ORDER BY is_read ASC, created_at DESC LIMIT ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            List<Notification> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(rs.getInt("user_id"));
                    n.setKind(rs.getString("kind"));
                    n.setMessage(rs.getString("message"));
                    n.setLink(rs.getString("link"));
                    n.setRead(rs.getBoolean("is_read"));
                    Timestamp t = rs.getTimestamp("created_at");
                    n.setCreatedAt(t == null ? null : t.toLocalDateTime());
                    out.add(n);
                }
            }
            return out;
        }
    }

    public int countUnread(int userId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** The user id in the WHERE clause is the authorisation check. */
    public void markRead(int id, int userId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void markAllRead(int userId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE notifications SET is_read = 1 WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
