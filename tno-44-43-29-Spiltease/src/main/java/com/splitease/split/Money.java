package com.splitease.split;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * All arithmetic is done in paise (long) so that no rupee is ever lost to
 * floating-point rounding. Rs 100.50 is stored as 10050.
 */
public final class Money {

    private Money() { }

    public static long toPaise(BigDecimal rupees) {
        return rupees.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    public static BigDecimal toRupees(long paise) {
        return BigDecimal.valueOf(paise, 2);
    }
}
