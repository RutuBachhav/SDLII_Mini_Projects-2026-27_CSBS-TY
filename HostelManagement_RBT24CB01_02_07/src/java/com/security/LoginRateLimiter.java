package com.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory limiter for repeated failed logins.
 */
public final class LoginRateLimiter {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MS = 10L * 60L * 1000L;
    private static final long LOCK_MS = 5L * 60L * 1000L;

    private static final Map<String, Attempt> ATTEMPTS =
            new ConcurrentHashMap<>();

    private LoginRateLimiter() {
    }

    public static boolean isBlocked(String key) {
        Attempt attempt = ATTEMPTS.get(key);
        if (attempt == null) {
            return false;
        }
        synchronized (attempt) {
            long now = System.currentTimeMillis();
            if (attempt.lockedUntil > now) {
                return true;
            }
            if (attempt.lockedUntil > 0) {
                attempt.lockedUntil = 0;
                attempt.failures = 0;
                attempt.windowStart = now;
            }
            if (attempt.failures > 0
                    && now - attempt.windowStart > WINDOW_MS) {
                attempt.failures = 0;
                attempt.windowStart = now;
            }
            return false;
        }
    }

    public static void recordFailure(String key) {
        Attempt attempt = ATTEMPTS.computeIfAbsent(
                key, ignored -> new Attempt());
        synchronized (attempt) {
            long now = System.currentTimeMillis();
            if (attempt.failures == 0
                    || now - attempt.windowStart > WINDOW_MS) {
                attempt.failures = 0;
                attempt.windowStart = now;
            }
            attempt.failures++;
            if (attempt.failures >= MAX_FAILURES) {
                attempt.lockedUntil = now + LOCK_MS;
            }
        }
    }

    public static void clear(String key) {
        ATTEMPTS.remove(key);
    }

    private static final class Attempt {
        private int failures;
        private long windowStart = System.currentTimeMillis();
        private long lockedUntil;
    }
}
