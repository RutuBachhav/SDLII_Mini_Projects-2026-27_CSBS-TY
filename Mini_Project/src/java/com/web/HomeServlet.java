package com.web;

import com.dao.MovieDAO;
import com.dao.ShowDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HomeServlet.class.getName());
    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowDAO showDAO = new ShowDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dbError = (String) getServletContext().getAttribute("dbError");
        if (dbError != null) {
            request.setAttribute("error", dbError);
        } else {
            try {
                String query = request.getParameter("q");
                String genre = request.getParameter("genre");
                request.setAttribute("movies", movieDAO.search(query, genre, ""));
                request.setAttribute("genres", movieDAO.genres());
                request.setAttribute("nowCount", movieDAO.countByStatus("NOW_SHOWING"));
                request.setAttribute("theaterCount", movieDAO.countTheaters());
                request.setAttribute("todayCount", showDAO.countOn(LocalDate.now()));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Home page failed", e);
                request.setAttribute("error", "Could not load films. " + e.getMessage());
            }
        }
        request.getRequestDispatcher("/home.jsp").forward(request, response);
    }
}
