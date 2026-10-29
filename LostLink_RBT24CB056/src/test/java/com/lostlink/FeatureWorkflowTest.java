package com.lostlink;

import static org.junit.jupiter.api.Assertions.*;

import com.lostlink.Models.*;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Cross-layer feature checks using real SQLite files, never the user's database. */
class FeatureWorkflowTest {
  @TempDir Path dir;
  Database db;
  Repository repo;
  LocalDate day = LocalDate.now().minusDays(3);

  @BeforeEach
  void open() throws Exception {
    db = new Database(dir);
    repo = new Repository(db);
  }

  Report item(long id, Type type, String category, LocalDate date, String name) {
    return new Report(
        id,
        type,
        name,
        category,
        "Blue",
        "Library annex",
        date,
        "Initials AK; sticker on back",
        "Test Student",
        "student@example.test",
        "",
        Status.ACTIVE);
  }

  long add(Type type) throws Exception {
    return repo.save(item(0, type, "Electronics", day, "Calculator"));
  }

  long claim(long found, Long lost, String contact) throws Exception {
    repo.claim(found, lost, "Student", contact, "Serial number 123; matching receipt");
    return repo.claims().get(0).id();
  }

  void integrity() throws Exception {
    try (Connection c = db.connect();
        Statement s = c.createStatement()) {
      try (ResultSet rs = s.executeQuery("PRAGMA integrity_check")) {
        assertTrue(rs.next());
        assertEquals("ok", rs.getString(1));
      }
      try (ResultSet rs = s.executeQuery("PRAGMA foreign_key_check")) {
        assertFalse(rs.next(), "Broken foreign key");
      }
    }
  }

  @Test
  void completeJourneyUpdatesDashboardSearchMatchesAndHistory() throws Exception {
    assertTrue(repo.reports().isEmpty());
    assertTrue(repo.claims().isEmpty());
    assertEquals(0, repo.recoveredPairs());
    long l = add(Type.LOST), f = add(Type.FOUND);
    assertEquals(1, repo.reports().stream().filter(r -> r.type() == Type.LOST).count());
    assertEquals(1, repo.reports().stream().filter(r -> r.type() == Type.FOUND).count());
    assertEquals(2, repo.reports().stream().filter(r -> r.status() == Status.ACTIVE).count());
    assertEquals(100, new MatchingService().suggest(repo.reports()).get(0).score());
    repo.save(item(l, Type.LOST, "Electronics", day, "Casio calculator"));
    assertEquals(
        1,
        repo.reports().stream()
            .filter(r -> ReportSearch.includes(r, "Casio", "LOST", "Electronics", "ACTIVE"))
            .count());
    long id = claim(f, l, "student@example.test");
    assertEquals(ClaimStatus.PENDING, repo.claims().get(0).status());
    repo.recover(id, "Receipt serial verified; returned to student in person");
    assertEquals(0, repo.reports().stream().filter(r -> r.status() == Status.ACTIVE).count());
    assertEquals(1, repo.recoveredPairs());
    assertTrue(new MatchingService().suggest(repo.reports()).isEmpty());
    Repository restarted = new Repository(new Database(dir));
    assertEquals(ClaimStatus.APPROVED, restarted.claims().get(0).status());
    assertEquals(
        "Receipt serial verified; returned to student in person", restarted.recoveries().get(0)[4]);
    assertEquals(
        2,
        restarted.reports().stream()
            .filter(r -> ReportSearch.includes(r, "", "All types", "All categories", "RECOVERED"))
            .count());
    integrity();
  }

  @Test
  void everyReportFieldRoundTripsAndPreparedStatementsKeepSqlLiteral() throws Exception {
    String sql = "O'Brien'); DROP TABLE reports; --";
    Report input =
        new Report(
            0,
            Type.FOUND,
            sql,
            "Cards & IDs",
            "Violet",
            "Café – Wing B",
            day,
            "学生 card\nSecond line",
            "Test's name",
            "+91 00000 00000",
            "photo.png",
            Status.ACTIVE);
    long id = repo.save(input);
    Report got = repo.reports().get(0);
    assertEquals(id, got.id());
    assertEquals(input.name(), got.name());
    assertEquals(input.type(), got.type());
    assertEquals(input.category(), got.category());
    assertEquals(input.colour(), got.colour());
    assertEquals(input.location(), got.location());
    assertEquals(input.date(), got.date());
    assertEquals(input.description(), got.description());
    assertEquals(input.reporter(), got.reporter());
    assertEquals(input.contact(), got.contact());
    assertEquals(input.photo(), got.photo());
    assertEquals(Status.ACTIVE, got.status());
    integrity();
  }

  @Test
  void invalidReportsNeverInsertAndOptionalNullsBecomeEmptyStrings() throws Exception {
    for (Report r :
        List.of(
            item(0, Type.LOST, "Invalid category", day, "Phone"),
            item(0, Type.LOST, "Electronics", LocalDate.now().plusDays(1), "Phone"),
            item(0, Type.LOST, "Electronics", day, " "),
            item(0, Type.LOST, "Electronics", day, "x".repeat(4001))))
      assertThrows(IllegalArgumentException.class, () -> repo.save(r));
    assertTrue(repo.reports().isEmpty());
    repo.save(
        new Report(
            0, Type.LOST, "Keys", "Keys", null, "Hall", day, null, "A", "a", null, Status.ACTIVE));
    Report got = repo.reports().get(0);
    assertEquals("", got.colour());
    assertEquals("", got.description());
    assertEquals("", got.photo());
    integrity();
  }

  @Test
  void claimsRejectWrongCategoryEarlierFoundDateMissingIdsAndRequiredFields() throws Exception {
    long f = add(Type.FOUND), l = repo.save(item(0, Type.LOST, "Keys", day, "Keys"));
    long laterLost = repo.save(item(0, Type.LOST, "Electronics", day.plusDays(1), "Phone"));
    assertThrows(IllegalArgumentException.class, () -> claim(f, l, "one"));
    assertThrows(IllegalArgumentException.class, () -> claim(f, laterLost, "two"));
    assertThrows(IllegalArgumentException.class, () -> claim(999, null, "three"));
    assertThrows(IllegalArgumentException.class, () -> claim(f, 999L, "four"));
    assertThrows(IllegalArgumentException.class, () -> repo.claim(f, null, " ", "a", "Proof"));
    assertThrows(IllegalArgumentException.class, () -> repo.claim(f, null, "A", " ", "Proof"));
    assertThrows(IllegalArgumentException.class, () -> repo.claim(f, null, "A", "a", " "));
    assertTrue(repo.claims().isEmpty());
    integrity();
  }

  @Test
  void rejectedClaimAndRecoveredItemCannotBeReused() throws Exception {
    long f = add(Type.FOUND), l = add(Type.LOST), rejected = claim(f, l, "one");
    repo.reject(rejected);
    assertThrows(IllegalArgumentException.class, () -> repo.reject(rejected));
    assertThrows(IllegalArgumentException.class, () -> repo.recover(rejected, "Receipt"));
    assertThrows(IllegalArgumentException.class, () -> claim(f, l, " ONE "));
    long accepted = claim(f, l, "two");
    repo.recover(accepted, "Receipt verified");
    assertThrows(IllegalArgumentException.class, () -> claim(f, null, "three"));
    long other = add(Type.FOUND);
    assertThrows(IllegalArgumentException.class, () -> claim(other, l, "four"));
    assertEquals(1, repo.recoveries().size());
    integrity();
  }

  @Test
  void claimedReportsPermitDescriptiveEditsButProtectStructureAndDeletion() throws Exception {
    long l = add(Type.LOST), f = add(Type.FOUND);
    claim(f, l, "one");
    repo.save(item(l, Type.LOST, "Electronics", day, "Updated calculator"));
    assertThrows(
        IllegalArgumentException.class, () -> repo.save(item(l, Type.LOST, "Keys", day, "Keys")));
    assertThrows(
        IllegalArgumentException.class,
        () -> repo.save(item(l, Type.LOST, "Electronics", day.minusDays(1), "Phone")));
    assertThrows(IllegalArgumentException.class, () -> repo.delete(l));
    assertThrows(IllegalArgumentException.class, () -> repo.delete(f));
    long disposable = add(Type.FOUND);
    repo.delete(disposable);
    assertEquals(2, repo.reports().size());
    integrity();
  }

  @Test
  void constraintsEnforceForeignKeysEnumsAndUniqueRecoveries() throws Exception {
    long l = add(Type.LOST), f = add(Type.FOUND);
    long id = claim(f, l, "one");
    repo.recover(id, "Receipt verified");
    try (Connection c = db.connect();
        Statement s = c.createStatement()) {
      assertThrows(
          SQLException.class,
          () ->
              s.executeUpdate(
                  "INSERT INTO claims(found_id,claimant,contact,details)"
                      + " VALUES(999,'A','b','proof')"));
      assertThrows(
          SQLException.class,
          () -> s.executeUpdate("UPDATE reports SET status='UNKNOWN' WHERE id=" + l));
      assertThrows(
          SQLException.class,
          () -> s.executeUpdate("UPDATE claims SET status='UNKNOWN' WHERE id=" + id));
      assertThrows(SQLException.class, () -> s.executeUpdate("DELETE FROM reports WHERE id=" + f));
      assertThrows(
          SQLException.class,
          () ->
              s.executeUpdate(
                  "INSERT INTO recoveries(claim_id,found_id,lost_id,verification) VALUES("
                      + id
                      + ","
                      + f
                      + ","
                      + l
                      + ",'again')"));
    }
    integrity();
  }

  @Test
  void demoLoadingRollsBackCompletelyAndCanBeRetried() throws Exception {
    long real = add(Type.LOST);
    try (Connection c = db.connect();
        Statement s = c.createStatement()) {
      s.execute(
          "CREATE TRIGGER fail_demo BEFORE INSERT ON reports WHEN NEW.type='FOUND' BEGIN SELECT"
              + " RAISE(ABORT,'test demo failure'); END");
    }
    assertThrows(SQLException.class, () -> repo.loadDemo());
    assertEquals(1, repo.reports().size());
    assertEquals(real, repo.reports().get(0).id());
    try (Connection c = db.connect();
        Statement s = c.createStatement()) {
      s.execute("DROP TRIGGER fail_demo");
    }
    repo.loadDemo();
    assertEquals(5, repo.reports().size());
    assertThrows(IllegalArgumentException.class, () -> repo.loadDemo());
    integrity();
  }

  @Test
  void simultaneousRecoveryAttemptsProduceExactlyOneRecovery() throws Exception {
    long l = add(Type.LOST), f = add(Type.FOUND), id = claim(f, l, "one");
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    Callable<Boolean> attempt =
        () -> {
          start.await();
          try {
            new Repository(db).recover(id, "Verified receipt");
            return true;
          } catch (SQLException | IllegalArgumentException e) {
            return false;
          }
        };
    try {
      Future<Boolean> a = pool.submit(attempt), b = pool.submit(attempt);
      start.countDown();
      int successes = (a.get(15, TimeUnit.SECONDS) ? 1 : 0) + (b.get(15, TimeUnit.SECONDS) ? 1 : 0);
      assertEquals(1, successes);
    } finally {
      pool.shutdownNow();
    }
    assertEquals(1, repo.recoveries().size());
    assertEquals(1, repo.recoveredPairs());
    assertEquals(ClaimStatus.APPROVED, repo.claims().get(0).status());
    integrity();
  }
}
