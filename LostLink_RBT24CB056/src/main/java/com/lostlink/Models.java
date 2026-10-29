package com.lostlink;

import java.time.LocalDate;

/** Immutable data passed between the repository, services and Swing views. */
public final class Models {
  private Models() {}

  public enum Type {
    LOST,
    FOUND
  }

  public enum Status {
    ACTIVE,
    RECOVERED
  }

  public enum ClaimStatus {
    PENDING,
    APPROVED,
    REJECTED
  }

  public static final String[] CATEGORIES = {
    "Electronics",
    "Books & Stationery",
    "Bags",
    "Clothing",
    "Keys",
    "Cards & IDs",
    "Accessories",
    "Other"
  };

  public record Report(
      long id,
      Type type,
      String name,
      String category,
      String colour,
      String location,
      LocalDate date,
      String description,
      String reporter,
      String contact,
      String photo,
      Status status) {
    @Override
    public String toString() {
      return "#" + id + " · " + name;
    }
  }

  public record Claim(
      long id,
      long foundId,
      Long lostId,
      String claimant,
      String contact,
      String details,
      ClaimStatus status,
      String created) {}

  public record Match(Report lost, Report found, double score, String explanation) {}
}
