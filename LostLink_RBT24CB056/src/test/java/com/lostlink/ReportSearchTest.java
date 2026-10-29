package com.lostlink;

import static org.junit.jupiter.api.Assertions.*;

import com.lostlink.Models.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReportSearchTest {
  Report r =
      new Report(
          12,
          Type.LOST,
          "Calculator [blue]",
          "Electronics",
          "Blue",
          "Library",
          LocalDate.now(),
          "Initials AK",
          "Sam",
          "sam@example.test",
          "",
          Status.ACTIVE);

  @Test
  void searchesTextLiterallyAcrossDetails() {
    assertTrue(ReportSearch.includes(r, " [BLUE] ", "All types", "All categories", "All statuses"));
    assertTrue(ReportSearch.includes(r, "initials", "LOST", "Electronics", "ACTIVE"));
    assertTrue(ReportSearch.includes(r, "sam", "All types", "All categories", "All statuses"));
    assertFalse(ReportSearch.includes(r, ".*", "All types", "All categories", "All statuses"));
  }

  @Test
  void combinesAllFilters() {
    assertTrue(ReportSearch.includes(r, "", "LOST", "Electronics", "ACTIVE"));
    assertFalse(ReportSearch.includes(r, "", "FOUND", "Electronics", "ACTIVE"));
    assertFalse(ReportSearch.includes(r, "", "LOST", "Keys", "ACTIVE"));
    assertFalse(ReportSearch.includes(r, "", "LOST", "Electronics", "RECOVERED"));
  }
}
