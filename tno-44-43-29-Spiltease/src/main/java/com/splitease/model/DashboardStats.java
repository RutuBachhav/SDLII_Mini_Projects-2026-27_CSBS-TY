package com.splitease.model;

import java.math.BigDecimal;

/** Totals shown on the dashboard cards. */
public class DashboardStats {

    private int users;
    private int groups;
    private int expenses;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal totalSettled = BigDecimal.ZERO;

    public DashboardStats() { }

    public int getUsers() { return users; }
    public void setUsers(int users) { this.users = users; }

    public int getGroups() { return groups; }
    public void setGroups(int groups) { this.groups = groups; }

    public int getExpenses() { return expenses; }
    public void setExpenses(int expenses) { this.expenses = expenses; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public BigDecimal getTotalSettled() { return totalSettled; }
    public void setTotalSettled(BigDecimal totalSettled) { this.totalSettled = totalSettled; }
}
