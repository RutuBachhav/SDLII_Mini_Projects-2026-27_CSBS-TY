package com.splitease.service;

import com.splitease.dao.ExpenseDAO;
import com.splitease.dto.ExpenseRequest;
import com.splitease.dto.ParticipantInput;
import com.splitease.exception.ApiException;
import com.splitease.model.Expense;
import com.splitease.split.Money;
import com.splitease.split.SplitFactory;
import com.splitease.split.SplitStrategy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Validates and records expenses. */
public class ExpenseService {

    public static final Set<String> CATEGORIES =
            Set.of("Food", "Travel", "Stay", "Shopping", "Entertainment", "Utilities", "General");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");   // Rs 10 lakh per bill

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final GroupService groupService = new GroupService();

    public List<Expense> listExpenses(int groupId) {
        groupService.getGroup(groupId);
        return expenseDAO.findByGroup(groupId);
    }

    public Expense addExpense(int groupId, ExpenseRequest req) {
        groupService.getGroup(groupId);                       // 404 if group missing
        if (req == null) {
            throw ApiException.badRequest("Request body is missing");
        }

        String desc = UserService.trim(req.getDescription());
        if (desc.isEmpty() || desc.length() > 150) {
            throw ApiException.badRequest("Description is required (max 150 characters)");
        }
        BigDecimal amount = req.getAmount();
        if (amount == null || amount.signum() <= 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw ApiException.badRequest("Amount must be between Rs 0.01 and Rs 10,00,000");
        }
        if (amount.scale() > 2 && amount.stripTrailingZeros().scale() > 2) {
            throw ApiException.badRequest("Amount can have at most 2 decimal places");
        }
        String category = UserService.trim(req.getCategory());
        if (category.isEmpty()) {
            category = "General";
        }
        if (!CATEGORIES.contains(category)) {
            throw ApiException.badRequest("Unknown category: " + category);
        }
        String date = UserService.trim(req.getExpenseDate());
        try {
            date = date.isEmpty() ? LocalDate.now().toString() : LocalDate.parse(date).toString();
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("Date must be in yyyy-mm-dd format");
        }

        groupService.requireMember(groupId, req.getPaidBy(), "Payer");
        for (ParticipantInput p : req.getParticipants()) {
            groupService.requireMember(groupId, p.getUserId(), "Participant");
        }

        // Polymorphism: the factory returns EqualSplit, ExactSplit or PercentSplit
        SplitStrategy strategy = SplitFactory.forType(req.getSplitType());
        long totalPaise = Money.toPaise(amount);
        Map<Integer, Long> shares = strategy.split(totalPaise, req.getParticipants());

        Expense e = new Expense();
        e.setGroupId(groupId);
        e.setDescription(desc);
        e.setAmount(Money.toRupees(totalPaise));
        e.setPaidBy(req.getPaidBy());
        e.setSplitType(strategy.getType());
        e.setCategory(category);
        e.setExpenseDate(date);
        int id = expenseDAO.insert(e, shares);

        return expenseDAO.findByGroup(groupId).stream()
                .filter(x -> x.getId() == id).findFirst()
                .orElseThrow(() -> ApiException.serverError("Expense was not saved", null));
    }

    public void deleteExpense(int expenseId) {
        if (expenseDAO.delete(expenseId) < 0) {
            throw ApiException.notFound("Expense " + expenseId + " not found");
        }
    }
}
