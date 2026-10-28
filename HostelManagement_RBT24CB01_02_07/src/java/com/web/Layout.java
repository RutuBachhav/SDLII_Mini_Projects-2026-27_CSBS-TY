package com.web;

import com.security.AuthorizationService;
import com.security.CsrfUtil;
import com.security.Permission;
import com.security.SessionAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;

/**
 * Shared HTML layout for all authenticated HostelManagement pages.
 * The request context is installed by AuthFilter so every servlet and JSP
 * renders exactly the same role-aware chrome.
 */
public final class Layout {

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private Layout() {
    }

    public static void begin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Context context = new Context();
        context.contextPath = request.getContextPath();
        context.username = SessionAuth.username(session);
        context.roles = SessionAuth.roles(session);
        context.displayRole = SessionAuth.displayRole(session);
        context.csrf = session == null
                ? "" : CsrfUtil.ensureToken(session);
        CURRENT.set(context);
    }

    public static void end() {
        CURRENT.remove();
    }

    private static Context current() {
        Context context = CURRENT.get();
        return context == null ? Context.empty() : context;
    }

    public static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String nav(Context context) {
        String dash = "dashboard".equals(context.active) ? " active" : "";
        String hos = "hostels".equals(context.active) ? " active" : "";
        String rm = "rooms".equals(context.active) ? " active" : "";
        String users = "users".equals(context.active) ? " active" : "";
        String find = "roomchoice".equals(context.active) ? " active" : "";
        String myRequests = "myrequests".equals(context.active)
                ? " active" : "";
        String manageRequests = "managerequests".equals(context.active)
                ? " active" : "";
        String ctx = context.contextPath;

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"sidebar\">")
                .append("<div class=\"logo\">")
                .append("<span class=\"mark\">&#127976;</span>")
                .append("<h2>Hostel Management</h2>")
                .append("<p>Hostel &amp; Room System</p>")
                .append("</div>")
                .append("<div class=\"menu\">")
                .append("<a href=\"").append(ctx).append("/index.jsp\" ")
                .append("class=\"menu-title").append(dash).append("\">")
                .append("<span class=\"ico\">&#9673;</span> Dashboard</a>")
                .append("<a href=\"").append(ctx).append("/hostel.jsp\" ")
                .append("class=\"menu-title").append(hos).append("\">")
                .append("<span class=\"ico\">&#127976;</span> Hostels</a>")
                .append("<div class=\"submenu\">");

        if (allows(context, Permission.HOSTEL_CREATE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/addHostel.jsp\">&#10010; Add Hostel</a>");
        }
        html.append("<a href=\"").append(ctx)
                .append("/viewHostel\">&#9776; All Hostels</a>");
        if (allows(context, Permission.HOSTEL_UPDATE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/updateHostel.jsp\">&#9671; Update Hostel</a>");
        }
        if (allows(context, Permission.HOSTEL_DELETE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/deleteHostel\">&minus; Delete Hostel</a>");
        }

        html.append("</div>")
                .append("<a href=\"").append(ctx).append("/room.jsp\" ")
                .append("class=\"menu-title").append(rm).append("\">")
                .append("<span class=\"ico\">&#128719;</span> Rooms</a>")
                .append("<div class=\"submenu\">");

        if (allows(context, Permission.ROOM_CREATE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/addRoom.jsp\">&#10010; Add Room</a>");
        }
        html.append("<a href=\"").append(ctx)
                .append("/viewRoom\">&#9776; All Rooms</a>");
        if (allows(context, Permission.ROOM_UPDATE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/updateRoom.jsp\">&#9671; Update Room</a>");
        }
        if (allows(context, Permission.ROOM_DELETE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/deleteRoom\">&minus; Delete Room</a>");
        }

        html.append("</div>");
        if (allows(context, Permission.ROOM_SEARCH)) {
            html.append("<a href=\"").append(ctx)
                    .append("/findRoom\" class=\"menu-title")
                    .append(find).append("\">")
                    .append("<span class=\"ico\">&#128269;</span> Find a Room")
                    .append("</a>");
        }
        if (allows(context, Permission.ROOM_REQUEST_READ_OWN)) {
            html.append("<a href=\"").append(ctx)
                    .append("/myRoomRequests\" class=\"menu-title")
                    .append(myRequests).append("\">")
                    .append("<span class=\"ico\">&#128221;</span> My Room ")
                    .append("Requests</a>");
        }
        if (allows(context, Permission.ROOM_REQUEST_MANAGE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/manageRoomRequests\" class=\"menu-title")
                    .append(manageRequests).append("\">")
                    .append("<span class=\"ico\">&#128203;</span> Manage ")
                    .append("Requests</a>");
        }
        if (allows(context, Permission.USER_MANAGE)) {
            html.append("<a href=\"").append(ctx)
                    .append("/admin/users\" class=\"menu-title")
                    .append(users).append("\">")
                    .append("<span class=\"ico\">&#128100;</span> Users</a>");
        }
        html.append("</div></div>");
        return html.toString();
    }

    private static boolean allows(Context context, Permission permission) {
        return AuthorizationService.allowsAny(
                context.roles, permission);
    }

    /** Opens the page: doctype, head, sidebar, topbar, content div. */
    public static String open(String title, String topTitle, String active) {
        Context context = current();
        context.active = active;

        String userLabel = context.username == null
                ? "Guest" : esc(context.username);
        String roleLabel = context.displayRole == null
                ? "GUEST" : esc(context.displayRole);
        String avatar = context.username == null || context.username.isEmpty()
                ? "G" : esc(context.username.substring(0, 1).toUpperCase());
        String logoutForm = context.username == null
                ? ""
                : "<form class=\"logout-form\" action=\""
                + context.contextPath + "/logout\" method=\"post\">"
                + csrfField()
                + "<button class=\"btn btn-logout btn-sm\" type=\"submit\">"
                + "Logout</button></form>";

        return "<!DOCTYPE html>"
                + "<html><head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + esc(title) + " - Hostel Management</title>"
                + "<link rel=\"stylesheet\" href=\""
                + context.contextPath + "/css/style.css\">"
                + "</head><body>"
                + nav(context)
                + "<div class=\"main\">"
                + "<div class=\"topbar\"><h2>" + topTitle + "</h2>"
                + "<div class=\"admin\">"
                + "<span class=\"user-meta\">Welcome, <b>" + userLabel
                + "</b><span class=\"badge badge-role\">" + roleLabel
                + "</span></span>"
                + "<span class=\"avatar\">" + avatar + "</span>"
                + logoutForm
                + "</div></div>"
                + "<div class=\"content\">";
    }

    /** Closes the content, main and body tags. */
    public static String close() {
        return "<div class=\"footer\">HostelManagement &mdash; "
                + "Hostels and Rooms</div>"
                + "</div></div></body></html>";
    }

    public static String csrfField() {
        String token = current().csrf;
        if (token == null || token.isEmpty()) {
            return "";
        }
        return "<input type=\"hidden\" name=\"" + CsrfUtil.PARAM
                + "\" value=\"" + esc(token) + "\">";
    }

    public static String alertOk(String heading, String bodyHtml) {
        return "<div class=\"alert alert-success\">"
                + "<span class=\"ico\">&#10004;</span>"
                + "<div><h4>" + esc(heading) + "</h4><p>"
                + bodyHtml + "</p></div></div>";
    }

    public static String alertErr(String heading, String bodyHtml) {
        return "<div class=\"alert alert-error\">"
                + "<span class=\"ico\">&#9888;</span>"
                + "<div><h4>" + esc(heading) + "</h4><p>"
                + bodyHtml + "</p></div></div>";
    }

    public static String panel(String heading, String sub, String bodyHtml) {
        return "<div class=\"panel\">"
                + "<div class=\"panel-head\"><div><h3>"
                + esc(heading) + "</h3><p>" + esc(sub) + "</p></div></div>"
                + "<div class=\"panel-body\">" + bodyHtml + "</div>"
                + "</div>";
    }

    public static String statusBadge(String status) {
        if (status == null) {
            return "<span class=\"badge badge-blue\">-</span>";
        }
        if ("AVAILABLE".equalsIgnoreCase(status)) {
            return "<span class=\"badge badge-green\">AVAILABLE</span>";
        }
        if ("OCCUPIED".equalsIgnoreCase(status)) {
            return "<span class=\"badge badge-red\">OCCUPIED</span>";
        }
        return "<span class=\"badge badge-blue\">" + esc(status) + "</span>";
    }

    private static final class Context {
        private String contextPath = "";
        private String username;
        private List<String> roles = Collections.emptyList();
        private String displayRole;
        private String csrf = "";
        private String active = "";

        private static Context empty() {
            return new Context();
        }
    }
}
