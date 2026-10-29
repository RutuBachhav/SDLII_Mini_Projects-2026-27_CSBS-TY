package com.labloop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Salted SHA-256 password hashing. Passwords are never stored as plain text. */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String hash(String raw) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        return saltText + "$" + digest(saltText, raw);
    }

    public static boolean verify(String raw, String stored) {
        if (raw == null || stored == null || !stored.contains("$")) return false;
        String[] parts = stored.split("\\$", 2);
        return MessageDigest.isEqual(parts[1].getBytes(StandardCharsets.UTF_8),
                digest(parts[0], raw).getBytes(StandardCharsets.UTF_8));
    }

    private static String digest(String salt, String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest((salt + raw).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
