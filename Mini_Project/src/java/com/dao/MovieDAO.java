package com.dao;

import com.entity.Movie;
import com.util.DBConnection;
import com.util.Html;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    private static final String COLUMNS =
            "movie_id, title, description, genre, language, duration_min, certificate, poster_color, status";

    public List<Movie> search(String query, String genre, String status) throws SQLException {
        String sql = """
                SELECT movie_id, title, description, genre, language, duration_min, certificate, poster_color, status
                FROM movies
                WHERE (? = '' OR title LIKE CONCAT('%', ?, '%') OR genre LIKE CONCAT('%', ?, '%') OR language LIKE CONCAT('%', ?, '%'))
                  AND (? = '' OR genre = ?)
                  AND (? = '' OR status = ?)
                ORDER BY CASE status WHEN 'NOW_SHOWING' THEN 0 ELSE 1 END, title
                """;
        String q = query == null ? "" : query.trim();
        String g = genre == null ? "" : genre.trim();
        String s = status == null ? "" : status.trim();
        List<Movie> movies = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            ps.setString(4, q);
            ps.setString(5, g);
            ps.setString(6, g);
            ps.setString(7, s);
            ps.setString(8, s);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movies.add(map(rs));
                }
            }
        }
        return movies;
    }

    public Movie findById(int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM movies WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<String> genres() throws SQLException {
        String sql = "SELECT DISTINCT genre FROM movies ORDER BY genre";
        List<String> genres = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                genres.add(rs.getString(1));
            }
        }
        return genres;
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM movies WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countTheaters() throws SQLException {
        String sql = "SELECT COUNT(*) FROM theaters";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void save(Movie movie) throws SQLException {
        movie.setPosterColor(Html.color(movie.getPosterColor()));
        if (movie.getId() > 0) {
            String sql = """
                    UPDATE movies
                    SET title = ?, description = ?, genre = ?, language = ?, duration_min = ?,
                        certificate = ?, poster_color = ?, status = ?
                    WHERE movie_id = ?
                    """;
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, movie);
                ps.setInt(9, movie.getId());
                ps.executeUpdate();
            }
            return;
        }
        String sql = """
                INSERT INTO movies (title, description, genre, language, duration_min, certificate, poster_color, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, movie);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    movie.setId(keys.getInt(1));
                }
            }
        }
    }

    public void delete(int id) throws SQLException {
        String check = "SELECT COUNT(*) FROM shows WHERE movie_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(check)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Remove this film's shows before deleting it.");
                }
            }
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM movies WHERE movie_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Movie movie) throws SQLException {
        ps.setString(1, movie.getTitle().trim());
        ps.setString(2, movie.getDescription() == null ? "" : movie.getDescription().trim());
        ps.setString(3, movie.getGenre().trim());
        ps.setString(4, movie.getLanguage().trim());
        ps.setInt(5, movie.getDurationMin());
        ps.setString(6, movie.getCertificate());
        ps.setString(7, movie.getPosterColor());
        ps.setString(8, movie.getStatus());
    }

    private Movie map(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setDescription(rs.getString("description"));
        movie.setGenre(rs.getString("genre"));
        movie.setLanguage(rs.getString("language"));
        movie.setDurationMin(rs.getInt("duration_min"));
        movie.setCertificate(rs.getString("certificate"));
        movie.setPosterColor(rs.getString("poster_color"));
        movie.setStatus(rs.getString("status"));
        return movie;
    }
}
