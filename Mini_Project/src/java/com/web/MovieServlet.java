package com.web;

import com.dao.MovieDAO;
import com.dao.ShowDAO;
import com.entity.Movie;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/movie")
public class MovieServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(MovieServlet.class.getName());
    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowDAO showDAO = new ShowDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Movie movie = movieDAO.findById(id);
            if (movie == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("movie", movie);
            request.setAttribute("shows", showDAO.findByMovie(id));
            request.getRequestDispatcher("/movie.jsp").forward(request, response);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Movie page failed", e);
            request.setAttribute("error", "Could not load this film. " + e.getMessage());
            request.getRequestDispatcher("/movie.jsp").forward(request, response);
        }
    }
}
