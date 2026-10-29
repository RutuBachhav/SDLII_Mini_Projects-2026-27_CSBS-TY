package com.splitease.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** JSON body of POST /api/groups/{id}/expenses */
public class ExpenseRequest {

    private String description;
    private BigDecimal amount;
    private int paidBy;
    private String splitType;
    private String category;
    private String expenseDate;
    private List<ParticipantInput> participants = new ArrayList<>();

    public ExpenseRequest() { }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public int getPaidBy() { return paidBy; }
    public void setPaidBy(int paidBy) { this.paidBy = paidBy; }

    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }

    public List<ParticipantInput> getParticipants() { return participants; }
    public void setParticipants(List<ParticipantInput> participants) { this.participants = participants; }
}
