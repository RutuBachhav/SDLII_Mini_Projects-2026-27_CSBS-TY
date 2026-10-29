package com.splitease.model;

import java.math.BigDecimal;

/** One participant's portion of an expense (table "expense_splits"). */
public class Share {

    private int userId;
    private String userName;
    private BigDecimal amount;

    public Share() { }

    public Share(int userId, String userName, BigDecimal amount) {
        this.userId = userId;
        this.userName = userName;
        this.amount = amount;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
