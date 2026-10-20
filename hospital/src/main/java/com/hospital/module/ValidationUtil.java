package com.hospital.module;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/** Shared server-side validation. All POST handlers must use these. */
public final class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TIME = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final Pattern PHONE = Pattern.compile("^[+\\d][\\d ()\\-]{5,20}$");

    private ValidationUtil() {
    }

    public static boolean isEmail(String v) {
        return v != null && v.length() <= 255 && EMAIL.matcher(v.trim()).matches();
    }

    public static boolean isName(String v) {
        return v != null && v.trim().length() >= 2 && v.trim().length() <= 120;
    }

    /** Minimum 8 chars with at least one letter and one digit. */
    public static boolean isPassword(String v) {
        if (v == null || v.length() < 8 || v.length() > 128) {
            return false;
        }
        boolean letter = false, digit = false;
        for (char c : v.toCharArray()) {
            if (Character.isLetter(c)) {
                letter = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            }
        }
        return letter && digit;
    }

    public static boolean isPhone(String v) {
        return v == null || v.trim().isEmpty() || PHONE.matcher(v.trim()).matches();
    }

    public static LocalDate parseDate(String v) {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(v.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalTime parseTime(String v) {
        if (v == null || !TIME.matcher(v.trim()).matches()) {
            return null;
        }
        try {
            return LocalTime.parse(v.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String clean(String v, int max) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t.length() > max ? t.substring(0, max) : t;
    }

    /** Small HTML escaper for JSP output (no new dependency). */
    public static String esc(String v) {
        if (v == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(v.length() + 16);
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
