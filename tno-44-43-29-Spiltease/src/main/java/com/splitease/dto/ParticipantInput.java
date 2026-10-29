package com.splitease.dto;

import java.math.BigDecimal;

/**
 * One participant in an "add expense" request.
 * value is ignored for EQUAL, is a rupee amount for EXACT and a percentage for PERCENT.
 */
public class ParticipantInput {

    private int userId;
    private BigDecimal value;

    public ParticipantInput() { }

    public ParticipantInput(int userId, BigDecimal value) {
        this.userId = userId;
        this.value = value;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}
