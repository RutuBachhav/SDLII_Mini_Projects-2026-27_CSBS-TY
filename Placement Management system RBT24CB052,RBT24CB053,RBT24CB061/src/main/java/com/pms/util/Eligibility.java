package com.pms.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Pure eligibility rules shared by servlets and unit tests.
 * {@code eligibleBranches} is a comma-separated list; null/blank/ALL means
 * every branch is eligible.
 */
public final class Eligibility {

    private Eligibility() {}

    public static boolean isBranchEligible(String studentBranch, String eligibleBranches) {
        if (eligibleBranches == null || eligibleBranches.isBlank()
                || eligibleBranches.trim().equalsIgnoreCase("ALL")
                || eligibleBranches.trim().equals("*")) {
            return true;
        }
        if (studentBranch == null || studentBranch.isBlank()) {
            return false;
        }
        String mine = studentBranch.trim();
        for (String part : eligibleBranches.split(",")) {
            if (mine.equalsIgnoreCase(part.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCgpaEligible(BigDecimal studentCgpa, BigDecimal minimumCgpa) {
        if (minimumCgpa == null) {
            return true;
        }
        if (studentCgpa == null) {
            return false;
        }
        return studentCgpa.compareTo(minimumCgpa) >= 0;
    }

    /** Null deadline means open. A deadline of today is still open. */
    public static boolean isDeadlineOpen(Date deadline, LocalDate today) {
        if (deadline == null) {
            return true;
        }
        return !deadline.toLocalDate().isBefore(today);
    }

    private static final Pattern CGPA_FORMAT = Pattern.compile("^\\d{1,2}(\\.\\d{1,2})?$");
    private static final BigDecimal CGPA_MAX = new BigDecimal("10");

    /**
     * Parses and validates a CGPA form value. Accepts decimals from 0 to 10
     * with at most two decimal places; returns the value normalized to scale 2.
     *
     * @throws IllegalArgumentException with a user-facing message when invalid
     */
    public static BigDecimal parseCgpa(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("CGPA is required.");
        }
        if (!CGPA_FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("CGPA must be a number between 0 and 10 with at most two decimal places.");
        }
        BigDecimal cgpa;
        try {
            cgpa = new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("CGPA must be a number between 0 and 10 with at most two decimal places.");
        }
        if (cgpa.compareTo(BigDecimal.ZERO) < 0 || cgpa.compareTo(CGPA_MAX) > 0) {
            throw new IllegalArgumentException("CGPA must be between 0 and 10.");
        }
        return cgpa.setScale(2, RoundingMode.HALF_UP);
    }
}
