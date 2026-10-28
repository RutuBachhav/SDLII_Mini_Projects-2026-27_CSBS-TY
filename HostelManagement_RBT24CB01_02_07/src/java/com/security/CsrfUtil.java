package com.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Session-bound synchronizer token used by every state-changing form.
 */
public final class CsrfUtil {

    public static final String SESSION_ATTR = "csrfToken";
    public static final String PARAM = "_csrf";
    public static final String HEADER = "X-CSRF-Token";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    public static String ensureToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        synchronized (session) {
            Object existing = session.getAttribute(SESSION_ATTR);
            if (existing instanceof String && !((String) existing).isEmpty()) {
                return (String) existing;
            }
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(bytes);
            session.setAttribute(SESSION_ATTR, token);
            return token;
        }
    }

    public static boolean validate(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTR);
        if (!(expected instanceof String)) {
            return false;
        }
        String actual = request.getParameter(PARAM);
        if (actual == null || actual.isEmpty()) {
            actual = request.getHeader(HEADER);
        }
        if (actual == null || actual.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(
                ((String) expected).getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
