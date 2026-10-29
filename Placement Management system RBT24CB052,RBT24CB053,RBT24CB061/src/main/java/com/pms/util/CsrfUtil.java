package com.pms.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Session-bound CSRF tokens. Every authenticated session gets a token
 * (see AuthFilter); state-changing POSTs must send it back as
 * the {@code csrfToken} parameter.
 */
public final class CsrfUtil {

    public static final String ATTR_CSRF = "csrfToken";
    public static final String PARAM_CSRF = "csrfToken";

    private CsrfUtil() {}

    /** Returns the session token, creating one if absent. */
    public static String getToken(HttpSession session) {
        Object existing = session.getAttribute(ATTR_CSRF);
        if (existing instanceof String s && !s.isBlank()) {
            return s;
        }
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        session.setAttribute(ATTR_CSRF, token);
        return token;
    }

    /** Constant-time comparison of the request parameter against the session token. */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(ATTR_CSRF);
        String actual = request.getParameter(PARAM_CSRF);
        if (!(expected instanceof String) || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                ((String) expected).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                actual.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
