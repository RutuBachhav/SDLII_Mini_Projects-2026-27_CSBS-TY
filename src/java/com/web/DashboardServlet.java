package com.web;

import com.dao.ClaimDao;
import com.dao.ItemDao;
import com.dao.MatchDao;
import com.dao.NotificationDao;
import com.entity.User;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The signed-in user's own view: their reports, their matches, their claims,
 * the claims waiting for them, and their recent notifications.
 *
 * Every query is filtered by the session user's id, so the page cannot show
 * another account's data even by accident.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final ClaimDao claims = new ClaimDao();
    private final MatchDao matches = new MatchDao();
    private final NotificationDao notifications = new NotificationDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        try {
            request.setAttribute("myLost", items.listByReporter(user.getId(), "LOST"));
            request.setAttribute("myFound", items.listByReporter(user.getId(), "FOUND"));
            request.setAttribute("matches", matches.forUser(user.getId(), 8));
            request.setAttribute("myClaims", claims.listByClaimant(user.getId()));
            request.setAttribute("toReview", claims.listForReviewer(user.getId()));
            request.setAttribute("pendingForMe", claims.countPendingForReviewer(user.getId()));
            request.setAttribute("notifications", notifications.listForUser(user.getId(), 6));
            render(request, response, "dashboard.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }
}
