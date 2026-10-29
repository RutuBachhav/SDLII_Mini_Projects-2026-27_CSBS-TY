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

@WebServlet("/book")
public class BookServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(BookServlet.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = Auth.requireUser(request, response);
        if (user == null) {
            return;
        }
        String showId = request.getParameter("showId");
        try {
            String code = bookingDAO.book(user.getId(), Integer.parseInt(showId), request.getParameterValues("seats"));
            Auth.flash(request, "ok", "Booking confirmed.");
            response.sendRedirect(request.getContextPath() + "/ticket?code=" + code);
        } catch (BookingException e) {
            Auth.flash(request, "err", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/seats?showId=" + showId);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Booking failed", e);
            Auth.flash(request, "err", "Could not complete the booking. Please try again.");
            response.sendRedirect(request.getContextPath() + "/seats?showId=" + showId);
        }
    }
}
