package com.splitease.model;

import java.util.ArrayList;
import java.util.List;

/** Response of GET /api/groups/{id}/balances : balances + the simplified "settle up" plan. */
public class SettlementPlan {

    private List<Balance> balances = new ArrayList<>();
    private List<Payment> payments = new ArrayList<>();
    /** Payments needed if every pairwise debt were paid directly (before simplification). */
    private int pairwiseCount;
    /** Payments needed after simplification (always &lt;= members - 1). */
    private int simplifiedCount;
    private boolean settled;

    public SettlementPlan() { }

    public List<Balance> getBalances() { return balances; }
    public void setBalances(List<Balance> balances) { this.balances = balances; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public int getPairwiseCount() { return pairwiseCount; }
    public void setPairwiseCount(int pairwiseCount) { this.pairwiseCount = pairwiseCount; }

    public int getSimplifiedCount() { return simplifiedCount; }
    public void setSimplifiedCount(int simplifiedCount) { this.simplifiedCount = simplifiedCount; }

    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }
}
