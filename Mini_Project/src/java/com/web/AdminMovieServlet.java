package com.web;

import com.dao.MovieDAO;
import com.entity.Movie;
import com.entity.User;
import com.util.Auth;
import com.util.Html;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/movies")
public class AdminMovieServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminMovieServlet.class.getName());
    private final MovieDAO movieDAO = new MovieDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (Auth.requireAdmin(request, response) == null) {
            return;
        }
        try {
            request.setAttribute("movies", movieDAO.search("", "", ""));
            String edit = request.getParameter("edit");
            if (edit != null && !edit.isBlank()) {
                request.setAttribute("editMovie", movieDAO.findById(Integer.parseInt(edit)));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Admin movies failed", e);
            request.setAttribute("error", "Could not load films. " + e.getMessage());
        }
        request.getRequestDispatcher("/adminMovies.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        User admin = Auth.requireAdmin(request, response);
        if (admin == null) {
            return;
        }
        String action = request.getParameter("action");
        try {
            if ("delete".equals(action)) {
                movieDAO.delete(Integer.parseInt(request.getParameter("movieId")));
                Auth.flash(request, "ok", "Film removed.");
            } else {
                Movie movie = read(request);
                String error = validate(movie);
                if (error != null) {
                    Auth.flash(request, "err", error);
                    response.sendRedirect(request.getContextPath() + "/admin/movies");
                    return;
                }
                boolean isNew = movie.getId() == 0;
                movieDAO.save(movie);
                Auth.flash(request, "ok", isNew ? "Film added." : "Film saved.");
            }
        } catch (SQLException e) {
            Auth.flash(request, "err", e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Save movie failed", e);
            Auth.flash(request, "err", "Could not save the film.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/movies");
    }

    private Movie read(HttpServletRequest request) {
        Movie movie = new Movie();
        String id = request.getParameter("movieId");
        if (id != null && !id.isBlank()) {
            movie.setId(Integer.parseInt(id));
        }
        movie.setTitle(trim(request.getParameter("title")));
        movie.setDescription(trim(request.getParameter("description")));
        movie.setGenre(trim(request.getParameter("genre")));
        movie.setLanguage(trim(request.getParameter("language")));
        movie.setCertificate(trim(request.getParameter("certificate")));
        movie.setPosterColor(Html.color(trim(request.getParameter("posterColor"))));
        movie.setStatus(trim(request.getParameter("status")));
        try {
            movie.setDurationMin(Integer.parseInt(trim(request.getParameter("durationMin"))));
        } catch (NumberFormatException e) {
            movie.setDurationMin(0);
        }
        return movie;
    }

    private String validate(Movie movie) {
        if (movie.getTitle().length() < 1) {
            return "Title is required.";
        }
        if (movie.getGenre().isEmpty() || movie.getLanguage().isEmpty()) {
            return "Genre and language are required.";
        }
        if (movie.getDurationMin() < 1 || movie.getDurationMin() > 400) {
            return "Duration must be between 1 and 400 minutes.";
        }
        if (!movie.getCertificate().matches("U|UA|A")) {
            return "Certificate must be U, UA, or A.";
        }
        if (!movie.getStatus().matches("NOW_SHOWING|COMING_SOON")) {
            return "Status must be now showing or coming soon.";
        }
        return null;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
