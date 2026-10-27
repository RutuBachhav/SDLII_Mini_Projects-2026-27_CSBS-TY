package com.splitease.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** A single bill paid by one member and divided among several (table "expenses"). */
public class Expense {

    private int id;
    private int groupId;
    private String description;
    private BigDecimal amount;
    private int paidBy;
    private String paidByName;
    private String splitType;
    private String category;
    private String expenseDate;
    private List<Share> shares = new ArrayList<>();

    public Expense() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getPaidBy() { return paidBy; }
    public void setPaidBy(int paidBy) { this.paidBy = paidBy; }

    public String getPaidByName() { return paidByName; }
    public void setPaidByName(String paidByName) { this.paidByName = paidByName; }

    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }

    public List<Share> getShares() { return shares; }
    public void setShares(List<Share> shares) { this.shares = shares; }
}
