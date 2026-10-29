package com.lostlink;

import static org.junit.jupiter.api.Assertions.*;

import com.lostlink.Models.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchingServiceTest {
  final MatchingService service = new MatchingService();

  Report r(
      long id,
      Type type,
      String category,
      String name,
      String colour,
      String location,
      LocalDate date,
      String description,
      Status status) {
    return new Report(
        id,
        type,
        name,
        category,
        colour,
        location,
        date,
        description,
        "A",
        "a@example.test",
        "",
        status);
  }

  final LocalDate day = LocalDate.of(2026, 1, 1);

  Report lost() {
    return r(
        1,
        Type.LOST,
        "Electronics",
        "Black phone",
        "Black",
        "Library",
        day,
        "Blue sticker",
        Status.ACTIVE);
  }

  Report found() {
    return r(
        2,
        Type.FOUND,
        "Electronics",
        "Black phone",
        "Black",
        "Library",
        day,
        "Blue sticker",
        Status.ACTIVE);
  }

  @Test
  void identicalFieldsScoreOneHundred() {
    assertEquals(100, service.compare(lost(), found()).orElseThrow().score(), 0.001);
  }

  @Test
  void weightedPartialScore() {
    Report f =
        r(
            2,
            Type.FOUND,
            "Electronics",
            "phone",
            "Black",
            "Library annex",
            day.plusDays(15),
            "",
            Status.ACTIVE);
    assertEquals(65, service.compare(lost(), f).orElseThrow().score(), 0.001);
  }

  @Test
  void missingOptionalFieldsEarnZeroWithoutRenormalization() {
    Report l = r(1, Type.LOST, "Keys", "Key", "", "Desk", day, "", Status.ACTIVE),
        f = r(2, Type.FOUND, "Keys", "Key", "", "Desk", day, "", Status.ACTIVE);
    assertEquals(75, service.compare(l, f).orElseThrow().score(), 0.001);
  }

  @Test
  void rejectsIncompatibleCategories() {
    assertTrue(
        service
            .compare(
                lost(),
                r(2, Type.FOUND, "Keys", "Phone", "Black", "Library", day, "", Status.ACTIVE))
            .isEmpty());
  }

  @Test
  void rejectsEarlierFoundDate() {
    assertTrue(
        service
            .compare(
                lost(),
                r(
                    2,
                    Type.FOUND,
                    "Electronics",
                    "Phone",
                    "Black",
                    "Library",
                    day.minusDays(1),
                    "",
                    Status.ACTIVE))
            .isEmpty());
  }

  @Test
  void excludesRecoveredAndReversedPairs() {
    assertTrue(service.compare(found(), lost()).isEmpty());
    assertTrue(
        service
            .compare(
                lost(),
                r(
                    2,
                    Type.FOUND,
                    "Electronics",
                    "Phone",
                    "Black",
                    "Library",
                    day,
                    "",
                    Status.RECOVERED))
            .isEmpty());
  }

  @Test
  void deduplicatesPairs() {
    assertEquals(1, service.suggest(List.of(lost(), found(), lost(), found())).size());
  }

  @Test
  void tokenNormalizationAndEmptyHandling() {
    assertEquals(1, MatchingService.similarity("Café, BLUE!", "blue cafe"));
    assertEquals(0, MatchingService.similarity("", ""));
    assertEquals(0, MatchingService.similarity(null, "blue"));
    assertEquals(0.5, MatchingService.similarity("phone", "black phone"));
  }

  @Test
  void dateScoreStopsAtZero() {
    Report f =
        r(
            2,
            Type.FOUND,
            "Electronics",
            "Black phone",
            "Black",
            "Library",
            day.plusDays(90),
            "Blue sticker",
            Status.ACTIVE);
    assertEquals(90, service.compare(lost(), f).orElseThrow().score());
  }
}
