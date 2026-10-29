package com.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Adaptive password hashing (BCrypt, cost 12).
 */
public final class PasswordUtil {

    private static final int COST = 12;

    private static volatile String dummyHash;

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password is required");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(COST));
    }

    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Hash used only to keep failed-login timing similar for unknown users.
     */
    public static String dummyHash() {
        String value = dummyHash;
        if (value == null) {
            synchronized (PasswordUtil.class) {
                value = dummyHash;
                if (value == null) {
                    value = hash("dummy-password-value-for-timing");
                    dummyHash = value;
                }
            }
        }
        return value;
    }

    public static String strengthProblem(String password) {
        if (password == null || password.length() < 10) {
            return "Password must contain at least 10 characters.";
        }
        boolean letter = false;
        boolean digit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letter = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            }
        }
        if (!letter || !digit) {
            return "Password must contain at least one letter and one digit.";
        }
        if (password.length() > 100) {
            return "Password is too long.";
        }
        return null;
    }
}
