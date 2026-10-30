package hackmate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Small helper around the HTTP session so every servlet stores and reads the
 * logged in user in exactly the same way.
 */
public final class SessionUtil {

    /** Session attribute holding the {@code users.user_id} of the logged in user. */
    public static final String USER_ID = "userId";

    /** Session attribute holding the display name of the logged in user. */
    public static final String USER_NAME = "fullName";

    /** Session attribute holding the email of the logged in user. */
    public static final String USER_EMAIL = "email";

    private SessionUtil() {
    }

    /**
     * @return the id of the logged in user, or -1 when nobody is logged in
     */
    public static int getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return -1;
        }
        Object value = session.getAttribute(USER_ID);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return -1;
    }

    /**
     * @return true when the request belongs to a logged in user
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getUserId(request) > 0;
    }

    /**
     * Marks the session as belonging to the given user.
     */
    public static void logIn(HttpServletRequest request, int userId, String fullName, String email) {
        // A new session prevents session fixation, so always create a fresh one.
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, userId);
        session.setAttribute(USER_NAME, fullName);
        session.setAttribute(USER_EMAIL, email);
    }

    /**
     * Reads a single string attribute from the session, never returning null.
     */
    public static String getString(HttpServletRequest request, String name, String fallback) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return fallback;
        }
        Object value = session.getAttribute(name);
        return value == null ? fallback : value.toString();
    }

    /**
     * Removes every HackMate attribute and invalidates the session.
     */
    public static void logOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
