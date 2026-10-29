package com.splitease.dao;

import com.splitease.model.DashboardStats;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** One query that feeds the dashboard cards. */
public class StatsDAO {

    public DashboardStats load() {
        String sql = "SELECT (SELECT COUNT(*) FROM users), (SELECT COUNT(*) FROM expense_groups), "
                   + "(SELECT COUNT(*) FROM expenses), (SELECT COALESCE(SUM(amount),0) FROM expenses), "
                   + "(SELECT COALESCE(SUM(amount),0) FROM settlements)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            DashboardStats s = new DashboardStats();
            if (rs.next()) {
                s.setUsers(rs.getInt(1));
                s.setGroups(rs.getInt(2));
                s.setExpenses(rs.getInt(3));
                s.setTotalSpent(rs.getBigDecimal(4));
                s.setTotalSettled(rs.getBigDecimal(5));
            }
            return s;
        } catch (SQLException e) {
            throw DBUtil.wrap(e);
        }
    }
}
