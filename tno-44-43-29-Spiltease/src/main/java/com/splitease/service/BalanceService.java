package com.splitease.service;

import com.splitease.algo.DebtSimplifier;
import com.splitease.dao.ExpenseDAO;
import com.splitease.dao.GroupDAO;
import com.splitease.dao.SettlementDAO;
import com.splitease.exception.ApiException;
import com.splitease.model.Balance;
import com.splitease.model.Expense;
import com.splitease.model.Payment;
import com.splitease.model.Settlement;
import com.splitease.model.Share;
import com.splitease.model.SettlementPlan;
import com.splitease.model.User;
import com.splitease.split.Money;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes every member's balance and the simplified settle-up plan.
 *
 *   net = (paid for expenses) - (own share of expenses)
 *       + (settlements paid out) - (settlements received)
 */
public class BalanceService {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final GroupDAO groupDAO = new GroupDAO();

    public SettlementPlan buildPlan(int groupId) {
        if (groupDAO.findById(groupId) == null) {
            throw ApiException.notFound("Group " + groupId + " not found");
        }
        List<User> members = groupDAO.findMembers(groupId);
        Map<Integer, Long> paid = expenseDAO.totalPaidByMember(groupId);
        Map<Integer, Long> owed = expenseDAO.totalOwedByMember(groupId);
        Map<Integer, Long> sent = settlementDAO.totalSentByMember(groupId);
        Map<Integer, Long> received = settlementDAO.totalReceivedByMember(groupId);

        SettlementPlan plan = new SettlementPlan();
        Map<Integer, Long> net = new LinkedHashMap<>();
        Map<Integer, String> names = new HashMap<>();
        for (User m : members) {
            int id = m.getId();
            long p = paid.getOrDefault(id, 0L);
            long o = owed.getOrDefault(id, 0L);
            long n = p - o + sent.getOrDefault(id, 0L) - received.getOrDefault(id, 0L);
            net.put(id, n);
            names.put(id, m.getName());
            plan.getBalances().add(new Balance(id, m.getName(),
                    Money.toRupees(p), Money.toRupees(o), Money.toRupees(n)));
        }

        List<Payment> payments = DebtSimplifier.simplify(net);
        for (Payment pay : payments) {
            pay.setFromName(names.get(pay.getFromUser()));
            pay.setToName(names.get(pay.getToUser()));
        }
        plan.setPayments(payments);
        plan.setSimplifiedCount(payments.size());
        plan.setPairwiseCount(countPairwiseDebts(groupId));
        plan.setSettled(payments.isEmpty());
        return plan;
    }

    /**
     * How many payments would be needed WITHOUT simplification: every debtor
     * pays every person who paid a bill for them (opposite debts between the
     * same two people are netted). Used to show the benefit of the algorithm.
     */
    int countPairwiseDebts(int groupId) {
        Map<String, Long> owe = new HashMap<>();      // key "a>b" = a owes b (paise)
        for (Expense e : expenseDAO.findByGroup(groupId)) {
            for (Share s : e.getShares()) {
                if (s.getUserId() != e.getPaidBy()) {
                    owe.merge(s.getUserId() + ">" + e.getPaidBy(), Money.toPaise(s.getAmount()), Long::sum);
                }
            }
        }
        for (Settlement st : settlementDAO.findByGroup(groupId)) {
            owe.merge(st.getFromUser() + ">" + st.getToUser(), -Money.toPaise(st.getAmount()), Long::sum);
        }
        Map<String, Long> pairNet = new HashMap<>();
        for (Map.Entry<String, Long> e : owe.entrySet()) {
            String[] ab = e.getKey().split(">");
            int a = Integer.parseInt(ab[0]);
            int b = Integer.parseInt(ab[1]);
            String key = Math.min(a, b) + "-" + Math.max(a, b);
            long signed = (a < b) ? e.getValue() : -e.getValue();
            pairNet.merge(key, signed, Long::sum);
        }
        int count = 0;
        for (long v : pairNet.values()) {
            if (v != 0) {
                count++;
            }
        }
        return count;
    }
}
