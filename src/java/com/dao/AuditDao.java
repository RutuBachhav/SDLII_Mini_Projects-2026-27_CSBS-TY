package com.dao;

import com.util.Db;
import com.entity.AuditEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Administrative history. Append-only: rows are written and read, never edited. */
public class AuditDao {

    public void log(Integer actorId, String action, String entity, Integer entityId, String detail) {
        try (Connection c = Db.open()) {
            log(c, actorId, action, entity, entityId, detail);
        } catch (SQLException e) {
            // History is useful but must never break the operation it describes.
            System.getLogger("FoundIt").log(System.Logger.Level.WARNING,
                    "Could not write audit entry: " + e.getMessage());
        }
    }

    public void log(Connection c, Integer actorId, String action, String entity,
                    Integer entityId, String detail) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO audit_log (actor_id, action, entity, entity_id, detail) "
                + "VALUES (?,?,?,?,?)")) {
            if (actorId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, actorId);
            }
            ps.setString(2, action);
            ps.setString(3, entity);
            if (entityId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, entityId);
            }
            ps.setString(5, detail);
            ps.executeUpdate();
        }
    }

    public List<AuditEntry> recent(int limit) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT a.*, u.name AS actor_name FROM audit_log a "
                     + "LEFT JOIN users u ON u.id = a.actor_id "
                     + "ORDER BY a.created_at DESC, a.id DESC LIMIT ?")) {
            ps.setInt(1, limit);
            List<AuditEntry> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditEntry a = new AuditEntry();
                    a.setId(rs.getInt("id"));
                    a.setActorName(rs.getString("actor_name"));
                    a.setAction(rs.getString("action"));
                    a.setEntity(rs.getString("entity"));
                    int entityId = rs.getInt("entity_id");
                    a.setEntityId(rs.wasNull() ? null : entityId);
                    a.setDetail(rs.getString("detail"));
                    Timestamp t = rs.getTimestamp("created_at");
                    a.setCreatedAt(t == null ? null : t.toLocalDateTime());
                    out.add(a);
                }
            }
            return out;
        }
    }
}
