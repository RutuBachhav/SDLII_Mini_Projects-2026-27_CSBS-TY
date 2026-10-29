package com.splitease.dao;

import com.splitease.model.Group;
import com.splitease.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for tables "expense_groups" and "group_members". */
public class GroupDAO {

    private static final String SUMMARY_SQL =
            "SELECT g.group_id, g.name, g.description, DATE_FORMAT(g.created_at, '%d %b %Y') AS created, "
          + " (SELECT COUNT(*) FROM group_members m WHERE m.group_id = g.group_id) AS members, "
          + " (SELECT COUNT(*) FROM expenses e WHERE e.group_id = g.group_id) AS expense_count, "
          + " (SELECT COALESCE(SUM(e.amount), 0) FROM expenses e WHERE e.group_id = g.group_id) AS total "
          + "FROM expense_groups g ";

    public List<Group> findAll() {
        List<Group> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(SUMMARY_SQL + "ORDER BY g.created_at DESC, g.group_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
        return list;
    }

    public Group findById(int id) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(SUMMARY_SQL + "WHERE g.group_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Group g = map(rs);
                g.setMembers(findMembers(con, id));
                return g;
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    public List<User> findMembers(int groupId) {
        try (Connection con = DBUtil.getConnection()) {
            return findMembers(con, groupId);
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    private List<User> findMembers(Connection con, int groupId) throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.email, u.phone FROM users u "
                   + "JOIN group_members m ON m.user_id = u.user_id WHERE m.group_id = ? ORDER BY u.name";
        List<User> members = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                }
            }
        }
        return members;
    }

    /** Creates the group and adds its first members in ONE transaction. */
    public int insert(String name, String description, List<Integer> memberIds) {
        String groupSql = "INSERT INTO expense_groups (name, description) VALUES (?, ?)";
        String memberSql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        Connection con = DBUtil.getConnection();
        try {
            con.setAutoCommit(false);
            int groupId;
            try (PreparedStatement ps = con.prepareStatement(groupSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, description);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    groupId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(memberSql)) {
                for (int userId : memberIds) {
                    ps.setInt(1, groupId);
                    ps.setInt(2, userId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
            return groupId;
        } catch (SQLException e) {
            rollback(con);
            throw DBUtil.wrap(e);
        } finally {
            close(con);
        }
    }

    public void addMember(int groupId, int userId) {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    public boolean isMember(int groupId, int userId) {
        String sql = "SELECT 1 FROM group_members WHERE group_id = ? AND user_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    public void delete(int groupId) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM expense_groups WHERE group_id = ?")) {
            ps.setInt(1, groupId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    private Group map(ResultSet rs) throws SQLException {
        Group g = new Group();
        g.setId(rs.getInt("group_id"));
        g.setName(rs.getString("name"));
        g.setDescription(rs.getString("description"));
        g.setCreatedAt(rs.getString("created"));
        g.setMemberCount(rs.getInt("members"));
        g.setExpenseCount(rs.getInt("expense_count"));
        g.setTotalSpent(rs.getBigDecimal("total"));
        return g;
    }

    static void rollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ignored) {
            // nothing more we can do
        }
    }

    static void close(Connection con) {
        try {
            con.setAutoCommit(true);
            con.close();
        } catch (SQLException ignored) {
            // connection already closed
        }
    }
}
