package com.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Small helpers shared by the servlets: reading parameters, the CSRF token, the
 * logged-in user id, and one-shot flash messages.
 */
public final class Web {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_EMAIL = "userEmail";
    public static final String SESSION_CSRF = "csrfToken";
    public static final String SESSION_FLASH = "flash";

    private static final SecureRandom RANDOM = new SecureRandom();

    private Web() {
    }

    // -- parameters ---------------------------------------------------------

    /** Trimmed parameter, or null when absent or blank. */
    public static String str(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null) {
            return null;
        }
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    /** Trimmed parameter, or "" - for redisplaying a form. */
    public static String strOrEmpty(HttpServletRequest req, String name) {
        String v = str(req, name);
        return v == null ? "" : v;
    }

    public static String require(HttpServletRequest req, String name, String label) {
        String v = str(req, name);
        if (v == null) {
            throw new ValidationException(name, label + " is required.");
        }
        return v;
    }

    /** Required id parameter. A missing or non-numeric id is a 404, not a 500. */
    public static long requireId(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null) {
            throw new NotFoundException("No record was requested.");
        }
        try {
            long id = Long.parseLong(v);
            if (id <= 0) {
                throw new NotFoundException("That record does not exist.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new NotFoundException("That record does not exist.");
        }
    }

    /** Optional id parameter; returns null when absent, blank or not a number. */
    public static Long optionalId(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null) {
            return null;
        }
        try {
            long id = Long.parseLong(v);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Validates a "where to go back to" value taken from a form.
     *
     * Only a path within this application is accepted, and only one of the
     * application's own pages. Anything with a scheme, a host or a backslash is
     * replaced with {@code fallback}, so a crafted returnTo cannot bounce a
     * signed-in student off to another site.
     */
    public static String safePath(String raw, String fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        String path = raw.trim();
        if (path.contains(":") || path.contains("\\") || path.startsWith("//")) {
            return fallback;
        }
        if (!path.startsWith("/")) {
            return fallback;
        }
        if (path.startsWith("/dashboard") || path.startsWith("/tasks") || path.startsWith("/subjects")) {
            return path;
        }
        return fallback;
    }

    // -- session ------------------------------------------------------------

    /** The logged-in user's id, or null when nobody is logged in. */
    public static Long currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Long) session.getAttribute(SESSION_USER_ID);
    }

    /** The logged-in user's id, or a 404-style failure if the session is gone. */
    public static long requireUserId(HttpServletRequest req) {
        Long id = currentUserId(req);
        if (id == null) {
            throw new AppException("Your session has expired. Please sign in again.");
        }
        return id;
    }

    // -- CSRF ---------------------------------------------------------------

    /**
     * Returns this session's CSRF token, creating one on first use. Every form
     * in the application posts it back in a hidden field and
     * {@code com.filter.CsrfFilter} rejects a POST without a match.
     */
    public static String csrfToken(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        String token = (String) session.getAttribute(SESSION_CSRF);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_CSRF, token);
        }
        return token;
    }

    // -- flash messages -----------------------------------------------------

    /**
     * Queues a message to be shown once on the next page. Used with
     * redirect-after-POST: the servlet redirects, the next GET shows the
     * message, and a refresh does not repeat the operation.
     */
    public static void flash(HttpServletRequest req, String type, String text) {
        req.getSession(true).setAttribute(SESSION_FLASH, new Flash(type, text));
    }

    public static void flashSuccess(HttpServletRequest req, String text) {
        flash(req, "success", text);
    }

    public static void flashError(HttpServletRequest req, String text) {
        flash(req, "danger", text);
    }

    /** Reads and clears the pending message. */
    public static Flash takeFlash(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Flash flash = (Flash) session.getAttribute(SESSION_FLASH);
        if (flash != null) {
            session.removeAttribute(SESSION_FLASH);
        }
        return flash;
    }

    /** A one-shot message. {@code type} is a Bootstrap alert suffix. */
    public static final class Flash implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        private final String type;
        private final String text;

        public Flash(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
}
