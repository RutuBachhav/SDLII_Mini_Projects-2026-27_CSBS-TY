package com.web;

import com.security.AuthorizationService;
import com.security.Permission;
import com.security.SessionAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Central server-side authorization guard used by every servlet.
 */
public final class Access {

    private Access() {
    }

    public static boolean require(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Permission permission)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (!SessionAuth.isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!AuthorizationService.allowsAny(
                SessionAuth.roles(session), permission)) {
            renderForbidden(request, response,
                    "Your role does not allow this operation.");
            return false;
        }
        return true;
    }

    public static void renderForbidden(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String message)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(Layout.open("Access Denied",
                "Access <span>Denied</span>", ""));
        out.println("<div class=\"narrow\">");
        out.println(Layout.alertErr("Permission required",
                Layout.esc(message)));
        out.println("<div class=\"btn-row\">"
                + "<a class=\"btn btn-primary\" href=\""
                + request.getContextPath() + "/index.jsp\">Dashboard</a>"
                + "</div>");
        out.println("</div>");
        out.println(Layout.close());
    }
}
