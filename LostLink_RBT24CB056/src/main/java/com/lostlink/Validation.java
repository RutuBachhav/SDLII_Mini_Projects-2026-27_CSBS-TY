package com.lostlink;

import com.lostlink.Models.*;
import java.time.LocalDate;
import java.util.Arrays;

public final class Validation {
  private Validation() {}

  public static String required(String value, String label) {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException(label + " is required.");
    String clean = value.trim();
    if (clean.length() > 4000)
      throw new IllegalArgumentException(label + " must be at most 4,000 characters.");
    return clean;
  }

  public static LocalDate date(String input) {
    try {
      LocalDate date = LocalDate.parse(input.trim());
      if (date.isAfter(LocalDate.now()))
        throw new IllegalArgumentException("Incident date cannot be in the future.");
      return date;
    } catch (java.time.format.DateTimeParseException | NullPointerException ex) {
      throw new IllegalArgumentException("Enter a real incident date in YYYY-MM-DD format.");
    }
  }

  public static void report(Report r) {
    if (r.type() == null) throw new IllegalArgumentException("Choose Lost or Found.");
    required(r.name(), "Item name");
    required(r.location(), "Location");
    required(r.reporter(), "Reporter name");
    required(r.contact(), "Contact details");
    if (!Arrays.asList(Models.CATEGORIES).contains(r.category()))
      throw new IllegalArgumentException("Choose a valid category.");
    if (r.date() == null || r.date().isAfter(LocalDate.now()))
      throw new IllegalArgumentException("Use an incident date of today or earlier.");
    if (r.colour() != null && r.colour().length() > 4000
        || r.description() != null && r.description().length() > 4000)
      throw new IllegalArgumentException(
          "Colour and description must be at most 4,000 characters.");
  }
}
