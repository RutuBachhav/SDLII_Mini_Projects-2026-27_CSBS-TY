package com.web;

import com.dao.NotificationDao;
import com.entity.User;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Shared plumbing for the servlets: rendering a JSP with the attributes every
 * page needs, redirecting with a message, and turning a database failure into
 * something a user can read.
 */
public abstract class BaseServlet extends HttpServlet {

    private final NotificationDao notifications = new NotificationDao();

    /** Forwards to a JSP under /WEB-INF/jsp, which is not reachable by URL. */
    protected void render(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        request.setAttribute("user", user);
        request.setAttribute("csrf", Web.csrfToken(request));
        if (user != null) {
            try {
                request.setAttribute("unread", notifications.countUnread(user.getId()));
            } catch (SQLException e) {
                request.setAttribute("unread", 0);
            }
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(Web.FLASH) != null) {
            request.setAttribute("flash", session.getAttribute(Web.FLASH));
            request.setAttribute("flashKind", session.getAttribute(Web.FLASH_KIND));
            session.removeAttribute(Web.FLASH);
            session.removeAttribute(Web.FLASH_KIND);
        }
        request.getRequestDispatcher("/WEB-INF/jsp/" + view).forward(request, response);
    }

    /** Redirect-after-post, with a message waiting on the other side. */
    protected void redirect(HttpServletRequest request, HttpServletResponse response,
                           String path, String kind, String message) throws IOException {
        Web.flash(request, kind, message);
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /**
     * One place where a SQLException becomes a page. The real message goes to
     * the server log; the user gets a sentence without SQL in it.
     */
    protected void fail(HttpServletRequest request, HttpServletResponse response, SQLException e)
            throws ServletException, IOException {
        log("Database error while handling " + request.getRequestURI(), e);
        request.setAttribute("errorTitle", "The database is not reachable");
        request.setAttribute("errorDetail",
                "FoundIt could not read or write its data. Check that MySQL is running and "
                + "that the connection settings in WEB-INF/classes/foundit.properties are correct.");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        render(request, response, "error.jsp");
    }

    protected void notFound(HttpServletRequest request, HttpServletResponse response,
                            String what) throws ServletException, IOException {
        request.setAttribute("errorTitle", what + " was not found");
        request.setAttribute("errorDetail",
                "The link may be out of date, or the record may have been removed.");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        render(request, response, "error.jsp");
    }

    protected void forbidden(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("errorTitle", "You cannot open this page");
        request.setAttribute("errorDetail",
                "This record belongs to another user. If you think this is a mistake, "
                + "sign in with the account that created it.");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        render(request, response, "error.jsp");
    }
}
