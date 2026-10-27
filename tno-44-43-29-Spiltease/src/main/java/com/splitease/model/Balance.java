package com.splitease.model;

import java.math.BigDecimal;

/**
 * Where one member stands in a group.
 * net &gt; 0 : the group owes this member money (creditor)
 * net &lt; 0 : this member owes the group money (debtor)
 */
public class Balance {

    private int userId;
    private String userName;
    private BigDecimal paid;
    private BigDecimal owed;
    private BigDecimal net;

    public Balance() { }

    public Balance(int userId, String userName, BigDecimal paid, BigDecimal owed, BigDecimal net) {
        this.userId = userId;
        this.userName = userName;
        this.paid = paid;
        this.owed = owed;
        this.net = net;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public BigDecimal getPaid() { return paid; }
    public void setPaid(BigDecimal paid) { this.paid = paid; }

    public BigDecimal getOwed() { return owed; }
    public void setOwed(BigDecimal owed) { this.owed = owed; }

    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }
}
