package com.pms.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Secure password hashing based on PBKDF2WithHmacSHA256.
 *
 * <p>Stored format (fits in users.password_hash VARCHAR(255)):
 * {@code PBKDF2WithHmacSHA256$iterations$Base64(salt)$Base64(hash)}
 */
public final class PasswordUtil {

    public static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    /** OWASP-recommended iteration count for PBKDF2-HMAC-SHA256. */
    public static final int ITERATIONS = 210000;
    public static final int SALT_BYTES = 16;
    public static final int KEY_BITS = 256;

    private PasswordUtil() {
        // Utility class.
    }

    /**
     * Hashes a password with a fresh random salt.
     *
     * @param password password characters (cleared by the caller afterwards)
     * @return encoded {@code algorithm$iterations$salt$hash} string, never the plaintext
     */
    public static String hash(char[] password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return ALGORITHM + "$" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Verifies a password against a stored value (for future login use).
     */
    public static boolean verify(char[] password, String stored)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = stored.split("\\$", 4);
        if (parts.length != 4 || !ALGORITHM.equals(parts[0])) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, expected.length * 8);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] actual = skf.generateSecret(spec).getEncoded();
            if (actual.length != expected.length) {
                return false;
            }
            int diff = 0;
            for (int i = 0; i < actual.length; i++) {
                diff |= actual[i] ^ expected[i];
            }
            return diff == 0;
        } finally {
            spec.clearPassword();
        }
    }
}
