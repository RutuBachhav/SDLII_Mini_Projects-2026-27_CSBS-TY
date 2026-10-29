package com.web;

import com.dao.BookingDAO;
import com.entity.Booking;
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

@WebServlet("/ticket")
public class TicketServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TicketServlet.class.getName());
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = Auth.requireUser(request, response);
        if (user == null) {
            return;
        }
        try {
            Booking booking = bookingDAO.findByCode(request.getParameter("code"));
            if (booking == null || (!user.isAdmin() && booking.getUserId() != user.getId())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("booking", booking);
            request.getRequestDispatcher("/ticket.jsp").forward(request, response);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Ticket page failed", e);
            request.setAttribute("error", "Could not load this ticket.");
            request.getRequestDispatcher("/ticket.jsp").forward(request, response);
        }
    }
}
