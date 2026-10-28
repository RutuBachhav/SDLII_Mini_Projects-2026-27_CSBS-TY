package com.security;

import com.entity.AppRole;
import com.entity.AppUser;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Authenticated identity stored in the HTTP session.
 */
public final class SessionAuth {

    public static final String USERNAME = "authUser";
    public static final String USER_ID = "authUserId";
    public static final String ROLES = "authRoles";
    public static final String DISPLAY_ROLE = "authRole";

    private SessionAuth() {
    }

    public static boolean isAuthenticated(HttpSession session) {
        return session != null
                && session.getAttribute(USERNAME) instanceof String
                && !roles(session).isEmpty();
    }

    public static String username(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(USERNAME);
        return value instanceof String ? (String) value : null;
    }

    public static Long userId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(USER_ID);
        return value instanceof Long ? (Long) value : null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> roles(HttpSession session) {
        if (session == null) {
            return Collections.emptyList();
        }
        Object value = session.getAttribute(ROLES);
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<?> raw = (List<?>) value;
        List<String> roles = new ArrayList<>(raw.size());
        for (Object item : raw) {
            if (item instanceof String) {
                roles.add(((String) item).toUpperCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableList(roles);
    }

    public static String displayRole(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(DISPLAY_ROLE);
        return value instanceof String ? (String) value : null;
    }

    public static void login(HttpSession session, AppUser user) {
        List<String> roleNames = new ArrayList<>();
        if (user.getRoles() != null) {
            for (AppRole role : user.getRoles()) {
                if (role != null && role.getRoleName() != null) {
                    roleNames.add(role.getRoleName().toUpperCase(Locale.ROOT));
                }
            }
        }
        session.setAttribute(USERNAME, user.getUsername());
        session.setAttribute(USER_ID, user.getUserId());
        session.setAttribute(ROLES, roleNames);
        session.setAttribute(DISPLAY_ROLE,
                AuthorizationService.highestRole(roleNames));
    }
}
