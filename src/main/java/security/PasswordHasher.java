package security;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2-HMAC-SHA256 password hashing.
 * <p>
 * Format on disk: {@code base64(salt):iterations:base64(hash)}.
 * Default 100,000 iterations, 16-byte salt, 256-bit derived key.
 */
public final class PasswordHasher {

    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int    DEFAULT_ITERATIONS = 100_000;
    private static final int    SALT_BYTES  = 16;
    private static final int    KEY_BITS    = 256;

    private PasswordHasher() {}

    /** Hash a fresh password (generates a new random salt). */
    public static String hash(String plain) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return hashWithSalt(plain, salt, DEFAULT_ITERATIONS);
    }

    /** Hash with an explicit salt+iterations (used by tests / migrations). */
    public static String hashWithSalt(String plain, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plain.toCharArray(), salt, iterations, KEY_BITS);
            byte[] key = SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt) + ":"
                 + iterations + ":"
                 + Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    /** Constant-time verification against a stored hash. */
    public static boolean verify(String plain, String stored) {
        if (plain == null || stored == null) return false;
        String[] parts = stored.split(":");
        if (parts.length != 3) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            int iter = Integer.parseInt(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            PBEKeySpec spec = new PBEKeySpec(plain.toCharArray(), salt, iter, expected.length * 8);
            byte[] actual = SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
            return constantTimeEquals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
