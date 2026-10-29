package com.dao;

import com.util.Db;
import com.entity.Claim;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClaimDao {

    private static final String SELECT =
            "SELECT cl.*, i.title AS item_title, i.type AS item_type, i.reporter_id AS item_reporter, "
            + "ru.name AS item_reporter_name, cu.name AS claimant_name, rv.name AS reviewer_name, "
            + "(SELECT COUNT(*) FROM item_returns r WHERE r.claim_id = cl.id) AS returned "
            + "FROM claims cl "
            + "JOIN items i ON i.id = cl.item_id "
            + "JOIN users ru ON ru.id = i.reporter_id "
            + "JOIN users cu ON cu.id = cl.claimant_id "
            + "LEFT JOIN users rv ON rv.id = cl.reviewed_by ";

    public int insert(int itemId, int claimantId, String evidence, String contact)
            throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO claims (item_id, claimant_id, evidence, contact) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimantId);
            ps.setString(3, evidence);
            ps.setString(4, contact);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Stops a second live claim by the same person on the same item. */
    public boolean hasLiveClaim(int itemId, int claimantId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM claims WHERE item_id = ? AND claimant_id = ? "
                     + "AND status IN ('PENDING','APPROVED','COMPLETED')")) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Claim findById(int id) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE cl.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Claim> listByClaimant(int claimantId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE cl.claimant_id = ? ORDER BY cl.created_at DESC")) {
            ps.setInt(1, claimantId);
            return mapAll(ps);
        }
    }

    /** Claims on reports this user filed: their review inbox. */
    public List<Claim> listForReviewer(int reporterId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE i.reporter_id = ? "
                     + "ORDER BY FIELD(cl.status,'PENDING','APPROVED','COMPLETED','REJECTED'), "
                     + "cl.created_at DESC")) {
            ps.setInt(1, reporterId);
            return mapAll(ps);
        }
    }

    public List<Claim> listByItem(int itemId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + "WHERE cl.item_id = ? ORDER BY cl.created_at DESC")) {
            ps.setInt(1, itemId);
            return mapAll(ps);
        }
    }

    public List<Claim> listAll(String status) throws SQLException {
        String where = (status == null || status.isEmpty() || "ALL".equals(status))
                ? "WHERE 1 = 1" : "WHERE cl.status = ?";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     SELECT + where + " ORDER BY cl.created_at DESC LIMIT 200")) {
            if (where.contains("?")) {
                ps.setString(1, status);
            }
            return mapAll(ps);
        }
    }

    /**
     * Status change guarded by the current status, so two reviewers clicking at
     * the same time cannot both succeed and an illegal jump cannot happen.
     * Returns false when the transition was not allowed.
     */
    public boolean transition(Connection c, int claimId, String from, String to,
                              Integer reviewerId, String note) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE claims SET status = ?, review_note = ?, reviewed_by = ?, reviewed_at = NOW() "
                + "WHERE id = ? AND status = ?")) {
            ps.setString(1, to);
            ps.setString(2, note);
            if (reviewerId == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, reviewerId);
            }
            ps.setInt(4, claimId);
            ps.setString(5, from);
            return ps.executeUpdate() == 1;
        }
    }

    /** Rejects every other pending claim once one is approved. */
    public int rejectOtherPending(Connection c, int itemId, int keepClaimId, int reviewerId)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE claims SET status = 'REJECTED', "
                + "review_note = 'Another claim on this item was approved.', "
                + "reviewed_by = ?, reviewed_at = NOW() "
                + "WHERE item_id = ? AND id <> ? AND status = 'PENDING'")) {
            ps.setInt(1, reviewerId);
            ps.setInt(2, itemId);
            ps.setInt(3, keepClaimId);
            return ps.executeUpdate();
        }
    }

    public int countByStatus(String status) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM claims WHERE status = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countPendingForReviewer(int reporterId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM claims cl JOIN items i ON i.id = cl.item_id "
                     + "WHERE i.reporter_id = ? AND cl.status = 'PENDING'")) {
            ps.setInt(1, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static List<Claim> mapAll(PreparedStatement ps) throws SQLException {
        List<Claim> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
        }
        return out;
    }

    private static Claim map(ResultSet rs) throws SQLException {
        Claim cl = new Claim();
        cl.setId(rs.getInt("id"));
        cl.setItemId(rs.getInt("item_id"));
        cl.setItemTitle(rs.getString("item_title"));
        cl.setItemType(rs.getString("item_type"));
        cl.setItemReporterId(rs.getInt("item_reporter"));
        cl.setItemReporterName(rs.getString("item_reporter_name"));
        cl.setClaimantId(rs.getInt("claimant_id"));
        cl.setClaimantName(rs.getString("claimant_name"));
        cl.setEvidence(rs.getString("evidence"));
        cl.setContact(rs.getString("contact"));
        cl.setStatus(rs.getString("status"));
        cl.setReviewNote(rs.getString("review_note"));
        int reviewer = rs.getInt("reviewed_by");
        cl.setReviewedBy(rs.wasNull() ? null : reviewer);
        cl.setReviewedByName(rs.getString("reviewer_name"));
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        cl.setReviewedAt(reviewedAt == null ? null : reviewedAt.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        cl.setCreatedAt(created == null ? null : created.toLocalDateTime());
        cl.setReturnRecorded(rs.getInt("returned") > 0);
        return cl;
    }
}
