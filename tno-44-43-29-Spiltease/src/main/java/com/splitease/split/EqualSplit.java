package com.splitease.split;

import com.splitease.dto.ParticipantInput;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Everyone pays the same: Rs 900 among 3 = Rs 300 each. */
public class EqualSplit extends SplitStrategy {

    @Override
    public String getType() {
        return "EQUAL";
    }

    @Override
    protected Map<Integer, Long> calculate(long totalPaise, List<ParticipantInput> participants) {
        int n = participants.size();
        long[] base = new long[n];
        Arrays.fill(base, totalPaise / n);
        return distribute(totalPaise, participants, base);
    }
}
