package com.lostlink;

import com.lostlink.Models.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.LocalDate;
import javax.imageio.ImageIO;

/** Child JVM probe: exercises actual process exit and restart with a real SQLite file. */
public final class RestartProbe {
  public static void main(String[] args) throws Exception {
    Database db = new Database(Path.of(args[0]));
    Repository repo = new Repository(db);
    switch (args[1]) {
      case "write" -> repo.save(report(Type.LOST, "Restart sentinel", ""));
      case "read" -> {
        if (repo.reports().size() != 1 || !repo.reports().get(0).name().equals("Restart sentinel"))
          throw new IllegalStateException("Report did not survive process restart");
      }
      case "workflow-write" -> {
        Path original = db.directory().resolve("original.png");
        ImageIO.write(
            new BufferedImage(100, 60, BufferedImage.TYPE_INT_RGB), "png", original.toFile());
        String photo = new PhotoStore(db.directory()).copy(original);
        Files.delete(original);
        long lost = repo.save(report(Type.LOST, "Restart calculator", photo));
        long found = repo.save(report(Type.FOUND, "Restart calculator", photo));
        repo.claim(found, lost, "Test student", "test@example.test", "Receipt serial number");
        repo.recover(repo.claims().get(0).id(), "Verified receipt before restart");
        repo.loadDemo();
      }
      case "workflow-read" -> {
        if (repo.reports().size() != 6
            || repo.claims().size() != 1
            || repo.recoveredPairs() != 1
            || repo.recoveries().size() != 1)
          throw new IllegalStateException("Workflow records did not survive restart");
        if (repo.claims().get(0).status() != ClaimStatus.APPROVED
            || repo.reports().stream().filter(r -> r.status() == Status.RECOVERED).count() != 2)
          throw new IllegalStateException("Recovery states did not survive restart");
        if (!repo.recoveries().get(0)[4].equals("Verified receipt before restart"))
          throw new IllegalStateException("Verification notes did not survive restart");
        for (Report r : repo.reports())
          if (!r.photo().isBlank()
              && new PhotoStore(db.directory()).preview(r.photo(), 200, 200) == null)
            throw new IllegalStateException("Photo did not survive restart");
        try {
          repo.loadDemo();
          throw new IllegalStateException("Demo marker did not survive restart");
        } catch (IllegalArgumentException expected) {
          /* persistent demo marker */
        }
      }
      default -> throw new IllegalArgumentException("Unknown probe mode");
    }
  }

  static Report report(Type type, String name, String photo) {
    return new Report(
        0,
        type,
        name,
        "Electronics",
        "Black",
        "Library",
        LocalDate.now(),
        "Saved before exit",
        "Test",
        "test@example.test",
        photo,
        Status.ACTIVE);
  }
}
