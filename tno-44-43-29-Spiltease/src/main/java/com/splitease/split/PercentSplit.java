package com.splitease.split;

import com.splitease.dto.ParticipantInput;
import com.splitease.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/** Each participant pays a percentage of the bill; percentages must total 100. */
public class PercentSplit extends SplitStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public String getType() {
        return "PERCENT";
    }

    @Override
    protected Map<Integer, Long> calculate(long totalPaise, List<ParticipantInput> participants) {
        BigDecimal totalPercent = BigDecimal.ZERO;
        long[] base = new long[participants.size()];
        for (int i = 0; i < participants.size(); i++) {
            BigDecimal pct = participants.get(i).getValue();
            if (pct == null || pct.signum() < 0) {
                throw ApiException.badRequest("Enter a valid percentage for every participant");
            }
            totalPercent = totalPercent.add(pct);
            // floor(total * pct / 100); leftover paise are handed out by distribute()
            base[i] = BigDecimal.valueOf(totalPaise).multiply(pct)
                    .divide(HUNDRED, 0, RoundingMode.DOWN).longValue();
        }
        if (totalPercent.compareTo(HUNDRED) != 0) {
            throw ApiException.badRequest("Percentages add up to " + totalPercent.stripTrailingZeros().toPlainString()
                    + "% - they must total exactly 100%");
        }
        return distribute(totalPaise, participants, base);
    }
}
