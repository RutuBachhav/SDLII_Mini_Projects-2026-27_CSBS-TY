package com.dao;

import com.util.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Photo metadata only. The bytes live on disk; MySQL stores the storage key,
 * which is a server-generated filename, never anything the browser sent.
 */
public class PhotoDao {

    /** What PhotoServlet needs to stream a file safely. */
    public static class Photo {
        public int id;
        public int itemId;
        public String storageKey;
        public String contentType;
    }

    public void insert(int itemId, String storageKey, String contentType, long size)
            throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO item_photos (item_id, storage_key, content_type, size_bytes) "
                     + "VALUES (?,?,?,?)")) {
            ps.setInt(1, itemId);
            ps.setString(2, storageKey);
            ps.setString(3, contentType);
            ps.setInt(4, (int) size);
            ps.executeUpdate();
        }
    }

    public Photo findById(int id) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, item_id, storage_key, content_type FROM item_photos WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Photo p = new Photo();
                p.id = rs.getInt("id");
                p.itemId = rs.getInt("item_id");
                p.storageKey = rs.getString("storage_key");
                p.contentType = rs.getString("content_type");
                return p;
            }
        }
    }

    public void deleteForItem(int itemId) throws SQLException {
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM item_photos WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        }
    }
}
