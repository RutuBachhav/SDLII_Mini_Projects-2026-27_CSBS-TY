package com.web;

import com.dao.BookingDAO;
import com.dao.ShowDAO;
import com.entity.Show;
import com.util.Auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/seats")
public class SeatServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SeatServlet.class.getName());
    private final ShowDAO showDAO = new ShowDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int showId;
        try {
            showId = Integer.parseInt(request.getParameter("showId"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Show show = showDAO.findById(showId);
            if (show == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (Auth.current(request) == null) {
                Auth.rememberReturn(request, "/seats?showId=" + showId);
            }
            request.setAttribute("show", show);
            request.setAttribute("taken", bookingDAO.takenSeats(showId));
            request.getRequestDispatcher("/seats.jsp").forward(request, response);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Seat map failed", e);
            request.setAttribute("error", "Could not load seats. " + e.getMessage());
            request.getRequestDispatcher("/seats.jsp").forward(request, response);
        }
    }
}
