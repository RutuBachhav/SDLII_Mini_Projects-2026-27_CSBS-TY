package com.dao;

import com.util.Db;
import com.entity.Item;
import com.entity.MatchSuggestion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MatchDao {

    /**
     * One row per (lost, found) pair. The unique key on the pair plus this
     * upsert is what makes recomputing matches idempotent: scores get refreshed,
     * duplicates cannot appear.
     */
    public void save(int lostItemId, int foundItemId, int score, String reasons)
            throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO matches (lost_item_id, found_item_id, score, reasons) "
                     + "VALUES (?,?,?,?) "
                     + "ON DUPLICATE KEY UPDATE score = VALUES(score), reasons = VALUES(reasons)")) {
            ps.setInt(1, lostItemId);
            ps.setInt(2, foundItemId);
            ps.setInt(3, score);
            ps.setString(4, reasons);
            ps.executeUpdate();
        }
    }

    /** Drops suggestions that are no longer good enough or no longer eligible. */
    public void deleteBelow(int lostItemId, int foundItemId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM matches WHERE lost_item_id = ? AND found_item_id = ?")) {
            ps.setInt(1, lostItemId);
            ps.setInt(2, foundItemId);
            ps.executeUpdate();
        }
    }

    public void deleteForItem(int itemId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM matches WHERE lost_item_id = ? OR found_item_id = ?")) {
            ps.setInt(1, itemId);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    /** Suggestions for one report, with the other side of the pair attached. */
    public List<MatchSuggestion> forItem(int itemId, String itemType) throws SQLException {
        boolean lostSide = "LOST".equals(itemType);
        String otherColumn = lostSide ? "found_item_id" : "lost_item_id";
        String myColumn = lostSide ? "lost_item_id" : "found_item_id";
        String sql = "SELECT m.id AS match_id, m.lost_item_id, m.found_item_id, m.score, m.reasons, "
                + "i.*, u.name AS reporter_name, c.name AS category_name, "
                + "(SELECT p.id FROM item_photos p WHERE p.item_id = i.id ORDER BY p.id LIMIT 1) AS photo_id, "
                + "(SELECT COUNT(*) FROM claims cl WHERE cl.item_id = i.id AND cl.status='PENDING') AS open_claims "
                + "FROM matches m "
                + "JOIN items i ON i.id = m." + otherColumn + " "
                + "JOIN users u ON u.id = i.reporter_id "
                + "JOIN categories c ON c.id = i.category_id "
                + "WHERE m." + myColumn + " = ? AND i.status = 'OPEN' "
                + "ORDER BY m.score DESC";
        try (Connection c = Db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return mapAll(ps, false);
        }
    }

    /**
     * Every suggestion touching a report this user filed, for the dashboard.
     * The viewer's own report comes back as `mine`, the other side as
     * `counterpart`.
     */
    public List<MatchSuggestion> forUser(int userId, int limit) throws SQLException {
        String sql = "SELECT m.id AS match_id, m.lost_item_id, m.found_item_id, m.score, m.reasons, "
                + "mine.id AS mine_id, mine.title AS mine_title, mine.type AS mine_type, "
                + "mine.status AS mine_status, "
                + "other.*, u.name AS reporter_name, c.name AS category_name, "
                + "(SELECT p.id FROM item_photos p WHERE p.item_id = other.id ORDER BY p.id LIMIT 1) AS photo_id, "
                + "(SELECT COUNT(*) FROM claims cl WHERE cl.item_id = other.id AND cl.status='PENDING') AS open_claims "
                + "FROM matches m "
                + "JOIN items l ON l.id = m.lost_item_id "
                + "JOIN items f ON f.id = m.found_item_id "
                + "JOIN items mine  ON mine.id  = CASE WHEN l.reporter_id = ? "
                + "                                    THEN m.lost_item_id ELSE m.found_item_id END "
                + "JOIN items other ON other.id = CASE WHEN l.reporter_id = ? "
                + "                                    THEN m.found_item_id ELSE m.lost_item_id END "
                + "JOIN users u ON u.id = other.reporter_id "
                + "JOIN categories c ON c.id = other.category_id "
                + "WHERE (l.reporter_id = ? OR f.reporter_id = ?) "
                + "AND l.status = 'OPEN' AND f.status = 'OPEN' "
                + "ORDER BY m.score DESC LIMIT ?";
        try (Connection c = Db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, limit);
            return mapAll(ps, true);
        }
    }

    public int countAll() throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM matches");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static List<MatchSuggestion> mapAll(PreparedStatement ps, boolean withMine)
            throws SQLException {
        List<MatchSuggestion> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MatchSuggestion m = new MatchSuggestion();
                m.setId(rs.getInt("match_id"));
                m.setLostItemId(rs.getInt("lost_item_id"));
                m.setFoundItemId(rs.getInt("found_item_id"));
                m.setScore(rs.getInt("score"));
                m.setReasons(rs.getString("reasons"));
                m.setCounterpart(ItemDao.map(rs));
                if (withMine) {
                    Item mine = new Item();
                    mine.setId(rs.getInt("mine_id"));
                    mine.setTitle(rs.getString("mine_title"));
                    mine.setType(rs.getString("mine_type"));
                    mine.setStatus(rs.getString("mine_status"));
                    m.setMine(mine);
                }
                out.add(m);
            }
        }
        return out;
    }
}
