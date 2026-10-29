package com.web;

import com.dao.BookingDAO;
import com.entity.User;
import com.util.Auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/bookings")
public class MyBookingsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(MyBookingsServlet.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Auth.requireUser(request, response);
        if (user == null) {
            return;
        }
        try {
            request.setAttribute("bookings", bookingDAO.findByUser(user.getId()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Bookings page failed", e);
            request.setAttribute("error", "Could not load your tickets. " + e.getMessage());
        }
        request.getRequestDispatcher("/bookings.jsp").forward(request, response);
    }
}
