package com.hospital.web;

import com.hospital.module.AppUser;
import com.hospital.module.Role;
import java.security.SecureRandom;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** Session + CSRF helpers. The role always comes from the server session. */
public final class SessionUtil {

    private SessionUtil() {
    }

    public static void login(HttpServletRequest req, AppUser user) {
        HttpSession old = req.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession s = req.getSession(true);
        s.setAttribute("uid", user.getId());
        s.setAttribute("urole", user.getRole().name());
        s.setAttribute("uname", user.getFullName());
        s.setAttribute("csrf", token());
        s.setMaxInactiveInterval(30 * 60);
    }

    public static void logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) {
            s.invalidate();
        }
    }

    public static Long userId(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return null;
        }
        Object v = s.getAttribute("uid");
        return v instanceof Long ? (Long) v : null;
    }

    public static Role role(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return null;
        }
        Object v = s.getAttribute("urole");
        if (v == null) {
            return null;
        }
        try {
            return Role.valueOf(String.valueOf(v));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String userName(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return null;
        }
        Object v = s.getAttribute("uname");
        return v == null ? null : String.valueOf(v);
    }

    public static String csrf(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) {
            return null;
        }
        Object v = s.getAttribute("csrf");
        if (v == null) {
            v = token();
            s.setAttribute("csrf", v);
        }
        return String.valueOf(v);
    }

    public static boolean validCsrf(HttpServletRequest req) {
        String session = csrf(req);
        String sent = req.getParameter("_csrf");
        return session != null && sent != null && session.equals(sent);
    }

    private static String token() {
        byte[] b = new byte[24];
        new SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(Character.forDigit((x >> 4) & 0xF, 16));
            sb.append(Character.forDigit(x & 0xF, 16));
        }
        return sb.toString();
    }
}
