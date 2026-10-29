package com.web;

import com.dao.BookingDAO;
import com.util.Auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/bookings")
public class AdminBookingServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminBookingServlet.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (Auth.requireAdmin(request, response) == null) {
            return;
        }
        try {
            request.setAttribute("bookings", bookingDAO.findAll());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Admin bookings failed", e);
            request.setAttribute("error", "Could not load bookings. " + e.getMessage());
        }
        request.getRequestDispatcher("/adminBookings.jsp").forward(request, response);
    }
}
