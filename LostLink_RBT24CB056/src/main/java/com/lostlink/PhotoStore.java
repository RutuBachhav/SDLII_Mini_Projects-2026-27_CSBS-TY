package com.lostlink;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class PhotoStore {
  private final Path directory;

  public PhotoStore(Path directory) {
    this.directory = directory.resolve("photos");
  }

  /** Decode once and store a bounded PNG copy so the original can move independently. */
  public String copy(Path source) throws IOException {
    if (Files.size(source) > 15_000_000)
      throw new IOException("Choose a photo smaller than 15 MB.");
    BufferedImage input = ImageIO.read(source.toFile());
    if (input == null) throw new IOException("Choose a supported image: PNG, JPEG or GIF.");
    double scale = Math.min(1, 1600.0 / Math.max(input.getWidth(), input.getHeight()));
    BufferedImage output =
        new BufferedImage(
            Math.max(1, (int) (input.getWidth() * scale)),
            Math.max(1, (int) (input.getHeight() * scale)),
            BufferedImage.TYPE_INT_RGB);
    Graphics2D g = output.createGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, output.getWidth(), output.getHeight());
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.drawImage(input, 0, 0, output.getWidth(), output.getHeight(), null);
    g.dispose();
    String filename = UUID.randomUUID() + ".png";
    Path target = directory.resolve(filename);
    if (!ImageIO.write(output, "png", target.toFile()))
      throw new IOException("Could not save the selected photo.");
    return filename;
  }

  public ImageIcon preview(String filename, int width, int height) throws IOException {
    Path path = directory.resolve(filename).normalize();
    if (!path.startsWith(directory) || filename.isBlank()) return null;
    BufferedImage input = ImageIO.read(path.toFile());
    if (input == null) throw new IOException("The saved photo cannot be decoded.");
    double scale = Math.min((double) width / input.getWidth(), (double) height / input.getHeight());
    return new ImageIcon(
        input.getScaledInstance(
            Math.max(1, (int) (input.getWidth() * scale)),
            Math.max(1, (int) (input.getHeight() * scale)),
            Image.SCALE_SMOOTH));
  }
}
