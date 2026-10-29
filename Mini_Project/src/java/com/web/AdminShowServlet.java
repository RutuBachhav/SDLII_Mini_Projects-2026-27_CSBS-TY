package com.web;

import com.dao.MovieDAO;
import com.dao.ShowDAO;
import com.util.Auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/shows")
public class AdminShowServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminShowServlet.class.getName());
    private final ShowDAO showDAO = new ShowDAO();
    private final MovieDAO movieDAO = new MovieDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (Auth.requireAdmin(request, response) == null) {
            return;
        }
        try {
            request.setAttribute("shows", showDAO.findAll());
            request.setAttribute("movies", movieDAO.search("", "", ""));
            request.setAttribute("screens", showDAO.screens());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Admin shows failed", e);
            request.setAttribute("error", "Could not load shows. " + e.getMessage());
        }
        request.getRequestDispatcher("/adminShows.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Auth.requireAdmin(request, response) == null) {
            return;
        }
        try {
            if ("delete".equals(request.getParameter("action"))) {
                showDAO.delete(Integer.parseInt(request.getParameter("showId")));
                Auth.flash(request, "ok", "Show removed.");
            } else {
                int movieId = Integer.parseInt(request.getParameter("movieId"));
                int screenId = Integer.parseInt(request.getParameter("screenId"));
                LocalDate date = LocalDate.parse(request.getParameter("showDate"));
                LocalTime time = LocalTime.parse(request.getParameter("showTime"));
                BigDecimal price = new BigDecimal(request.getParameter("price").trim());
                if (date.isBefore(LocalDate.now())) {
                    throw new SQLException("Show date cannot be in the past.");
                }
                if (price.signum() <= 0 || price.compareTo(new BigDecimal("10000")) > 0) {
                    throw new SQLException("Price must be between 1 and 10000.");
                }
                if (movieDAO.findById(movieId) == null) {
                    throw new SQLException("Choose a film.");
                }
                showDAO.insert(movieId, screenId, date, time, price);
                Auth.flash(request, "ok", "Show added.");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            Auth.flash(request, "err", "Enter a valid date, time, and price.");
        } catch (SQLException e) {
            Auth.flash(request, "err", e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Save show failed", e);
            Auth.flash(request, "err", "Could not save the show.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/shows");
    }
}
