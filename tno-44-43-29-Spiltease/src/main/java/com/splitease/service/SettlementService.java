package com.splitease.service;

import com.splitease.dao.SettlementDAO;
import com.splitease.exception.ApiException;
import com.splitease.model.Settlement;
import com.splitease.split.Money;
import java.math.BigDecimal;
import java.util.List;

/** Records real-world payments ("Mark as paid"). */
public class SettlementService {

    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final GroupService groupService = new GroupService();

    public List<Settlement> listSettlements(int groupId) {
        groupService.getGroup(groupId);
        return settlementDAO.findByGroup(groupId);
    }

    public Settlement record(int groupId, Settlement s) {
        groupService.getGroup(groupId);
        if (s == null) {
            throw ApiException.badRequest("Request body is missing");
        }
        if (s.getFromUser() == s.getToUser()) {
            throw ApiException.badRequest("Payer and receiver must be different people");
        }
        BigDecimal amount = s.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw ApiException.badRequest("Settlement amount must be greater than zero");
        }
        groupService.requireMember(groupId, s.getFromUser(), "Payer");
        groupService.requireMember(groupId, s.getToUser(), "Receiver");

        s.setGroupId(groupId);
        s.setAmount(Money.toRupees(Money.toPaise(amount)));
        String note = UserService.trim(s.getNote());
        s.setNote(note.isEmpty() ? "Settled via SplitEase" : note);
        int id = settlementDAO.insert(s);
        return settlementDAO.findByGroup(groupId).stream()
                .filter(x -> x.getId() == id).findFirst().orElse(s);
    }
}
