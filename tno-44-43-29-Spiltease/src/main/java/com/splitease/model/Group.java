package com.splitease.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** A group of people sharing costs: a trip, a flat, a club event (table "expense_groups"). */
public class Group {

    private int id;
    private String name;
    private String description;
    private String createdAt;
    private int memberCount;
    private int expenseCount;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private List<User> members = new ArrayList<>();

    public Group() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getExpenseCount() { return expenseCount; }
    public void setExpenseCount(int expenseCount) { this.expenseCount = expenseCount; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }
}
