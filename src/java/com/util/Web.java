package com.util;

import com.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/** Small helpers shared by the servlets: session user, CSRF token, parameters. */
public final class Web {

    public static final String SESSION_USER = "user";
    public static final String SESSION_CSRF = "csrf";
    public static final String FLASH = "flash";
    public static final String FLASH_KIND = "flashKind";

    private static final SecureRandom RANDOM = new SecureRandom();

    private Web() {
    }

    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute(SESSION_USER);
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User u = currentUser(request);
        return u != null && u.isAdmin();
    }

    /**
     * The per-session CSRF token. Every form posts it back in a hidden field and
     * CsrfFilter compares the two before any state-changing request runs.
     */
    public static String csrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(SESSION_CSRF);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_CSRF, token);
        }
        return token;
    }

    public static String str(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    public static int num(HttpServletRequest request, String name, int fallback) {
        try {
            return Integer.parseInt(str(request, name));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * A one-shot message carried across a redirect. Using redirect-after-post
     * everywhere is also what stops a refresh from repeating a submission.
     */
    public static void flash(HttpServletRequest request, String kind, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute(FLASH, message);
        session.setAttribute(FLASH_KIND, kind);
    }

    /** Truncates a value to a column width without throwing on short input. */
    public static String clip(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
