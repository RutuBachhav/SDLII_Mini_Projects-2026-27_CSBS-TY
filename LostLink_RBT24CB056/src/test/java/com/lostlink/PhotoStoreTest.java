package com.lostlink;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PhotoStoreTest {
  @TempDir Path dir;

  @Test
  void copiedPhotoSurvivesOriginalRemoval() throws Exception {
    new Database(dir);
    Path source = dir.resolve("source.png");
    ImageIO.write(new BufferedImage(120, 80, BufferedImage.TYPE_INT_RGB), "png", source.toFile());
    PhotoStore store = new PhotoStore(dir);
    String name = store.copy(source);
    Files.delete(source);
    assertNotNull(store.preview(name, 200, 200));
    assertTrue(Files.exists(dir.resolve("photos").resolve(name)));
  }

  @Test
  void unsupportedFilesHaveHelpfulErrors() throws Exception {
    new Database(dir);
    Path source = dir.resolve("not-image.txt");
    Files.writeString(source, "not an image");
    assertThrows(java.io.IOException.class, () -> new PhotoStore(dir).copy(source));
  }

  @Test
  void largeImageIsBoundedAndPreviewPreservesAspectRatio() throws Exception {
    new Database(dir);
    Path source = dir.resolve("wide.png");
    ImageIO.write(
        new BufferedImage(2400, 1200, BufferedImage.TYPE_INT_RGB), "png", source.toFile());
    PhotoStore store = new PhotoStore(dir);
    String name = store.copy(source);
    BufferedImage saved = ImageIO.read(dir.resolve("photos").resolve(name).toFile());
    assertEquals(1600, saved.getWidth());
    assertEquals(800, saved.getHeight());
    var preview = store.preview(name, 200, 200);
    assertEquals(200, preview.getIconWidth());
    assertEquals(100, preview.getIconHeight());
  }

  @Test
  void oversizeInputAndOutsidePhotoPathsAreRejected() throws Exception {
    new Database(dir);
    Path source = dir.resolve("too-large.png");
    try (var file = new java.io.RandomAccessFile(source.toFile(), "rw")) {
      file.setLength(15_000_001);
    }
    PhotoStore store = new PhotoStore(dir);
    var error = assertThrows(java.io.IOException.class, () -> store.copy(source));
    assertTrue(error.getMessage().contains("15 MB"));
    assertNull(store.preview("../too-large.png", 200, 200));
    assertNull(store.preview("", 200, 200));
  }
}
