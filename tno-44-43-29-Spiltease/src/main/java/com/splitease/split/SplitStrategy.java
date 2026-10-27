package com.splitease.split;

import com.splitease.dto.ParticipantInput;
import com.splitease.exception.ApiException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for the three ways an expense can be divided.
 * Each subclass decides how many paise every participant owes.
 * (Strategy pattern + inheritance + runtime polymorphism.)
 */
public abstract class SplitStrategy {

    /** Name stored in the expenses.split_type column. */
    public abstract String getType();

    /**
     * @param totalPaise   bill amount in paise
     * @param participants who shares the bill (and their amount / percent if needed)
     * @return userId -> share in paise; the shares always add up to totalPaise exactly
     */
    public final Map<Integer, Long> split(long totalPaise, List<ParticipantInput> participants) {
        if (totalPaise <= 0) {
            throw ApiException.badRequest("Amount must be greater than zero");
        }
        if (participants == null || participants.isEmpty()) {
            throw ApiException.badRequest("Select at least one participant");
        }
        Set<Integer> seen = new HashSet<>();
        for (ParticipantInput p : participants) {
            if (!seen.add(p.getUserId())) {
                throw ApiException.badRequest("A participant appears twice in the split");
            }
        }
        Map<Integer, Long> shares = calculate(totalPaise, participants);

        long sum = 0;
        for (long s : shares.values()) {
            sum += s;
        }
        if (sum != totalPaise) {   // safety net: should never happen
            throw ApiException.serverError("Split does not add up to the bill amount", null);
        }
        return shares;
    }

    /** Subclasses implement the actual division rule. */
    protected abstract Map<Integer, Long> calculate(long totalPaise, List<ParticipantInput> participants);

    /**
     * Gives the left-over paise (after integer division) one at a time to the
     * first participants, so Rs 100 / 3 becomes 33.34 + 33.33 + 33.33.
     */
    protected static Map<Integer, Long> distribute(long totalPaise, List<ParticipantInput> participants,
                                                   long[] baseShares) {
        long assigned = 0;
        for (long b : baseShares) {
            assigned += b;
        }
        long remainder = totalPaise - assigned;
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            long extra = (i < remainder) ? 1 : 0;
            result.put(participants.get(i).getUserId(), baseShares[i] + extra);
        }
        return result;
    }
}
