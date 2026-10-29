package com.campuscare.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TokenUtil {

    private static final String SECRET = "CampusCare-Secret-Key-2026-Secure-Enterprise-Java-Project-SignKey";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long EXPIRATION_MILLIS = 24 * 60 * 60 * 1000L; // 24 hours

    public static class TokenClaims {
        private final Long userId;
        private final String role;
        private final long expiry;

        public TokenClaims(Long userId, String role, long expiry) {
            this.userId = userId;
            this.role = role;
            this.expiry = expiry;
        }

        public Long getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }

        public long getExpiry() {
            return expiry;
        }
    }

    public static String generateToken(Long userId, String role) {
        long expiry = System.currentTimeMillis() + EXPIRATION_MILLIS;
        String payload = userId + ":" + role + ":" + expiry;
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        return encodedPayload + "." + signature;
    }

    public static TokenClaims validateToken(String token) {
        if (token == null || !token.contains(".")) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }
        String encodedPayload = parts[0];
        String signature = parts[1];

        String expectedSignature = sign(encodedPayload);
        if (!expectedSignature.equals(signature)) {
            return null;
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
            String[] fields = payload.split(":");
            if (fields.length != 3) {
                return null;
            }
            Long userId = Long.valueOf(fields[0]);
            String role = fields[1];
            long expiry = Long.parseLong(fields[2]);

            if (System.currentTimeMillis() > expiry) {
                return null; // Expired
            }

            return new TokenClaims(userId, role, expiry);
        } catch (Exception e) {
            return null;
        }
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKey = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error signing token", e);
        }
    }
}
