package com.service;

import com.util.Config;
import jakarta.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Photo storage on the filesystem.
 *
 * Where: the directory named by the {@code foundit.storage.dir} property, which
 * foundit.properties points at this project's own {@code uploads} folder. It is
 * never derived from the server's working directory, and it sits outside
 * {@code web/}, so a build or a redeploy cannot wipe it and no file is reachable
 * by URL. Photographs are served by PhotoServlet instead.
 *
 * What: the bytes are decoded as an image before anything is written. A file
 * that does not decode is rejected whatever its name or declared type says, so
 * a renamed .exe cannot get in. The name on disk is generated here; the
 * browser's filename is never used for anything.
 */
public class Storage {

    /** 5 MB is plenty for a phone photo of a lost wallet. */
    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> ALLOWED = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp");

    /** Thrown when an upload is not acceptable; the message is safe to show. */
    public static class Rejected extends Exception {
        public Rejected(String message) {
            super(message);
        }
    }

    /** What was written, for the database row. */
    public static class Stored {
        public final String key;
        public final String contentType;
        public final long size;

        Stored(String key, String contentType, long size) {
            this.key = key;
            this.contentType = contentType;
            this.size = size;
        }
    }

    public Path baseDir() {
        String configured = Config.get("foundit.storage.dir", "");
        Path base = configured.isEmpty()
                ? Paths.get(System.getProperty("user.home"), ".foundit", "uploads")
                : Paths.get(configured);
        return base.toAbsolutePath().normalize();
    }

    public boolean isConfigured() {
        return !Config.get("foundit.storage.dir", "").isEmpty();
    }

    /**
     * Validates and stores one uploaded part.
     *
     * @param folder "items" for public photographs, "claims" for private files
     */
    public Stored save(Part part, String folder) throws Rejected, IOException {
        if (part == null || part.getSize() <= 0) {
            throw new Rejected("No file was uploaded.");
        }
        if (part.getSize() > MAX_BYTES) {
            throw new Rejected("That image is larger than 5 MB.");
        }

        Path dir = baseDir().resolve(folder).normalize();
        if (!dir.startsWith(baseDir())) {
            throw new Rejected("Invalid storage location.");
        }
        Files.createDirectories(dir);

        Path temp = Files.createTempFile(dir, "upload-", ".part");
        try {
            try (InputStream in = part.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            // Content check: it has to decode as an image, and the format comes
            // from the bytes rather than from anything the browser said.
            String format = sniffFormat(temp);
            if (format == null) {
                throw new Rejected("That file is not a readable image. "
                        + "Upload a JPG, PNG, GIF or WEBP.");
            }
            String extension = "jpeg".equals(format) ? "jpg" : format;
            String contentType = ALLOWED.get(extension);
            if (contentType == null) {
                throw new Rejected("Only JPG, PNG, GIF and WEBP images are accepted.");
            }

            String name = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path target = dir.resolve(name);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            return new Stored(folder + "/" + name, contentType, Files.size(target));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Turns a stored key back into a path, refusing anything that tries to climb
     * out of the uploads directory.
     */
    public Path resolve(String storageKey) throws Rejected {
        if (storageKey == null || storageKey.isBlank()
                || storageKey.contains("..") || storageKey.startsWith("/")
                || storageKey.contains("\\")) {
            throw new Rejected("Invalid file reference.");
        }
        Path base = baseDir();
        Path path = base.resolve(storageKey).normalize();
        if (!path.startsWith(base)) {
            throw new Rejected("Invalid file reference.");
        }
        return path;
    }

    private static String sniffFormat(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            javax.imageio.stream.ImageInputStream stream =
                    ImageIO.createImageInputStream(in);
            if (stream == null) {
                return null;
            }
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                return null;
            }
            var reader = readers.next();
            try {
                reader.setInput(stream);
                BufferedImage image = reader.read(0);
                if (image == null || image.getWidth() <= 0) {
                    return null;
                }
                return reader.getFormatName().toLowerCase(Locale.ROOT);
            } catch (IOException e) {
                return null;
            } finally {
                reader.dispose();
            }
        }
    }
}
