package com.splitease.dao;

import com.splitease.model.Settlement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Data access for table "settlements". */
public class SettlementDAO {

    public int insert(Settlement s) {
        String sql = "INSERT INTO settlements (group_id, from_user, to_user, amount, note) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getGroupId());
            ps.setInt(2, s.getFromUser());
            ps.setInt(3, s.getToUser());
            ps.setBigDecimal(4, s.getAmount());
            ps.setString(5, s.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }

    public List<Settlement> findByGroup(int groupId) {
        String sql = "SELECT s.settlement_id, s.group_id, s.from_user, f.name, s.to_user, t.name, s.amount, s.note, "
                   + " DATE_FORMAT(s.settled_at, '%d %b %Y, %h:%i %p') "
                   + "FROM settlements s JOIN users f ON f.user_id = s.from_user JOIN users t ON t.user_id = s.to_user "
                   + "WHERE s.group_id = ? ORDER BY s.settled_at DESC, s.settlement_id DESC";
        List<Settlement> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Settlement s = new Settlement();
                    s.setId(rs.getInt(1));
                    s.setGroupId(rs.getInt(2));
                    s.setFromUser(rs.getInt(3));
                    s.setFromName(rs.getString(4));
                    s.setToUser(rs.getInt(5));
                    s.setToName(rs.getString(6));
                    s.setAmount(rs.getBigDecimal(7));
                    s.setNote(rs.getString(8));
                    s.setSettledAt(rs.getString(9));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
        return list;
    }

    /** Total each member has PAID OUT as settlements: userId -> paise. */
    public Map<Integer, Long> totalSentByMember(int groupId) {
        return ExpenseDAO.sumQuery(
                "SELECT from_user, SUM(amount) FROM settlements WHERE group_id = ? GROUP BY from_user", groupId);
    }

    /** Total each member has RECEIVED as settlements: userId -> paise. */
    public Map<Integer, Long> totalReceivedByMember(int groupId) {
        return ExpenseDAO.sumQuery(
                "SELECT to_user, SUM(amount) FROM settlements WHERE group_id = ? GROUP BY to_user", groupId);
    }
}
