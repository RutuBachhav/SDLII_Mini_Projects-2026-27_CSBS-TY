package com.splitease.algo;

import com.splitease.model.Payment;
import com.splitease.split.Money;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Debt simplification ("Settle Up").
 *
 * Input : the net balance of every member in paise
 *         (positive = should receive money, negative = should pay money).
 * Output: a short list of payments that brings every balance to zero.
 *
 * Greedy min-cash-flow method:
 *   1. Put creditors in one max-heap and debtors in another (ordered by amount).
 *   2. Repeatedly match the biggest debtor with the biggest creditor and
 *      transfer min(debt, credit).
 *   3. Whoever is left with a non-zero balance goes back into its heap.
 * Every round clears at least one person, so the result has at most (n - 1)
 * payments for n members with a non-zero balance. Time: O(n log n).
 *
 * Note: finding the absolute minimum number of payments is NP-hard
 * (Verhoeff, 2004), so this greedy method is the standard practical choice.
 */
public final class DebtSimplifier {

    private DebtSimplifier() { }

    /** Small holder used inside the heaps. */
    private static final class Party {
        final int userId;
        long amount;          // always positive inside a heap

        Party(int userId, long amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }

    public static List<Payment> simplify(Map<Integer, Long> netBalances) {
        Comparator<Party> biggestFirst = (a, b) -> {
            int c = Long.compare(b.amount, a.amount);
            return c != 0 ? c : Integer.compare(a.userId, b.userId);   // deterministic order
        };
        PriorityQueue<Party> creditors = new PriorityQueue<>(biggestFirst);
        PriorityQueue<Party> debtors = new PriorityQueue<>(biggestFirst);

        long check = 0;
        for (Map.Entry<Integer, Long> e : netBalances.entrySet()) {
            long net = e.getValue();
            check += net;
            if (net > 0) {
                creditors.add(new Party(e.getKey(), net));
            } else if (net < 0) {
                debtors.add(new Party(e.getKey(), -net));
            }
        }
        if (check != 0) {
            throw new IllegalStateException("Balances do not sum to zero: " + check);
        }

        List<Payment> payments = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Party credit = creditors.poll();
            Party debit = debtors.poll();
            long x = Math.min(credit.amount, debit.amount);

            payments.add(new Payment(debit.userId, credit.userId, Money.toRupees(x)));

            credit.amount -= x;
            debit.amount -= x;
            if (credit.amount > 0) {
                creditors.add(credit);
            }
            if (debit.amount > 0) {
                debtors.add(debit);
            }
        }
        return payments;
    }
}
