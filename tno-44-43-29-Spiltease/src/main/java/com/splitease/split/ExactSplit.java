package com.splitease.split;

import com.splitease.dto.ParticipantInput;
import com.splitease.exception.ApiException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Each participant's rupee amount is entered by hand; they must add up to the bill. */
public class ExactSplit extends SplitStrategy {

    @Override
    public String getType() {
        return "EXACT";
    }

    @Override
    protected Map<Integer, Long> calculate(long totalPaise, List<ParticipantInput> participants) {
        Map<Integer, Long> result = new LinkedHashMap<>();
        long sum = 0;
        for (ParticipantInput p : participants) {
            BigDecimal v = p.getValue();
            if (v == null || v.signum() < 0) {
                throw ApiException.badRequest("Enter a valid amount for every participant");
            }
            long paise = Money.toPaise(v);
            result.put(p.getUserId(), paise);
            sum += paise;
        }
        if (sum != totalPaise) {
            throw ApiException.badRequest("Exact amounts add up to Rs " + Money.toRupees(sum)
                    + " but the bill is Rs " + Money.toRupees(totalPaise));
        }
        return result;
    }
}
