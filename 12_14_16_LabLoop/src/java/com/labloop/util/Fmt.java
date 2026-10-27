package com.labloop.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Small formatting helpers so JSP pages can show java.time values nicely. */
public final class Fmt {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private Fmt() { }

    public static String date(LocalDate d) { return d == null ? "—" : DATE.format(d); }
    public static String dateTime(LocalDateTime t) { return t == null ? "—" : DATE_TIME.format(t); }
    /** Value format expected by an HTML &lt;input type="datetime-local"&gt;. */
    public static String input(LocalDateTime t) { return t == null ? "" : INPUT.format(t); }
}
