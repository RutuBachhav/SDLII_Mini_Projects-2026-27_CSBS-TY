package com.pms;

import com.pms.util.Eligibility;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EligibilityTest {

    @Test
    void blankBranchesMeansAllEligible() {
        assertTrue(Eligibility.isBranchEligible("Computer Science", null));
        assertTrue(Eligibility.isBranchEligible("Mechanical", ""));
        assertTrue(Eligibility.isBranchEligible("Civil", "ALL"));
    }

    @Test
    void commaSeparatedBranchMatchIsCaseInsensitive() {
        assertTrue(Eligibility.isBranchEligible("cse", "CSE, IT"));
        assertTrue(Eligibility.isBranchEligible("IT", "CSE,IT"));
        assertFalse(Eligibility.isBranchEligible("Mechanical", "CSE, IT"));
    }

    @Test
    void cgpaRules() {
        assertTrue(Eligibility.isCgpaEligible(new BigDecimal("8.50"), null));
        assertTrue(Eligibility.isCgpaEligible(new BigDecimal("7.00"), new BigDecimal("7.00")));
        assertFalse(Eligibility.isCgpaEligible(new BigDecimal("6.99"), new BigDecimal("7.00")));
        assertFalse(Eligibility.isCgpaEligible(null, new BigDecimal("6.00")));
    }

    @Test
    void deadlineRules() {
        LocalDate today = LocalDate.of(2026, 10, 12);
        assertTrue(Eligibility.isDeadlineOpen(null, today));
        assertTrue(Eligibility.isDeadlineOpen(Date.valueOf(today), today));
        assertTrue(Eligibility.isDeadlineOpen(Date.valueOf(today.plusDays(1)), today));
        assertFalse(Eligibility.isDeadlineOpen(Date.valueOf(today.minusDays(1)), today));
    }

    @Test
    void parseCgpaAcceptsBoundariesAndNormalizesScale() {
        assertEquals(new BigDecimal("8.50"), Eligibility.parseCgpa("8.5"));
        assertEquals(new BigDecimal("10.00"), Eligibility.parseCgpa("10"));
        assertEquals(new BigDecimal("0.00"), Eligibility.parseCgpa("0"));
        assertEquals(new BigDecimal("7.25"), Eligibility.parseCgpa(" 7.25 "));
    }

    @Test
    void parseCgpaRejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa("10.01"));
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa("-0.5"));
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa("99"));
    }

    @Test
    void parseCgpaRejectsBadFormat() {
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa(""));
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa(null));
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa("abc"));
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa("8.567"));
        assertThrows(IllegalArgumentException.class, () -> Eligibility.parseCgpa("8,5"));
    }
}
