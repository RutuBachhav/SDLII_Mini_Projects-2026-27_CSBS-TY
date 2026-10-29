package com.pms.util;

import jakarta.servlet.http.HttpSession;

/** Session attribute names, role constants and session helpers. */
public final class AuthUtil {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_ROLE = "userRole";
    public static final String ATTR_EMAIL = "userEmail";
    public static final String ATTR_STUDENT_ID = "studentId";

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMIN = "ADMIN";

    private AuthUtil() {}

    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute(ATTR_USER_ID) != null;
    }

    public static boolean isStudent(HttpSession session) {
        return ROLE_STUDENT.equals(session == null ? null : session.getAttribute(ATTR_ROLE));
    }

    public static boolean isAdmin(HttpSession session) {
        return ROLE_ADMIN.equals(session == null ? null : session.getAttribute(ATTR_ROLE));
    }

    public static Long currentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object v = session.getAttribute(ATTR_USER_ID);
        return v instanceof Long ? (Long) v : null;
    }

    public static Long currentStudentId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object v = session.getAttribute(ATTR_STUDENT_ID);
        return v instanceof Long ? (Long) v : null;
    }
}
