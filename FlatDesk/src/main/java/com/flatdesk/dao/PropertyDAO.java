
package com.flatdesk.dao;

import com.flatdesk.config.DB;
import com.flatdesk.model.Property;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {

    // GET ALL PROPERTIES
    public List<Property> all() {

        List<Property> list = new ArrayList<>();
        String sql = "SELECT * FROM properties ORDER BY id DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapProperty(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // GET PROPERTIES OF ONE OWNER
    public List<Property> findByOwnerId(int ownerId) {

        List<Property> list = new ArrayList<>();
        String sql =
                "SELECT * FROM properties WHERE owner_id = ? ORDER BY id DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProperty(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // FIND ONE PROPERTY BY ID
    public Property findById(int id) {

        String sql = "SELECT * FROM properties WHERE id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProperty(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ADD PROPERTY
    public int add(Property p) {

        String sql =
                "INSERT INTO properties " +
                "(title, country, city, address, type, bhk, rent, area, price, " +
                "owner, contact_number, owner_id, broker_id, created_by, created_by_role, " +
                "notes, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getTitle());
            ps.setString(2, p.getCountry());
            ps.setString(3, p.getCity());
            ps.setString(4, p.getAddress());
            ps.setString(5, p.getType());
            ps.setString(6, p.getBhk());
            ps.setDouble(7, p.getRent());
            ps.setDouble(8, p.getArea());
            ps.setDouble(9, p.getPrice());
            ps.setString(10, p.getOwner());
            ps.setString(11, p.getContactNumber());

            if (p.getOwnerId() > 0) {
                ps.setInt(12, p.getOwnerId());
            } else {
                ps.setNull(12, Types.INTEGER);
            }

            if (p.getBrokerId() > 0) {
                ps.setInt(13, p.getBrokerId());
            } else {
                ps.setNull(13, Types.INTEGER);
            }

            if (p.getCreatedBy() > 0) {
                ps.setInt(14, p.getCreatedBy());
            } else {
                ps.setNull(14, Types.INTEGER);
            }

            ps.setString(15, p.getCreatedByRole());
            ps.setString(16, p.getNotes());
            ps.setString(17, p.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    // UPDATE PROPERTY DETAILS ONLY
    // Ownership, creator and status are not changed here.
    public boolean updateDetails(int id, Property p) {

        String sql =
                "UPDATE properties SET " +
                "title = ?, country = ?, city = ?, address = ?, " +
                "type = ?, bhk = ?, rent = ?, area = ?, price = ?, " +
                "owner = ?, contact_number = ?, notes = ? " +
                "WHERE id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getTitle());
            ps.setString(2, p.getCountry());
            ps.setString(3, p.getCity());
            ps.setString(4, p.getAddress());
            ps.setString(5, p.getType());
            ps.setString(6, p.getBhk());
            ps.setDouble(7, p.getRent());
            ps.setDouble(8, p.getArea());
            ps.setDouble(9, p.getPrice());
            ps.setString(10, p.getOwner());
            ps.setString(11, p.getContactNumber());
            ps.setString(12, p.getNotes());
            ps.setInt(13, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // UPDATE PROPERTY STATUS
    public boolean updateStatus(int id, String status) {

        String sql = "UPDATE properties SET status = ? WHERE id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // DELETE PROPERTY
    public boolean delete(int id) {

        String sql = "DELETE FROM properties WHERE id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // MAP DATABASE ROW TO PROPERTY OBJECT
    private Property mapProperty(ResultSet rs) throws Exception {

        Property p = new Property();

        p.setId(rs.getInt("id"));
        p.setTitle(rs.getString("title"));
        p.setCountry(rs.getString("country"));
        p.setCity(rs.getString("city"));
        p.setAddress(rs.getString("address"));
        p.setType(rs.getString("type"));
        p.setBhk(rs.getString("bhk"));
        p.setRent(rs.getDouble("rent"));
        p.setArea(rs.getDouble("area"));
        p.setPrice(rs.getDouble("price"));
        p.setOwner(rs.getString("owner"));
        p.setContactNumber(rs.getString("contact_number"));

        p.setOwnerId(rs.getInt("owner_id"));
        p.setBrokerId(rs.getInt("broker_id"));
        p.setCreatedBy(rs.getInt("created_by"));
        p.setCreatedByRole(rs.getString("created_by_role"));
        p.setNotes(rs.getString("notes"));
        p.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toString());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            p.setUpdatedAt(updatedAt.toString());
        }

        return p;
    }
}