package com.web;

import com.dao.AuditDao;
import com.dao.CategoryDao;
import com.dao.ClaimDao;
import com.dao.ItemDao;
import com.dao.ItemQuery;
import com.dao.MatchDao;
import com.dao.ReturnDao;
import com.dao.UserDao;
import com.entity.Item;
import com.entity.User;
import com.service.MatchService;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The administrator area: statistics, moderation, claims, users and categories.
 *
 * AdminFilter guards every path here, and each action re-reads the session role
 * before it writes anything, so nothing depends on a hidden button.
 */
@WebServlet({"/admin", "/admin/items", "/admin/claims", "/admin/users", "/admin/categories",
             "/admin/rematch"})
public class AdminServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final UserDao users = new UserDao();
    private final ClaimDao claims = new ClaimDao();
    private final ReturnDao returns = new ReturnDao();
    private final MatchDao matches = new MatchDao();
    private final CategoryDao categories = new CategoryDao();
    private final AuditDao audit = new AuditDao();
    private final MatchService matching = new MatchService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            switch (request.getServletPath()) {
                case "/admin/items" -> itemsPage(request, response);
                case "/admin/claims" -> claimsPage(request, response);
                case "/admin/users" -> usersPage(request, response);
                case "/admin/categories" -> categoriesPage(request, response);
                default -> dashboard(request, response);
            }
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = Web.currentUser(request);
        String action = Web.str(request, "action");
        try {
            switch (request.getServletPath()) {
                case "/admin/items" -> moderateItem(request, response, admin, action);
                case "/admin/users" -> manageUser(request, response, admin, action);
                case "/admin/categories" -> manageCategory(request, response, admin, action);
                case "/admin/rematch" -> {
                    int stored = matching.recomputeAll();
                    audit.log(admin.getId(), "MATCHES_REBUILT", "system", null,
                            stored + " suggestions");
                    redirect(request, response, "/admin", "success",
                            "Matching rebuilt across every open report: " + stored
                            + " suggestions on the board.");
                }
                default -> redirect(request, response, "/admin");
            }
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    // ------------------------------------------------------------------ pages

    private void dashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("totalUsers", users.countAll());
        request.setAttribute("openLost", items.countByTypeAndStatus("LOST", "OPEN"));
        request.setAttribute("openFound", items.countByTypeAndStatus("FOUND", "OPEN"));
        request.setAttribute("pendingClaims", claims.countByStatus("PENDING"));
        request.setAttribute("completedReturns", returns.countAll());
        request.setAttribute("totalReports", items.countAll());
        request.setAttribute("totalMatches", matches.countAll());
        request.setAttribute("recentItems", items.recent(6));
        request.setAttribute("recentClaims", claims.listAll("ALL"));
        request.setAttribute("audit", audit.recent(12));
        render(request, response, "admin/dashboard.jsp");
    }

    private void itemsPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        ItemQuery q = new ItemQuery();
        q.setKeyword(Web.str(request, "q"));
        q.setStatus(Web.str(request, "status").isEmpty() ? "ALL" : Web.str(request, "status"));
        q.setType(Web.str(request, "type"));
        q.setPageSize(24);
        q.setPage(Web.num(request, "page", 1));
        int total = items.count(q);
        request.setAttribute("q", q);
        request.setAttribute("results", items.search(q));
        request.setAttribute("total", total);
        request.setAttribute("pages", Math.max(1, (int) Math.ceil(total / 24.0)));
        render(request, response, "admin/items.jsp");
    }

    private void claimsPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String status = Web.str(request, "status");
        request.setAttribute("status", status.isEmpty() ? "ALL" : status);
        request.setAttribute("claims", claims.listAll(status.isEmpty() ? "ALL" : status));
        render(request, response, "admin/claims.jsp");
    }

    private void usersPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String keyword = Web.str(request, "q");
        request.setAttribute("keyword", keyword);
        request.setAttribute("users", users.list(keyword));
        render(request, response, "admin/users.jsp");
    }

    private void categoriesPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("categories", categories.listAllWithCounts());
        render(request, response, "admin/categories.jsp");
    }

    // ------------------------------------------------------------------ actions

    private void moderateItem(HttpServletRequest request, HttpServletResponse response,
                              User admin, String action)
            throws ServletException, IOException, SQLException {
        int id = Web.num(request, "id", 0);
        // Moderating one row should not throw away the filters the administrator
        // was looking at, so the form posts them back and the redirect keeps them.
        String back = "/admin/items" + filterQuery(request);
        Item item = items.findById(id);
        if (item == null) {
            redirect(request, response, back, "error", "That report no longer exists.");
            return;
        }
        if ("close".equals(action)) {
            // Closing instead of deleting: the report, its claims and its history
            // stay in the database, it simply leaves search and matching.
            if (items.setStatus(id, "CLOSED", "OPEN")) {
                matches.deleteForItem(id);
                audit.log(admin.getId(), "REPORT_CLOSED_BY_ADMIN", "item", id, item.getTitle());
                redirect(request, response, back, "success",
                        "Report closed and removed from matching.");
            } else {
                redirect(request, response, back, "error",
                        "Only an open report can be closed.");
            }
        } else if ("reopen".equals(action)) {
            if (items.setStatus(id, "OPEN", "CLOSED")) {
                matching.recomputeFor(items.findById(id));
                audit.log(admin.getId(), "REPORT_REOPENED_BY_ADMIN", "item", id, item.getTitle());
                redirect(request, response, back, "success", "Report reopened.");
            } else {
                redirect(request, response, back, "error",
                        "Only a closed report can be reopened.");
            }
        } else {
            redirect(request, response, back, "error", "Unknown action.");
        }
    }

    /** Rebuilds the browse filters as a query string for a redirect. */
    private String filterQuery(HttpServletRequest request) {
        StringBuilder q = new StringBuilder();
        append(q, "q", Web.str(request, "q"));
        append(q, "type", Web.str(request, "type"));
        append(q, "status", Web.str(request, "status"));
        append(q, "page", Web.str(request, "page"));
        return q.length() == 0 ? "" : "?" + q.substring(1);
    }

    private static void append(StringBuilder q, String name, String value) {
        if (!value.isEmpty()) {
            q.append('&').append(name).append('=')
             .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }

    private void manageUser(HttpServletRequest request, HttpServletResponse response,
                            User admin, String action)
            throws ServletException, IOException, SQLException {
        int id = Web.num(request, "id", 0);
        if (id == admin.getId()) {
            redirect(request, response, "/admin/users", "error",
                    "You cannot change your own account here.");
            return;
        }
        boolean activate = "activate".equals(action);
        users.setActive(id, activate);
        audit.log(admin.getId(), activate ? "USER_ACTIVATED" : "USER_SUSPENDED", "user", id, null);
        redirect(request, response, "/admin/users", "success",
                activate ? "Account reactivated." : "Account suspended. Their history is kept.");
    }

    private void manageCategory(HttpServletRequest request, HttpServletResponse response,
                                User admin, String action)
            throws ServletException, IOException, SQLException {
        String name = Web.clip(Web.str(request, "name"), 60);
        int id = Web.num(request, "id", 0);
        switch (action) {
            case "create" -> {
                if (name == null || name.length() < 3) {
                    redirect(request, response, "/admin/categories", "error",
                            "A category name needs at least 3 characters.");
                    return;
                }
                try {
                    categories.create(name);
                    audit.log(admin.getId(), "CATEGORY_CREATED", "category", null, name);
                    redirect(request, response, "/admin/categories", "success",
                            "Category \"" + name + "\" added.");
                } catch (SQLException e) {
                    redirect(request, response, "/admin/categories", "error",
                            "That category already exists.");
                }
            }
            case "rename" -> {
                if (name == null || name.length() < 3) {
                    redirect(request, response, "/admin/categories", "error",
                            "A category name needs at least 3 characters.");
                    return;
                }
                categories.rename(id, name);
                audit.log(admin.getId(), "CATEGORY_RENAMED", "category", id, name);
                redirect(request, response, "/admin/categories", "success", "Category renamed.");
            }
            case "retire", "restore" -> {
                boolean restore = "restore".equals(action);
                categories.setActive(id, restore);
                audit.log(admin.getId(), restore ? "CATEGORY_RESTORED" : "CATEGORY_RETIRED",
                        "category", id, null);
                redirect(request, response, "/admin/categories", "success",
                        restore
                                ? "Category is offered on report forms again."
                                : "Category retired. Existing reports keep it.");
            }
            default -> redirect(request, response, "/admin/categories", "error", "Unknown action.");
        }
    }
}
