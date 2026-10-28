package com.web;

import com.entity.AppRole;
import com.entity.AppUser;
import com.security.AuthorizationService;
import com.security.Permission;
import com.security.PasswordUtil;
import com.security.SessionAuth;
import com.security.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet("/admin/users")
public class UserAdminServlet extends HttpServlet {

    private static final Pattern USERNAME =
            Pattern.compile("^[A-Za-z0-9._-]{3,60}$");

    private final UserDAO users = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if (!Access.require(request, response, Permission.USER_MANAGE)) {
            return;
        }
        render(request, response, null, HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (!Access.require(request, response, Permission.USER_MANAGE)) {
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                create(request, response);
                return;
            }
            if ("toggle".equals(action)) {
                toggle(request, response);
                return;
            }
            render(request, response, "Unknown action.", 400);
        } catch (RuntimeException ex) {
            getServletContext().log("User management action failed", ex);
            render(request, response,
                    "The change could not be saved. Check the server log.",
                    500);
        }
    }

    private void create(HttpServletRequest request,
                        HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String cleanUsername = username == null ? "" : username.trim();
        String cleanRole = role == null
                ? "" : role.trim().toUpperCase(Locale.ROOT);

        if (!USERNAME.matcher(cleanUsername).matches()) {
            render(request, response,
                    "Username must be 3-60 characters using letters, digits, "
                    + "dot, underscore or hyphen.", 400);
            return;
        }
        String problem = PasswordUtil.strengthProblem(password);
        if (problem != null) {
            render(request, response, problem, 400);
            return;
        }
        if (!AuthorizationService.validRole(cleanRole)) {
            render(request, response, "Choose a valid role.", 400);
            return;
        }

        try {
            users.createUser(cleanUsername, password, cleanRole);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            render(request, response, ex.getMessage(), 400);
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/admin/users?msg=created");
    }

    private void toggle(HttpServletRequest request,
                        HttpServletResponse response)
            throws ServletException, IOException {

        long userId;
        try {
            userId = Long.parseLong(request.getParameter("userId"));
        } catch (NumberFormatException ex) {
            render(request, response, "Invalid user id.", 400);
            return;
        }

        HttpSession session = request.getSession(false);
        Long selfId = SessionAuth.userId(session);
        if (selfId != null && selfId == userId) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/users?msg=self");
            return;
        }

        boolean activate = "true".equals(request.getParameter("active"));
        if (!users.setActive(userId, activate)) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/users?msg=notfound");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/admin/users?msg=" + (activate ? "activated" : "deactivated"));
    }

    private void render(HttpServletRequest request,
                        HttpServletResponse response,
                        String error,
                        int status)
            throws ServletException, IOException {

        List<AppUser> all = users.findAll();
        String msg = request.getParameter("msg");

        StringBuilder rows = new StringBuilder();
        if (all.isEmpty()) {
            rows.append("<tr><td colspan=\"5\" class=\"empty\">"
                    + "No user accounts found.</td></tr>");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm");
        HttpSession session = request.getSession(false);
        Long selfId = SessionAuth.userId(session);

        for (AppUser user : all) {
            StringBuilder roles = new StringBuilder();
            if (user.getRoles() != null) {
                for (AppRole role : user.getRoles()) {
                    if (role != null && role.getRoleName() != null) {
                        if (roles.length() > 0) {
                            roles.append(", ");
                        }
                        roles.append(Layout.esc(role.getRoleName()));
                    }
                }
            }

            boolean isSelf = selfId != null
                    && user.getUserId() != null
                    && selfId.longValue() == user.getUserId().longValue();

            String actionCell;
            if (isSelf) {
                actionCell = "<span class=\"badge badge-blue\">You</span>";
            } else {
                boolean nextActive = !user.isActive();
                actionCell = "<form class=\"inline-form\" action=\""
                        + request.getContextPath() + "/admin/users\" method=\"post\">"
                        + Layout.csrfField()
                        + "<input type=\"hidden\" name=\"action\" value=\"toggle\">"
                        + "<input type=\"hidden\" name=\"userId\" value=\""
                        + user.getUserId() + "\">"
                        + "<input type=\"hidden\" name=\"active\" value=\""
                        + nextActive + "\">"
                        + "<button class=\"btn btn-sm "
                        + (nextActive ? "btn-primary" : "btn-danger")
                        + "\" type=\"submit\">"
                        + (nextActive ? "Activate" : "Deactivate")
                        + "</button></form>";
            }

            rows.append("<tr>")
                    .append("<td><span class=\"badge badge-blue\">#")
                    .append(user.getUserId()).append("</span></td>")
                    .append("<td><b>").append(Layout.esc(user.getUsername()))
                    .append("</b></td>")
                    .append("<td>")
                    .append(roles.length() == 0
                            ? "<span class=\"badge badge-red\">NO ROLE</span>"
                            : roles)
                    .append("</td>")
                    .append("<td>")
                    .append(user.isActive()
                            ? "<span class=\"badge badge-green\">ACTIVE</span>"
                            : "<span class=\"badge badge-red\">INACTIVE</span>")
                    .append("</td>")
                    .append("<td>").append(user.getCreatedAt() == null
                            ? "-" : dateFormat.format(user.getCreatedAt()))
                    .append("</td>")
                    .append("<td>").append(actionCell).append("</td>")
                    .append("</tr>");
        }

        response.setStatus(status);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(Layout.open("User Management",
                "Access <span>Control</span>", "users"));

        if (error != null) {
            out.println("<div class=\"narrow\">");
            out.println(Layout.alertErr("Could not save", Layout.esc(error)));
            out.println("</div>");
        } else if (msg != null) {
            String message = null;
            switch (msg) {
                case "created":
                    message = "The user account was created.";
                    break;
                case "activated":
                    message = "The user account was activated.";
                    break;
                case "deactivated":
                    message = "The user account was deactivated.";
                    break;
                case "self":
                    message = "You cannot deactivate your own account.";
                    break;
                case "notfound":
                    message = "The selected user no longer exists.";
                    break;
                default:
                    break;
            }
            if (message != null) {
                out.println("<div class=\"narrow\">");
                out.println(Layout.alertOk("User Management", message));
                out.println("</div>");
            }
        }

        out.println("<div class=\"cards cards-2\">");
        out.println("<div class=\"card\">");
        out.println("<h3>&#10010; Create User</h3>");
        out.println("<p>Only Administrators can manage accounts. "
                + "Passwords are stored as BCrypt hashes.</p>");
        out.println("<form action=\"" + request.getContextPath()
                + "/admin/users\" method=\"post\">");
        out.println(Layout.csrfField());
        out.println("<input type=\"hidden\" name=\"action\" value=\"create\">");
        out.println("<div class=\"form-group\"><label>Username</label>"
                + "<input type=\"text\" name=\"username\" minlength=\"3\" "
                + "maxlength=\"60\" required></div>");
        out.println("<div class=\"form-group\"><label>Password</label>"
                + "<input type=\"password\" name=\"password\" minlength=\"10\" "
                + "required autocomplete=\"new-password\"></div>");
        out.println("<div class=\"form-group\"><label>Role</label>"
                + "<select name=\"role\" required>"
                + "<option value=\"WARDEN\">WARDEN</option>"
                + "<option value=\"VIEWER\">VIEWER</option>"
                + "<option value=\"ADMIN\">ADMIN</option>"
                + "</select></div>");
        out.println("<button class=\"btn btn-primary btn-block\" "
                + "type=\"submit\">Create User</button>");
        out.println("</form></div>");

        out.println("<div class=\"card accent\">");
        out.println("<h3>&#128274; Role Summary</h3>");
        out.println("<div class=\"stats stats-col\">");
        out.println("<div class=\"stat\"><b>ADMIN</b><span>Full access to "
                + "hostels, rooms and user management</span></div>");
        out.println("<div class=\"stat\"><b>WARDEN</b><span>View hostels; "
                + "view, add and update rooms</span></div>");
        out.println("<div class=\"stat\"><b>VIEWER</b><span>Read-only access "
                + "to hostels and rooms</span></div>");
        out.println("</div></div>");
        out.println("</div>");

        out.println(Layout.panel("User Accounts",
                all.size() + " account(s) registered",
                "<table class=\"table\">"
                + "<tr><th>ID</th><th>Username</th><th>Role</th>"
                + "<th>Status</th><th>Created</th><th>Action</th></tr>"
                + rows.toString()
                + "</table>"
                + "<div class=\"btn-row\">"
                + "<a class=\"btn btn-secondary\" href=\""
                + request.getContextPath() + "/index.jsp\">Dashboard</a>"
                + "</div>"));

        out.println(Layout.close());
    }
}
