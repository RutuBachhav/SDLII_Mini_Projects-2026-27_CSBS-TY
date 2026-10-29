package com.lostlink;

import static org.junit.jupiter.api.Assertions.*;

import com.lostlink.Models.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ValidationTest {
  @Test
  void requiresNames() {
    assertThrows(IllegalArgumentException.class, () -> Validation.required("  ", "Name"));
    assertEquals("Sam", Validation.required(" Sam ", "Name"));
  }

  @Test
  void datesRejectInvalidAndFuture() {
    assertThrows(IllegalArgumentException.class, () -> Validation.date("2026-02-30"));
    assertThrows(IllegalArgumentException.class, () -> Validation.date("22/10/2026"));
    assertThrows(
        IllegalArgumentException.class,
        () -> Validation.date(LocalDate.now().plusDays(1).toString()));
    assertEquals(LocalDate.now(), Validation.date(LocalDate.now().toString()));
  }

  @Test
  void reportRequiresLocationAndContact() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Validation.report(
                new Report(
                    0,
                    Type.LOST,
                    "Keys",
                    "Keys",
                    "",
                    "",
                    LocalDate.now(),
                    "",
                    "A",
                    "a",
                    "",
                    Status.ACTIVE)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Validation.report(
                new Report(
                    0,
                    Type.LOST,
                    "Keys",
                    "Keys",
                    "",
                    "Library",
                    LocalDate.now(),
                    "",
                    "A",
                    "",
                    "",
                    Status.ACTIVE)));
  }
}
