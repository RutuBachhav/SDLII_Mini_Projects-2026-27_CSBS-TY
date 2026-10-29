package com.lostlink;

import static org.junit.jupiter.api.Assertions.*;

import com.lostlink.Models.*;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class RepositoryTest {
  @TempDir Path dir;
  Database db;
  Repository repo;

  @BeforeEach
  void setup() throws Exception {
    db = new Database(dir);
    repo = new Repository(db);
  }

  Report report(long id, Type type, String name) {
    return new Report(
        id,
        type,
        name,
        "Electronics",
        "Black",
        "Library",
        LocalDate.now().minusDays(1),
        "Sticker",
        "Sam",
        "sam@example.test",
        "",
        Status.ACTIVE);
  }

  long add(Type type) throws Exception {
    return repo.save(report(0, type, "Phone"));
  }

  @Test
  void creationEditingDeletionAndPersistenceAcrossConnections() throws Exception {
    long id = add(Type.LOST);
    repo.save(report(id, Type.LOST, "Updated phone"));
    Repository reopened = new Repository(new Database(dir));
    assertEquals("Updated phone", reopened.reports().get(0).name());
    reopened.delete(id);
    assertTrue(repo.reports().isEmpty());
  }

  @Test
  void claimsAndAtomicRecovery() throws Exception {
    long lost = add(Type.LOST), found = add(Type.FOUND), otherFound = add(Type.FOUND);
    repo.claim(found, lost, "Sam", "sam@example.test", "Unlock code and serial number");
    repo.claim(found, null, "Other", "other@example.test", "Blue sticker");
    repo.claim(otherFound, lost, "Sam", "sam@example.test", "Serial number");
    long claim =
        repo.claims().stream()
            .filter(c -> c.foundId() == found && c.lostId() != null)
            .findFirst()
            .orElseThrow()
            .id();
    repo.recover(claim, "Checked serial number against original receipt; returned in person");
    assertEquals(2, repo.reports().stream().filter(r -> r.status() == Status.RECOVERED).count());
    assertEquals(1, repo.recoveredPairs());
    assertEquals(1, repo.claims().stream().filter(c -> c.status() == ClaimStatus.APPROVED).count());
    assertEquals(2, repo.claims().stream().filter(c -> c.status() == ClaimStatus.REJECTED).count());
    assertThrows(IllegalArgumentException.class, () -> repo.recover(claim, "Again"));
    assertEquals(1, repo.recoveries().size());
    assertThrows(IllegalArgumentException.class, () -> repo.delete(found));
    assertThrows(
        IllegalArgumentException.class, () -> repo.save(report(lost, Type.LOST, "Changed")));
  }

  @Test
  void duplicateClaimsNormalizeContact() throws Exception {
    long f = add(Type.FOUND);
    repo.claim(f, null, "A", " A@EXAMPLE.TEST ", "Serial");
    assertThrows(
        IllegalArgumentException.class, () -> repo.claim(f, null, "A", "a@example.test", "Serial"));
    assertEquals(1, repo.claims().size());
  }

  @Test
  void invalidLinksAndBlankVerificationDoNotMutateData() throws Exception {
    long l = add(Type.LOST), f = add(Type.FOUND);
    assertThrows(IllegalArgumentException.class, () -> repo.claim(l, f, "A", "a", "Proof"));
    repo.claim(f, l, "A", "a", "Proof");
    long claim = repo.claims().get(0).id();
    assertThrows(IllegalArgumentException.class, () -> repo.recover(claim, " "));
    assertEquals(0, repo.recoveries().size());
    assertTrue(repo.reports().stream().allMatch(r -> r.status() == Status.ACTIVE));
    assertEquals(ClaimStatus.PENDING, repo.claims().get(0).status());
  }

  @Test
  void rollbackOnDatabaseFailureRestoresEveryRecord() throws Exception {
    long l = add(Type.LOST), f = add(Type.FOUND);
    repo.claim(f, l, "A", "a", "Proof");
    long claim = repo.claims().get(0).id();
    try (Connection c = db.connect();
        Statement s = c.createStatement()) {
      s.execute(
          "CREATE TRIGGER force_failure BEFORE UPDATE ON claims WHEN NEW.status='APPROVED' BEGIN"
              + " SELECT RAISE(ABORT,'test failure'); END");
    }
    assertThrows(SQLException.class, () -> repo.recover(claim, "Checked original receipt"));
    assertEquals(0, repo.recoveries().size());
    assertTrue(repo.reports().stream().allMatch(r -> r.status() == Status.ACTIVE));
    assertEquals(ClaimStatus.PENDING, repo.claims().get(0).status());
  }

  @Test
  void standaloneFoundRecoveryDoesNotCountAsPair() throws Exception {
    long f = add(Type.FOUND);
    repo.claim(f, null, "A", "a", "Proof");
    repo.recover(repo.claims().get(0).id(), "Receipt verified");
    assertEquals(0, repo.recoveredPairs());
    assertEquals(1, repo.recoveries().size());
  }

  @Test
  void rejectAndPreventStructuralEditsToClaimedReports() throws Exception {
    long f = add(Type.FOUND);
    repo.claim(f, null, "A", "a", "Proof");
    assertThrows(IllegalArgumentException.class, () -> repo.save(report(f, Type.LOST, "Phone")));
    repo.reject(repo.claims().get(0).id());
    assertEquals(ClaimStatus.REJECTED, repo.claims().get(0).status());
    assertThrows(
        IllegalArgumentException.class, () -> repo.recover(repo.claims().get(0).id(), "Proof"));
  }

  @Test
  void demoLoadsOnceAndPreservesRealData() throws Exception {
    long id = add(Type.LOST);
    repo.loadDemo();
    assertEquals(5, repo.reports().size());
    assertThrows(IllegalArgumentException.class, () -> repo.loadDemo());
    assertEquals(5, repo.reports().size());
    assertTrue(repo.reports().stream().anyMatch(r -> r.id() == id));
  }

  @Test
  void foreignKeysEnabledOnEveryConnection() throws Exception {
    try (Connection c = db.connect();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("PRAGMA foreign_keys")) {
      assertTrue(rs.next());
      assertEquals(1, rs.getInt(1));
    }
  }
}
