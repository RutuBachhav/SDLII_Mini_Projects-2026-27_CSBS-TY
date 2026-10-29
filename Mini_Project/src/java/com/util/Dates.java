package com.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Dates {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private Dates() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE);
    }

    public static String formatTime(LocalTime time) {
        return time == null ? "" : time.format(TIME);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(STAMP);
    }

    public static String runtime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours <= 0) {
            return mins + "m";
        }
        if (mins == 0) {
            return hours + "h";
        }
        return hours + "h " + mins + "m";
    }

    public static String rupees(BigDecimal amount) {
        if (amount == null) {
            return "₹0";
        }
        return "₹" + String.format("%,.0f", amount);
    }
}
