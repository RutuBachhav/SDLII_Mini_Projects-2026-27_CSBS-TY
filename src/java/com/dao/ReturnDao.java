package com.dao;

import com.util.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnDao {

    /**
     * Part of the return transaction. The unique key on claim_id is the real
     * guard against two simultaneous handovers being recorded for one claim:
     * the second insert fails instead of creating a second history row.
     */
    public void insert(Connection c, int itemId, int claimId, int handledBy, String note)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO item_returns (item_id, claim_id, handled_by, note) VALUES (?,?,?,?)")) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimId);
            ps.setInt(3, handledBy);
            ps.setString(4, note);
            ps.executeUpdate();
        }
    }

    public int countAll() throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM item_returns");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
