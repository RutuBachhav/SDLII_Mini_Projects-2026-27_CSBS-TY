package com.labloop.web;

import com.labloop.cdi.UserSession;
import com.labloop.util.LabLoopException;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Helpers shared by all LabLoop servlets.
 * Pattern used everywhere: Servlet (controller) -> EJB service (business logic + DB) -> JSP (view).
 * Servlets never build HTML in Java strings; they put data in request attributes and forward to a JSP.
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** CDI injects the current user's session bean (a different instance for every browser). */
    @Inject
    protected UserSession me;

    protected void render(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    /** Post/Redirect/Get: after a successful POST, redirect so refresh does not resubmit the form. */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void flashOk(HttpServletRequest req, String msg) { req.getSession().setAttribute("flashOk", msg); }

    protected void flashError(HttpServletRequest req, LabLoopException e) {
        req.getSession().setAttribute("flashErr", String.join(" ", e.getMessages()));
    }

    protected void flashError(HttpServletRequest req, String msg) { req.getSession().setAttribute("flashErr", msg); }

    /** Sends 403 and returns false if the user is not lab staff. */
    protected boolean staffOnly(HttpServletResponse resp) throws IOException {
        if (!me.isStaff()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only lab assistants and admins can do this.");
            return false;
        }
        return true;
    }

    protected String actor() { return me.getFullName(); }

    /** URL part after the servlet mapping, e.g. "/equipment/edit" -> "/edit". Never null. */
    protected String action(HttpServletRequest req) {
        String p = req.getPathInfo();
        return (p == null || p.isEmpty()) ? "/" : p;
    }

    // ----- safe parameter parsing (blank -> null, bad format -> validation error) -----

    protected String str(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    protected Long longParam(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null) return null;
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            throw LabLoopException.validation("'" + v + "' is not a valid number for " + name + ".");
        }
    }

    protected int intParam(HttpServletRequest req, String name, int def) {
        String v = str(req, name);
        if (v == null) return def;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw LabLoopException.validation("'" + v + "' is not a whole number for " + name + ".");
        }
    }

    protected double doubleParam(HttpServletRequest req, String name, double def) {
        String v = str(req, name);
        if (v == null) return def;
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            throw LabLoopException.validation("'" + v + "' is not a valid amount for " + name + ".");
        }
    }

    protected LocalDate dateParam(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null) return null;
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw LabLoopException.validation("Invalid date for " + name + ".");
        }
    }

    /** Parses the value of &lt;input type="datetime-local"&gt;, e.g. 2026-10-13T10:00 */
    protected LocalDateTime dateTimeParam(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null) return null;
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e) {
            throw LabLoopException.validation("Invalid date/time for " + name + ".");
        }
    }

    protected <E extends Enum<E>> E enumParam(HttpServletRequest req, String name, Class<E> type) {
        String v = str(req, name);
        if (v == null) return null;
        try {
            return Enum.valueOf(type, v);
        } catch (IllegalArgumentException e) {
            throw LabLoopException.validation("Invalid value for " + name + ".");
        }
    }
}
