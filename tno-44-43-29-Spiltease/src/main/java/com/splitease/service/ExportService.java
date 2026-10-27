package com.splitease.service;

import com.splitease.model.Balance;
import com.splitease.model.Expense;
import com.splitease.model.Group;
import com.splitease.model.Payment;
import com.splitease.model.SettlementPlan;
import com.splitease.model.Share;

/** Builds a CSV summary of a group that opens directly in Excel. */
public class ExportService {

    private final GroupService groupService = new GroupService();
    private final ExpenseService expenseService = new ExpenseService();
    private final BalanceService balanceService = new BalanceService();

    public String groupCsv(int groupId) {
        Group g = groupService.getGroup(groupId);
        SettlementPlan plan = balanceService.buildPlan(groupId);
        StringBuilder sb = new StringBuilder();
        sb.append("Group,").append(csv(g.getName())).append('\n');
        sb.append("Total spent (Rs),").append(g.getTotalSpent()).append("\n\n");

        sb.append("EXPENSES\nDate,Description,Category,Paid by,Amount (Rs),Split type,Shares\n");
        for (Expense e : expenseService.listExpenses(groupId)) {
            StringBuilder shares = new StringBuilder();
            for (Share s : e.getShares()) {
                if (shares.length() > 0) {
                    shares.append("; ");
                }
                shares.append(s.getUserName()).append(' ').append(s.getAmount());
            }
            sb.append(e.getExpenseDate()).append(',').append(csv(e.getDescription())).append(',')
              .append(e.getCategory()).append(',').append(csv(e.getPaidByName())).append(',')
              .append(e.getAmount()).append(',').append(e.getSplitType()).append(',')
              .append(csv(shares.toString())).append('\n');
        }

        sb.append("\nBALANCES\nMember,Paid (Rs),Share (Rs),Net (Rs)\n");
        for (Balance b : plan.getBalances()) {
            sb.append(csv(b.getUserName())).append(',').append(b.getPaid()).append(',')
              .append(b.getOwed()).append(',').append(b.getNet()).append('\n');
        }

        sb.append("\nSETTLE UP (").append(plan.getSimplifiedCount()).append(" payments instead of ")
          .append(plan.getPairwiseCount()).append(")\nFrom,To,Amount (Rs)\n");
        for (Payment p : plan.getPayments()) {
            sb.append(csv(p.getFromName())).append(',').append(csv(p.getToName())).append(',')
              .append(p.getAmount()).append('\n');
        }
        return sb.toString();
    }

    private static String csv(String s) {
        if (s == null) {
            return "";
        }
        return (s.contains(",") || s.contains("\"")) ? '"' + s.replace("\"", "\"\"") + '"' : s;
    }
}
