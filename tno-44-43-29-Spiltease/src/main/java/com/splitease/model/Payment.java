package com.splitease.model;

import java.math.BigDecimal;

/** A suggested transfer produced by the debt-simplification algorithm: "from pays to". */
public class Payment {

    private int fromUser;
    private String fromName;
    private int toUser;
    private String toName;
    private BigDecimal amount;

    public Payment() { }

    public Payment(int fromUser, int toUser, BigDecimal amount) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
    }

    public int getFromUser() { return fromUser; }
    public void setFromUser(int fromUser) { this.fromUser = fromUser; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public int getToUser() { return toUser; }
    public void setToUser(int toUser) { this.toUser = toUser; }

    public String getToName() { return toName; }
    public void setToName(String toName) { this.toName = toName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
