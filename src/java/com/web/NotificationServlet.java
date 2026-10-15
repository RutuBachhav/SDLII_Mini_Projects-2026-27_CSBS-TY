package com.web;

import com.dao.NotificationDao;
import com.entity.User;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** The notification list, and marking items as read. */
@WebServlet({"/notifications", "/notifications/read"})
public class NotificationServlet extends BaseServlet {

    private final NotificationDao notifications = new NotificationDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        try {
            request.setAttribute("items", notifications.listForUser(user.getId(), 50));
            render(request, response, "notifications.jsp");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Web.currentUser(request);
        int id = Web.num(request, "id", 0);
        try {
            if (id > 0) {
                // The user id is part of the UPDATE, so one user cannot mark
                // another user's notification as read.
                notifications.markRead(id, user.getId());
            } else {
                notifications.markAllRead(user.getId());
            }
            String next = Web.str(request, "next");
            redirect(request, response,
                    next.startsWith("/") && !next.startsWith("//") ? next : "/notifications");
        } catch (SQLException e) {
            fail(request, response, e);
        }
    }
}
