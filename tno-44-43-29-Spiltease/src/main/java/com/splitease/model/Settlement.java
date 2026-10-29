package com.splitease.model;

import java.math.BigDecimal;

/** A real payment made to clear dues (table "settlements"). */
public class Settlement {

    private int id;
    private int groupId;
    private int fromUser;
    private String fromName;
    private int toUser;
    private String toName;
    private BigDecimal amount;
    private String note;
    private String settledAt;

    public Settlement() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

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

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getSettledAt() { return settledAt; }
    public void setSettledAt(String settledAt) { this.settledAt = settledAt; }
}
