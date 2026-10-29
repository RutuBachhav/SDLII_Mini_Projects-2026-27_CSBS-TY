package com.lostlink;

import com.lostlink.Models.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** All queries use short-lived connections; multi-record recovery is one atomic transaction. */
public final class Repository {
  private final Database db;

  public Repository(Database db) {
    this.db = db;
  }

  private static String clean(String s) {
    return s == null ? "" : s.trim();
  }

  private Report read(ResultSet r) throws SQLException {
    return new Report(
        r.getLong("id"),
        Type.valueOf(r.getString("type")),
        r.getString("name"),
        r.getString("category"),
        r.getString("colour"),
        r.getString("location"),
        LocalDate.parse(r.getString("incident_date")),
        r.getString("description"),
        r.getString("reporter"),
        r.getString("contact"),
        r.getString("photo"),
        Status.valueOf(r.getString("status")));
  }

  public List<Report> reports() throws SQLException {
    List<Report> rows = new ArrayList<>();
    try (Connection c = db.connect();
        PreparedStatement p = c.prepareStatement("SELECT * FROM reports ORDER BY id DESC");
        ResultSet rs = p.executeQuery()) {
      while (rs.next()) rows.add(read(rs));
    }
    return rows;
  }

  private long insert(Connection c, Report r) throws SQLException {
    try (PreparedStatement p =
        c.prepareStatement(
            "INSERT INTO"
                + " reports(type,name,category,colour,location,incident_date,description,reporter,contact,photo)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      bind(p, r);
      p.executeUpdate();
      try (ResultSet keys = p.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    }
    throw new SQLException("Could not obtain the new report number.");
  }

  private void bind(PreparedStatement p, Report r) throws SQLException {
    p.setString(1, r.type().name());
    p.setString(2, clean(r.name()));
    p.setString(3, r.category());
    p.setString(4, clean(r.colour()));
    p.setString(5, clean(r.location()));
    p.setString(6, r.date().toString());
    p.setString(7, clean(r.description()));
    p.setString(8, clean(r.reporter()));
    p.setString(9, clean(r.contact()));
    p.setString(10, clean(r.photo()));
  }

  public long save(Report r) throws SQLException {
    Validation.report(r);
    try (Connection c = db.connect()) {
      if (r.id() == 0) return insert(c, r);
      c.setAutoCommit(false);
      try {
        Report old = get(c, r.id());
        if (old.status() != Status.ACTIVE)
          throw new IllegalArgumentException(
              "Recovered reports are locked to preserve the recovery record.");
        try (PreparedStatement p =
            c.prepareStatement("SELECT count(*) FROM claims WHERE found_id=? OR lost_id=?")) {
          p.setLong(1, r.id());
          p.setLong(2, r.id());
          try (ResultSet rs = p.executeQuery()) {
            if (rs.next()
                && rs.getInt(1) > 0
                && (old.type() != r.type()
                    || !old.category().equals(r.category())
                    || !old.date().equals(r.date())))
              throw new IllegalArgumentException(
                  "Type, category and date are locked once a report has a claim.");
          }
        }
        try (PreparedStatement p =
            c.prepareStatement(
                "UPDATE reports SET"
                    + " type=?,name=?,category=?,colour=?,location=?,incident_date=?,description=?,reporter=?,contact=?,photo=?"
                    + " WHERE id=?")) {
          bind(p, r);
          p.setLong(11, r.id());
          p.executeUpdate();
        }
        c.commit();
        return r.id();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  private Report get(Connection c, long id) throws SQLException {
    try (PreparedStatement p = c.prepareStatement("SELECT * FROM reports WHERE id=?")) {
      p.setLong(1, id);
      try (ResultSet rs = p.executeQuery()) {
        if (rs.next()) return read(rs);
      }
    }
    throw new IllegalArgumentException(
        "Report #" + id + " no longer exists. Refresh and try again.");
  }

  public void delete(long id) throws SQLException {
    try (Connection c = db.connect();
        PreparedStatement p =
            c.prepareStatement(
                "DELETE FROM reports WHERE id=? AND status='ACTIVE' AND NOT EXISTS(SELECT 1 FROM"
                    + " claims WHERE found_id=? OR lost_id=?)")) {
      p.setLong(1, id);
      p.setLong(2, id);
      p.setLong(3, id);
      if (p.executeUpdate() != 1)
        throw new IllegalArgumentException(
            "Reports with claims or recovery history cannot be deleted. This preserves the audit"
                + " trail.");
    }
  }

  private static Long nullableLong(ResultSet rs, String column) throws SQLException {
    long value = rs.getLong(column);
    return rs.wasNull() ? null : value;
  }

  public List<Claim> claims() throws SQLException {
    List<Claim> rows = new ArrayList<>();
    try (Connection c = db.connect();
        PreparedStatement p = c.prepareStatement("SELECT * FROM claims ORDER BY id DESC");
        ResultSet rs = p.executeQuery()) {
      while (rs.next())
        rows.add(
            new Claim(
                rs.getLong("id"),
                rs.getLong("found_id"),
                nullableLong(rs, "lost_id"),
                rs.getString("claimant"),
                rs.getString("contact"),
                rs.getString("details"),
                ClaimStatus.valueOf(rs.getString("status")),
                rs.getString("created_at")));
    }
    return rows;
  }

  public void claim(long foundId, Long lostId, String claimant, String contact, String details)
      throws SQLException {
    claimant = Validation.required(claimant, "Claimant name");
    contact = Validation.required(contact, "Contact details").toLowerCase(Locale.ROOT);
    details = Validation.required(details, "Identifying details");
    try (Connection c = db.connect()) {
      c.setAutoCommit(false);
      try {
        Report found = get(c, foundId);
        if (found.type() != Type.FOUND || found.status() != Status.ACTIVE)
          throw new IllegalArgumentException("Choose an active found report.");
        if (lostId != null) verifyPair(get(c, lostId), found);
        try (PreparedStatement p =
            c.prepareStatement(
                "INSERT INTO claims(found_id,lost_id,claimant,contact,details)"
                    + " VALUES(?,?,?,?,?)")) {
          p.setLong(1, foundId);
          if (lostId == null) p.setNull(2, Types.INTEGER);
          else p.setLong(2, lostId);
          p.setString(3, claimant);
          p.setString(4, contact);
          p.setString(5, details);
          p.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        if (e instanceof SQLException && e.getMessage().contains("UNIQUE"))
          throw new IllegalArgumentException(
              "This contact already has a claim for that found item.");
        throw e;
      }
    }
  }

  private void verifyPair(Report lost, Report found) {
    if (lost.type() != Type.LOST
        || lost.status() != Status.ACTIVE
        || !lost.category().equals(found.category())
        || found.date().isBefore(lost.date()))
      throw new IllegalArgumentException(
          "Linked report must be active and lost, with the same category and a date on or before"
              + " the found date.");
  }

  public void reject(long claimId) throws SQLException {
    try (Connection c = db.connect();
        PreparedStatement p =
            c.prepareStatement(
                "UPDATE claims SET status='REJECTED' WHERE id=? AND status='PENDING'")) {
      p.setLong(1, claimId);
      if (p.executeUpdate() != 1)
        throw new IllegalArgumentException("Only pending claims can be rejected.");
    }
  }

  public void recover(long claimId, String verification) throws SQLException {
    verification = Validation.required(verification, "Manual verification notes");
    try (Connection c = db.connect()) {
      c.setAutoCommit(false);
      try {
        long foundId;
        Long lostId;
        try (PreparedStatement p =
            c.prepareStatement("SELECT * FROM claims WHERE id=? AND status='PENDING'")) {
          p.setLong(1, claimId);
          try (ResultSet rs = p.executeQuery()) {
            if (!rs.next())
              throw new IllegalArgumentException(
                  "Only a pending claim can be verified and recovered.");
            foundId = rs.getLong("found_id");
            long value = rs.getLong("lost_id");
            lostId = rs.wasNull() ? null : value;
          }
        }
        Report found = get(c, foundId);
        if (found.type() != Type.FOUND || found.status() != Status.ACTIVE)
          throw new IllegalArgumentException("This item has already been recovered.");
        if (lostId != null) verifyPair(get(c, lostId), found);
        try (PreparedStatement p =
            c.prepareStatement(
                "INSERT INTO recoveries(claim_id,found_id,lost_id,verification) VALUES(?,?,?,?)")) {
          p.setLong(1, claimId);
          p.setLong(2, foundId);
          if (lostId == null) p.setNull(3, Types.INTEGER);
          else p.setLong(3, lostId);
          p.setString(4, verification);
          p.executeUpdate();
        }
        try (PreparedStatement p =
            c.prepareStatement("UPDATE reports SET status='RECOVERED' WHERE id=? OR id=?")) {
          p.setLong(1, foundId);
          if (lostId == null) p.setNull(2, Types.INTEGER);
          else p.setLong(2, lostId);
          p.executeUpdate();
        }
        try (PreparedStatement p =
            c.prepareStatement(
                "UPDATE claims SET status='REJECTED' WHERE status='PENDING' AND (found_id=? OR"
                    + " (lost_id IS NOT NULL AND lost_id=?))")) {
          p.setLong(1, foundId);
          if (lostId == null) p.setNull(2, Types.INTEGER);
          else p.setLong(2, lostId);
          p.executeUpdate();
        }
        try (PreparedStatement p =
            c.prepareStatement("UPDATE claims SET status='APPROVED' WHERE id=?")) {
          p.setLong(1, claimId);
          p.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  public int recoveredPairs() throws SQLException {
    try (Connection c = db.connect();
        PreparedStatement p =
            c.prepareStatement("SELECT count(*) FROM recoveries WHERE lost_id IS NOT NULL");
        ResultSet rs = p.executeQuery()) {
      return rs.next() ? rs.getInt(1) : 0;
    }
  }

  public List<String[]> recoveries() throws SQLException {
    List<String[]> rows = new ArrayList<>();
    try (Connection c = db.connect();
        PreparedStatement p = c.prepareStatement("SELECT * FROM recoveries ORDER BY id DESC");
        ResultSet rs = p.executeQuery()) {
      while (rs.next())
        rows.add(
            new String[] {
              rs.getString("id"),
              rs.getString("claim_id"),
              rs.getString("found_id"),
              rs.getString("lost_id"),
              rs.getString("verification"),
              rs.getString("recovered_at")
            });
    }
    return rows;
  }

  public void loadDemo() throws SQLException {
    try (Connection c = db.connect()) {
      c.setAutoCommit(false);
      try {
        try (PreparedStatement p =
                c.prepareStatement("SELECT value FROM settings WHERE key='demo_loaded'");
            ResultSet rs = p.executeQuery()) {
          if (rs.next())
            throw new IllegalArgumentException(
                "Demo records have already been loaded. Your existing records were kept.");
        }
        LocalDate d = LocalDate.now().minusDays(2);
        insert(
            c,
            new Report(
                0,
                Type.LOST,
                "Casio scientific calculator",
                "Electronics",
                "Black",
                "Library second floor",
                d,
                "Blue sticker on back; initials AK",
                "Aarya (demo)",
                "aarya@example.test",
                "",
                Status.ACTIVE));
        insert(
            c,
            new Report(
                0,
                Type.FOUND,
                "Casio calculator",
                "Electronics",
                "Black",
                "Library second floor",
                d.plusDays(1),
                "Blue sticker on back",
                "Campus desk (demo)",
                "desk@example.test",
                "",
                Status.ACTIVE));
        insert(
            c,
            new Report(
                0,
                Type.LOST,
                "Canvas backpack",
                "Bags",
                "Green",
                "Engineering block",
                d,
                "Contains a mechanics notebook",
                "Sam (demo)",
                "sam@example.test",
                "",
                Status.ACTIVE));
        insert(
            c,
            new Report(
                0,
                Type.FOUND,
                "Steel water bottle",
                "Accessories",
                "Silver",
                "Sports hall",
                d.plusDays(1),
                "Small dent near the lid",
                "Riya (demo)",
                "riya@example.test",
                "",
                Status.ACTIVE));
        try (PreparedStatement p =
            c.prepareStatement("INSERT INTO settings(key,value) VALUES('demo_loaded','true')")) {
          p.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }
}
