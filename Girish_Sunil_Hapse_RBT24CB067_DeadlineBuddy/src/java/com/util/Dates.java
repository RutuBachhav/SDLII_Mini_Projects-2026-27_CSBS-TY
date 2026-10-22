package com.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Calendar helpers.
 *
 * Everything in the application compares calendar dates, never instants: a task
 * due "today" is due on today's date in the machine's own zone, and stays due
 * today until midnight. That is what a student means, and it is what keeps a
 * task from looking overdue at 05:30 because the server stored a UTC timestamp.
 *
 * due_date is a DATE and due_time a TIME; neither carries a zone, so no
 * conversion happens on the way in or out of MySQL.
 */
public final class Dates {

    /** What &lt;input type="date"&gt; and &lt;input type="time"&gt; post. */
    public static final DateTimeFormatter HTML_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter HTML_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter NICE_DATE = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter NICE_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private Dates() {
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /** Parses a required yyyy-MM-dd value, reporting the field on failure. */
    public static LocalDate requireDate(String raw, String field, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new ValidationException(field, label + " is required.");
        }
        try {
            return LocalDate.parse(raw.trim(), HTML_DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, label + " must be a real date (yyyy-mm-dd).");
        }
    }

    /** Parses an optional HH:mm value; returns null when the field is blank. */
    public static LocalTime optionalTime(String raw, String field, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String v = raw.trim();
        // Some browsers post seconds for <input type="time">.
        if (v.length() > 5) {
            v = v.substring(0, 5);
        }
        try {
            return LocalTime.parse(v, HTML_TIME);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, label + " must be a time like 17:30.");
        }
    }

    /** Whole calendar days from today to the given date; negative when past. */
    public static long daysFromToday(LocalDate date) {
        return date == null ? 0 : ChronoUnit.DAYS.between(today(), date);
    }

    public static String formatDate(LocalDate d) {
        return d == null ? "" : d.format(NICE_DATE);
    }

    public static String formatTime(LocalTime t) {
        return t == null ? "" : t.format(NICE_TIME);
    }

    public static String formatDateTime(LocalDateTime t) {
        return t == null ? "" : t.format(NICE_DATE) + " " + t.format(NICE_TIME);
    }

    /** "today", "tomorrow", "in 4 days", "3 days ago" - for deadline labels. */
    public static String relative(LocalDate date) {
        long days = daysFromToday(date);
        if (days == 0) {
            return "today";
        }
        if (days == 1) {
            return "tomorrow";
        }
        if (days == -1) {
            return "yesterday";
        }
        return days > 0 ? "in " + days + " days" : Math.abs(days) + " days ago";
    }

    /** Guards against a clock skew making "created 2 seconds from now". */
    public static boolean isFuture(LocalDateTime t) {
        return t != null && Duration.between(now(), t).toMinutes() > 1;
    }
}
