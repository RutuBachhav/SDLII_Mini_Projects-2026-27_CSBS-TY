package com.lostlink;

import com.lostlink.Models.Report;
import java.util.Locale;

/** Literal, case-insensitive search. User input is never interpreted as regex or SQL. */
public final class ReportSearch {
  private ReportSearch() {}

  public static boolean includes(
      Report r, String query, String type, String category, String status) {
    String hay =
        (r.id()
                + " "
                + r.name()
                + " "
                + r.description()
                + " "
                + r.location()
                + " "
                + r.colour()
                + " "
                + r.reporter()
                + " "
                + r.category())
            .toLowerCase(Locale.ROOT);
    return hay.contains(query.trim().toLowerCase(Locale.ROOT))
        && ("All types".equals(type) || r.type().name().equals(type))
        && ("All categories".equals(category) || r.category().equals(category))
        && ("All statuses".equals(status) || r.status().name().equals(status));
  }
}
