package hackmate;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing helper for HackMate.
 *
 * <p>Uses PBKDF2WithHmacSHA256, which is built into the JDK, so no extra
 * library has to be added to the project. Plain text passwords are never
 * stored in the database.</p>
 *
 * <p>Stored format (single database column, varchar(255) is enough):</p>
 * <pre>
 *   PBKDF2$&lt;iterations&gt;$&lt;saltBase64&gt;$&lt;hashBase64&gt;
 * </pre>
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "PBKDF2";
    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Hashes a plain text password with a freshly generated random salt.
     *
     * @param plainPassword the password typed by the user
     * @return the value that should be stored in {@code users.password}
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password must not be null");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = derive(plainPassword.toCharArray(), salt, ITERATIONS);

        return PREFIX + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Checks a plain text password against a stored hash.
     *
     * @param plainPassword the password typed by the user
     * @param storedValue   the value read from {@code users.password}
     * @return true when the password matches
     */
    public static boolean verify(String plainPassword, String storedValue) {
        if (plainPassword == null || storedValue == null) {
            return false;
        }

        String[] parts = storedValue.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(plainPassword.toCharArray(), salt, iterations);

            // Constant time comparison to avoid leaking information.
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            // Stored value was not produced by this algorithm, so it cannot match.
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash the password", e);
        }
    }
}
