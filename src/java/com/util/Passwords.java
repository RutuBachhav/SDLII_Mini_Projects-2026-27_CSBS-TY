package com.util;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing with PBKDF2-HMAC-SHA256, which ships with the JDK, so the
 * project needs no external crypto dependency.
 *
 * Stored form: pbkdf2$sha256$&lt;iterations&gt;$&lt;salt b64&gt;$&lt;hash b64&gt;
 * A row whose hash is "!" can never be logged into; that is how the seeded
 * administrator account stays locked until somebody runs AdminInit.
 */
public final class Passwords {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Passwords() {
    }

    public static String hash(String plain) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] key = derive(plain, salt, ITERATIONS);
        return "pbkdf2$sha256$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(key);
    }

    /** Constant-time comparison, and never throws on a malformed stored value. */
    public static boolean matches(String plain, String stored) {
        if (plain == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 5 || !"pbkdf2".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expected = Base64.getDecoder().decode(parts[4]);
            byte[] actual = derive(plain, salt, iterations);
            if (expected.length != actual.length) {
                return false;
            }
            int diff = 0;
            for (int i = 0; i < expected.length; i++) {
                diff |= expected[i] ^ actual[i];
            }
            return diff == 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derive(String plain, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(plain.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing is unavailable", e);
        }
    }

    /** Prints a hash for a password, used when seeding or resetting accounts. */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: Passwords <password>");
            return;
        }
        System.out.println(hash(args[0]));
    }
}
