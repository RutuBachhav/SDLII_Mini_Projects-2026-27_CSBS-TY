package com.dao;

import com.util.Db;
import com.entity.Item;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reads and writes reports. Every statement is prepared. */
public class ItemDao {

    /** Columns plus the joins every list and detail page needs. */
    private static final String SELECT =
            "SELECT i.*, u.name AS reporter_name, c.name AS category_name, "
            + "(SELECT p.id FROM item_photos p WHERE p.item_id = i.id ORDER BY p.id LIMIT 1) AS photo_id, "
            + "(SELECT COUNT(*) FROM claims cl WHERE cl.item_id = i.id AND cl.status = 'PENDING') AS open_claims "
            + "FROM items i "
            + "JOIN users u ON u.id = i.reporter_id "
            + "JOIN categories c ON c.id = i.category_id ";

    public int insert(Item item) throws SQLException {
        String sql = "INSERT INTO items (reporter_id, type, title, category_id, description, "
                + "color, private_marks, location, item_date, status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,'OPEN')";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getReporterId());
            ps.setString(2, item.getType());
            ps.setString(3, item.getTitle());
            ps.setInt(4, item.getCategoryId());
            ps.setString(5, item.getDescription());
            ps.setString(6, item.getColor());
            ps.setString(7, item.getPrivateMarks());
            ps.setString(8, item.getLocation());
            ps.setDate(9, Date.valueOf(item.getItemDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(Item item) throws SQLException {
        String sql = "UPDATE items SET title=?, category_id=?, description=?, color=?, "
                + "private_marks=?, location=?, item_date=? WHERE id=?";
        try (Connection c = Db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, item.getTitle());
            ps.setInt(2, item.getCategoryId());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getColor());
            ps.setString(5, item.getPrivateMarks());
            ps.setString(6, item.getLocation());
            ps.setDate(7, Date.valueOf(item.getItemDate()));
            ps.setInt(8, item.getId());
            ps.executeUpdate();
        }
    }

    public Item findById(int id) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE i.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** The newest reports for the landing page. */
    public List<Item> recent(int limit) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE i.status = 'OPEN' ORDER BY i.created_at DESC LIMIT ?")) {
            ps.setInt(1, limit);
            return mapAll(ps);
        }
    }

    public List<Item> listByReporter(int reporterId, String type) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE i.reporter_id = ? AND i.type = ? ORDER BY i.created_at DESC")) {
            ps.setInt(1, reporterId);
            ps.setString(2, type);
            return mapAll(ps);
        }
    }

    public List<Item> search(ItemQuery q) throws SQLException {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(q, args);
        String order = "oldest".equals(q.getSort()) ? "ASC" : "DESC";
        String sql = SELECT + where + " ORDER BY i.created_at " + order + " LIMIT ? OFFSET ?";
        args.add(q.getPageSize());
        args.add(q.getOffset());
        try (Connection c = Db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            return mapAll(ps);
        }
    }

    public int count(ItemQuery q) throws SQLException {
        List<Object> args = new ArrayList<>();
        String where = buildWhere(q, args);
        String sql = "SELECT COUNT(*) FROM items i " + where;
        try (Connection c = Db.open(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Candidates for matching: open reports of one side of the board. Closed and
     * resolved reports are excluded here, which is what keeps finished cases out
     * of new suggestions.
     */
    public List<Item> openByType(String type) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE i.type = ? AND i.status = 'OPEN'")) {
            ps.setString(1, type);
            return mapAll(ps);
        }
    }

    /** Status changes go through here so the transition rules live in one place. */
    public boolean setStatus(int id, String newStatus, String... allowedCurrent) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE items SET status = ?");
        if ("RESOLVED".equals(newStatus)) {
            sql.append(", resolved_at = NOW()");
        } else {
            sql.append(", resolved_at = NULL");
        }
        sql.append(" WHERE id = ?");
        if (allowedCurrent.length > 0) {
            sql.append(" AND status IN (").append("?,".repeat(allowedCurrent.length - 1)).append("?)");
        }
        try (Connection c = Db.open(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            for (int i = 0; i < allowedCurrent.length; i++) {
                ps.setString(3 + i, allowedCurrent[i]);
            }
            return ps.executeUpdate() == 1;
        }
    }

    /** Same thing inside a caller's transaction, used by the return workflow. */
    public boolean setStatus(Connection c, int id, String newStatus, String allowedCurrent)
            throws SQLException {
        String sql = "UPDATE items SET status = ?, resolved_at = "
                + ("RESOLVED".equals(newStatus) ? "NOW()" : "NULL")
                + " WHERE id = ? AND status = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            ps.setString(3, allowedCurrent);
            return ps.executeUpdate() == 1;
        }
    }

    public int countByTypeAndStatus(String type, String status) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM items WHERE type = ? AND status = ?")) {
            ps.setString(1, type);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countAll() throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM items");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Distinct locations, used to fill the location filter. */
    public List<String> locations() throws SQLException {
        List<String> out = new ArrayList<>();
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT location, COUNT(*) n FROM items GROUP BY location "
                     + "ORDER BY n DESC, location ASC LIMIT 30");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(rs.getString(1));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ helpers

    private String buildWhere(ItemQuery q, List<Object> args) {
        StringBuilder w = new StringBuilder(" WHERE 1 = 1");
        if (!q.getKeyword().isEmpty()) {
            w.append(" AND (i.title LIKE ? OR i.description LIKE ? OR i.location LIKE ?)");
            String like = "%" + q.getKeyword() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if ("LOST".equals(q.getType()) || "FOUND".equals(q.getType())) {
            w.append(" AND i.type = ?");
            args.add(q.getType());
        }
        if (q.getCategoryId() > 0) {
            w.append(" AND i.category_id = ?");
            args.add(q.getCategoryId());
        }
        if (!q.getLocation().isEmpty()) {
            w.append(" AND i.location LIKE ?");
            args.add("%" + q.getLocation() + "%");
        }
        if (!q.getStatus().isEmpty() && !"ALL".equals(q.getStatus())) {
            w.append(" AND i.status = ?");
            args.add(q.getStatus());
        }
        LocalDate from = parseDate(q.getDateFrom());
        if (from != null) {
            w.append(" AND i.item_date >= ?");
            args.add(Date.valueOf(from));
        }
        LocalDate to = parseDate(q.getDateTo());
        if (to != null) {
            w.append(" AND i.item_date <= ?");
            args.add(Date.valueOf(to));
        }
        if (q.getReporterId() > 0) {
            w.append(" AND i.reporter_id = ?");
            args.add(q.getReporterId());
        }
        return w.toString();
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void bind(PreparedStatement ps, List<Object> args) throws SQLException {
        for (int i = 0; i < args.size(); i++) {
            ps.setObject(i + 1, args.get(i));
        }
    }

    private static List<Item> mapAll(PreparedStatement ps) throws SQLException {
        List<Item> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    static Item map(ResultSet rs) throws SQLException {
        Item i = new Item();
        i.setId(rs.getInt("id"));
        i.setReporterId(rs.getInt("reporter_id"));
        i.setReporterName(rs.getString("reporter_name"));
        i.setType(rs.getString("type"));
        i.setTitle(rs.getString("title"));
        i.setCategoryId(rs.getInt("category_id"));
        i.setCategoryName(rs.getString("category_name"));
        i.setDescription(rs.getString("description"));
        i.setColor(rs.getString("color"));
        i.setPrivateMarks(rs.getString("private_marks"));
        i.setLocation(rs.getString("location"));
        Date d = rs.getDate("item_date");
        i.setItemDate(d == null ? null : d.toLocalDate());
        i.setStatus(rs.getString("status"));
        Timestamp created = rs.getTimestamp("created_at");
        i.setCreatedAt(created == null ? null : created.toLocalDateTime());
        Timestamp resolved = rs.getTimestamp("resolved_at");
        i.setResolvedAt(resolved == null ? null : resolved.toLocalDateTime());
        int photoId = rs.getInt("photo_id");
        i.setPhotoId(rs.wasNull() ? null : photoId);
        i.setOpenClaimCount(rs.getInt("open_claims"));
        return i;
    }
}
