package com.splitease.dao;

import com.splitease.model.Expense;
import com.splitease.model.Share;
import com.splitease.split.Money;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data access for tables "expenses" and "expense_splits". */
public class ExpenseDAO {

    /**
     * Saves the expense and all of its shares atomically:
     * either every row is written or none is (commit / rollback).
     */
    public int insert(Expense e, Map<Integer, Long> sharesInPaise) {
        String expSql = "INSERT INTO expenses (group_id, description, amount, paid_by, split_type, category, expense_date) "
                      + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String splitSql = "INSERT INTO expense_splits (expense_id, user_id, share_amount) VALUES (?, ?, ?)";
        Connection con = DBUtil.getConnection();
        try {
            con.setAutoCommit(false);
            int expenseId;
            try (PreparedStatement ps = con.prepareStatement(expSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, e.getGroupId());
                ps.setString(2, e.getDescription());
                ps.setBigDecimal(3, e.getAmount());
                ps.setInt(4, e.getPaidBy());
                ps.setString(5, e.getSplitType());
                ps.setString(6, e.getCategory());
                ps.setDate(7, Date.valueOf(e.getExpenseDate()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    expenseId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(splitSql)) {
                for (Map.Entry<Integer, Long> s : sharesInPaise.entrySet()) {
                    ps.setInt(1, expenseId);
                    ps.setInt(2, s.getKey());
                    ps.setBigDecimal(3, Money.toRupees(s.getValue()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
            return expenseId;
        } catch (SQLException ex) {
            GroupDAO.rollback(con);
            throw DBUtil.wrap(ex);
        } finally {
            GroupDAO.close(con);
        }
    }

    /** All expenses of a group (newest first) with their shares, using 2 queries instead of N+1. */
    public List<Expense> findByGroup(int groupId) {
        String expSql = "SELECT e.expense_id, e.group_id, e.description, e.amount, e.paid_by, u.name AS payer, "
                      + " e.split_type, e.category, DATE_FORMAT(e.expense_date, '%Y-%m-%d') AS d "
                      + "FROM expenses e JOIN users u ON u.user_id = e.paid_by "
                      + "WHERE e.group_id = ? ORDER BY e.expense_date DESC, e.expense_id DESC";
        String shareSql = "SELECT s.expense_id, s.user_id, u.name, s.share_amount FROM expense_splits s "
                        + "JOIN users u ON u.user_id = s.user_id JOIN expenses e ON e.expense_id = s.expense_id "
                        + "WHERE e.group_id = ? ORDER BY u.name";
        Map<Integer, Expense> byId = new LinkedHashMap<>();
        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(expSql)) {
                ps.setInt(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Expense e = new Expense();
                        e.setId(rs.getInt("expense_id"));
                        e.setGroupId(rs.getInt("group_id"));
                        e.setDescription(rs.getString("description"));
                        e.setAmount(rs.getBigDecimal("amount"));
                        e.setPaidBy(rs.getInt("paid_by"));
                        e.setPaidByName(rs.getString("payer"));
                        e.setSplitType(rs.getString("split_type"));
                        e.setCategory(rs.getString("category"));
                        e.setExpenseDate(rs.getString("d"));
                        byId.put(e.getId(), e);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(shareSql)) {
                ps.setInt(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Expense e = byId.get(rs.getInt(1));
                        if (e != null) {
                            e.getShares().add(new Share(rs.getInt(2), rs.getString(3), rs.getBigDecimal(4)));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw DBUtil.wrap(ex);
        }
        return new ArrayList<>(byId.values());
    }

    /** Returns the group id of the deleted expense, or -1 if it did not exist. */
    public int delete(int expenseId) {
        try (Connection con = DBUtil.getConnection()) {
            int groupId = -1;
            try (PreparedStatement ps = con.prepareStatement("SELECT group_id FROM expenses WHERE expense_id = ?")) {
                ps.setInt(1, expenseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        groupId = rs.getInt(1);
                    }
                }
            }
            if (groupId > 0) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM expenses WHERE expense_id = ?")) {
                    ps.setInt(1, expenseId);
                    ps.executeUpdate();     // shares are removed by ON DELETE CASCADE
                }
            }
            return groupId;
        } catch (SQLException ex) {
            throw DBUtil.wrap(ex);
        }
    }

    /** Sum of amounts each member PAID in a group: userId -> paise. */
    public Map<Integer, Long> totalPaidByMember(int groupId) {
        return sumQuery("SELECT paid_by, SUM(amount) FROM expenses WHERE group_id = ? GROUP BY paid_by", groupId);
    }

    /** Sum of shares each member OWES in a group: userId -> paise. */
    public Map<Integer, Long> totalOwedByMember(int groupId) {
        return sumQuery("SELECT s.user_id, SUM(s.share_amount) FROM expense_splits s "
                + "JOIN expenses e ON e.expense_id = s.expense_id WHERE e.group_id = ? GROUP BY s.user_id", groupId);
    }

    static Map<Integer, Long> sumQuery(String sql, int groupId) {
        Map<Integer, Long> map = new LinkedHashMap<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal v = rs.getBigDecimal(2);
                    map.put(rs.getInt(1), Money.toPaise(v));
                }
            }
        } catch (SQLException ex) {
            throw DBUtil.wrap(ex);
        }
        return map;
    }
}
