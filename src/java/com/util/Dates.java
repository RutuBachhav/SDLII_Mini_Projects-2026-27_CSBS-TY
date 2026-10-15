package com.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Date formatting for the pages. JSTL's fmt tags predate java.time, so the
 * models expose ready-made labels instead and the JSPs stay free of scriptlets.
 */
public final class Dates {

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_TIME =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH);

    private Dates() {
    }

    public static String day(LocalDate date) {
        return date == null ? "" : DAY.format(date);
    }

    public static String dayTime(LocalDateTime time) {
        return time == null ? "" : DAY_TIME.format(time);
    }

    /** "2 hours ago" style text for notification rows and activity lists. */
    public static String ago(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Duration d = Duration.between(time, LocalDateTime.now());
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        long days = d.toDays();
        if (days < 30) {
            return days + (days == 1 ? " day ago" : " days ago");
        }
        return DAY.format(time);
    }
}
