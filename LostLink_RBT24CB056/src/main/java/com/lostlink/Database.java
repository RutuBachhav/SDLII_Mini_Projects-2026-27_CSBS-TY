package com.lostlink;

import java.nio.file.*;
import java.sql.*;

public final class Database {
  private final Path directory;

  public Database(Path directory) throws Exception {
    this.directory = directory.toAbsolutePath();
    Files.createDirectories(this.directory.resolve("photos"));
    try (Connection c = connect();
        Statement s = c.createStatement()) {
      s.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS reports (
            id INTEGER PRIMARY KEY, type TEXT NOT NULL CHECK(type IN ('LOST','FOUND')),
            name TEXT NOT NULL CHECK(length(trim(name))>0), category TEXT NOT NULL,
            colour TEXT NOT NULL DEFAULT '', location TEXT NOT NULL, incident_date TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '', reporter TEXT NOT NULL, contact TEXT NOT NULL,
            photo TEXT NOT NULL DEFAULT '', status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','RECOVERED')),
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)
          """);
      s.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS claims (
            id INTEGER PRIMARY KEY, found_id INTEGER NOT NULL REFERENCES reports(id) ON DELETE RESTRICT,
            lost_id INTEGER REFERENCES reports(id) ON DELETE RESTRICT,
            claimant TEXT NOT NULL, contact TEXT NOT NULL, details TEXT NOT NULL,
            status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING','APPROVED','REJECTED')),
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(found_id, contact), CHECK(lost_id IS NULL OR lost_id <> found_id))
          """);
      s.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS recoveries (
            id INTEGER PRIMARY KEY, claim_id INTEGER NOT NULL UNIQUE REFERENCES claims(id) ON DELETE RESTRICT,
            found_id INTEGER NOT NULL UNIQUE REFERENCES reports(id) ON DELETE RESTRICT,
            lost_id INTEGER UNIQUE REFERENCES reports(id) ON DELETE RESTRICT,
            verification TEXT NOT NULL CHECK(length(trim(verification))>0),
            recovered_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)
          """);
      s.executeUpdate(
          "CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
      s.executeUpdate("CREATE INDEX IF NOT EXISTS reports_status_type ON reports(status,type)");
    }
  }

  public static Path defaultDirectory() {
    String override = System.getProperty("lostlink.dataDir");
    if (override != null && !override.isBlank()) return Path.of(override);
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("mac"))
      return Path.of(System.getProperty("user.home"), "Library", "Application Support", "LostLink");
    if (os.contains("win") && System.getenv("LOCALAPPDATA") != null)
      return Path.of(System.getenv("LOCALAPPDATA"), "LostLink");
    return Path.of(System.getProperty("user.home"), ".local", "share", "LostLink");
  }

  public Path directory() {
    return directory;
  }

  public Connection connect() throws SQLException {
    Connection c = DriverManager.getConnection("jdbc:sqlite:" + directory.resolve("lostlink.db"));
    try (Statement s = c.createStatement()) {
      s.execute("PRAGMA foreign_keys=ON");
      s.execute("PRAGMA busy_timeout=5000");
    } catch (SQLException e) {
      c.close();
      throw e;
    }
    return c;
  }
}
