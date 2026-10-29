package com.web;

import com.dao.BookingDAO;
import com.entity.User;
import com.util.Auth;
import com.util.BookingException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/cancel")
public class CancelServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CancelServlet.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = Auth.requireUser(request, response);
        if (user == null) {
            return;
        }
        String next = user.isAdmin() && "admin".equals(request.getParameter("from"))
                ? "/admin/bookings"
                : "/bookings";
        try {
            int bookingId = Integer.parseInt(request.getParameter("bookingId"));
            bookingDAO.cancel(bookingId, user.getId(), user.isAdmin());
            Auth.flash(request, "ok", "Booking cancelled. Those seats are free again.");
        } catch (BookingException e) {
            Auth.flash(request, "err", e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cancel failed", e);
            Auth.flash(request, "err", "Could not cancel the booking.");
        }
        response.sendRedirect(request.getContextPath() + next);
    }
}
