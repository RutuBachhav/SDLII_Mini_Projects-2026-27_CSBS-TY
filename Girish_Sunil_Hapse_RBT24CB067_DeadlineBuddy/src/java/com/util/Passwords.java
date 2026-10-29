package com.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing with PBKDF2-HMAC-SHA256 from the JDK - no third-party
 * dependency. Stored form:
 *
 * <pre>pbkdf2_sha256$&lt;iterations&gt;$&lt;salt-b64&gt;$&lt;hash-b64&gt;</pre>
 *
 * The iteration count travels with the hash, so raising
 * {@code security.pbkdf2.iterations} does not invalidate existing passwords.
 */
public final class Passwords {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Passwords() {
    }

    /** Hashes a plaintext password for storage. */
    public static String hash(String plain) {
        int iterations = Config.pbkdf2Iterations();
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] key = pbkdf2(plain, salt, iterations);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIX + "$" + iterations + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(key);
    }

    /**
     * Verifies a plaintext password against a stored hash. Returns false for a
     * malformed or missing hash rather than throwing, so a damaged row cannot
     * turn into a 500 on the login page.
     */
    public static boolean verify(String plain, String stored) {
        if (plain == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(plain, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String plain, byte[] salt, int iterations) {
        char[] chars = plain == null ? new char[0] : plain.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 is not available on this JVM", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Prints a hash for a password given on the command line. Used to produce
     * the hashes in {@code db/seed.sql}:
     *
     * <pre>java -cp build/web/WEB-INF/classes com.util.Passwords secret123</pre>
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("usage: Passwords <plaintext> [<plaintext> ...]");
            return;
        }
        for (String a : args) {
            System.out.println(new String(a.getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8) + " -> " + hash(a));
        }
    }
}
