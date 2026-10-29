package com.web;

import com.util.AppException;
import com.util.ConflictException;
import com.util.DataAccessException;
import com.util.NotFoundException;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared plumbing for the servlets: rendering a view, redirecting, and turning
 * an exception into a page the student can understand.
 *
 * Every request passes through {@link #service}, which is where the error
 * handling lives. A record that is missing or not the user's becomes a 404, a
 * stale workflow step a 409, and anything unexpected a 500 with a generic
 * message - the detail goes to the server log, never to the browser.
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            // The request encoding is set by <request-character-encoding> in
            // web.xml. Setting it here would be too late - the filters have
            // already read a parameter - and only logs a warning.
            super.service(req, res);
        } catch (NotFoundException e) {
            fail(req, res, HttpServletResponse.SC_NOT_FOUND, "Not found", e.getMessage());
        } catch (ConflictException e) {
            fail(req, res, HttpServletResponse.SC_CONFLICT, "Already changed", e.getMessage());
        } catch (AppException e) {
            fail(req, res, HttpServletResponse.SC_BAD_REQUEST, "That did not work", e.getMessage());
        } catch (DataAccessException e) {
            LOG.log(Level.SEVERE, "database failure serving " + req.getRequestURI(), e);
            fail(req, res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database unavailable",
                    "DeadlineBuddy could not reach its database. If this is a fresh install, "
                    + "check that MySQL is running and that db/schema.sql has been applied.");
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "unexpected failure serving " + req.getRequestURI(), e);
            fail(req, res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong",
                    "DeadlineBuddy hit an unexpected problem. Nothing was changed.");
        }
    }

    /**
     * Sends a real status code with the error page. A forward, not a redirect,
     * because a redirect would turn a 403 or 404 into a 302 on the way out.
     */
    private void fail(HttpServletRequest req, HttpServletResponse res, int status,
            String title, String message) throws ServletException, IOException {
        if (res.isCommitted()) {
            return;
        }
        res.reset();
        res.setStatus(status);
        req.setAttribute("errorTitle", title);
        req.setAttribute("errorMessage", message);
        req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, res);
    }

    /** Forwards to a view under WEB-INF, adding what every page needs. */
    protected void render(HttpServletRequest req, HttpServletResponse res, String view)
            throws ServletException, IOException {
        req.setAttribute("csrfToken", Web.csrfToken(req));
        req.setAttribute("flash", Web.takeFlash(req));
        if (req.getAttribute("errors") == null) {
            req.setAttribute("errors", Collections.<String, String>emptyMap());
        }
        req.getRequestDispatcher("/WEB-INF/jsp/" + view + ".jsp").forward(req, res);
    }

    /** Redirect-after-POST. The path is relative to the context root. */
    protected void redirect(HttpServletRequest req, HttpServletResponse res, String path)
            throws IOException {
        res.sendRedirect(req.getContextPath() + path);
    }

    /** Puts a ValidationException's messages where the form can show them. */
    protected void showErrors(HttpServletRequest req, Map<String, String> fieldErrors, String summary) {
        req.setAttribute("errors", fieldErrors);
        req.setAttribute("errorSummary", summary);
    }

    /** The path segment after the servlet's own mapping, e.g. "new" or "". */
    protected String action(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.length() <= 1) {
            return "";
        }
        String action = info.substring(1);
        int slash = action.indexOf('/');
        return slash < 0 ? action : action.substring(0, slash);
    }
}
