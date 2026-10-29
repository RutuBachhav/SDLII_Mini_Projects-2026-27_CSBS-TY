package com.dao;

import com.entity.Screen;
import com.entity.Show;
import com.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShowDAO {

    private static final String SELECT = """
            SELECT sh.show_id, sh.movie_id, sh.screen_id, sh.show_date, sh.show_time, sh.price,
                   m.title AS movie_title, t.name AS theater_name, t.city, s.name AS screen_name,
                   s.total_rows, s.seats_per_row,
                   (s.total_rows * s.seats_per_row) - (
                       SELECT COUNT(*) FROM booking_seats bs WHERE bs.show_id = sh.show_id
                   ) AS seats_left
            FROM shows sh
            JOIN movies m ON m.movie_id = sh.movie_id
            JOIN screens s ON s.screen_id = sh.screen_id
            JOIN theaters t ON t.theater_id = s.theater_id
            """;

    public List<Show> findByMovie(int movieId) throws SQLException {
        String sql = SELECT + """
                 WHERE sh.movie_id = ?
                   AND (sh.show_date > CURDATE() OR (sh.show_date = CURDATE() AND sh.show_time > CURTIME()))
                 ORDER BY sh.show_date, sh.show_time, t.name
                """;
        return query(sql, movieId);
    }

    public Show findById(int showId) throws SQLException {
        String sql = SELECT + " WHERE sh.show_id = ?";
        List<Show> shows = query(sql, showId);
        return shows.isEmpty() ? null : shows.get(0);
    }

    public List<Show> findAll() throws SQLException {
        return query(SELECT + " ORDER BY sh.show_date, sh.show_time, m.title", null);
    }

    public List<Screen> screens() throws SQLException {
        String sql = """
                SELECT s.screen_id, s.name, s.total_rows, s.seats_per_row, t.name AS theater_name, t.city
                FROM screens s
                JOIN theaters t ON t.theater_id = s.theater_id
                ORDER BY t.name, s.name
                """;
        List<Screen> screens = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Screen screen = new Screen();
                screen.setId(rs.getInt("screen_id"));
                screen.setName(rs.getString("name"));
                screen.setTotalRows(rs.getInt("total_rows"));
                screen.setSeatsPerRow(rs.getInt("seats_per_row"));
                screen.setTheaterName(rs.getString("theater_name"));
                screen.setCity(rs.getString("city"));
                screens.add(screen);
            }
        }
        return screens;
    }

    public int countOn(LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM shows WHERE show_date = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void insert(int movieId, int screenId, LocalDate date, java.time.LocalTime time, java.math.BigDecimal price)
            throws SQLException {
        String sql = "INSERT INTO shows (movie_id, screen_id, show_date, show_time, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.setInt(2, screenId);
            ps.setDate(3, Date.valueOf(date));
            ps.setTime(4, Time.valueOf(time));
            ps.setBigDecimal(5, price);
            ps.executeUpdate();
        }
    }

    public void delete(int showId) throws SQLException {
        String check = "SELECT COUNT(*) FROM bookings WHERE show_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(check)) {
                ps.setInt(1, showId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("This show already has bookings, so it cannot be removed.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM shows WHERE show_id = ?")) {
                ps.setInt(1, showId);
                ps.executeUpdate();
            }
        }
    }

    private List<Show> query(String sql, Integer id) throws SQLException {
        List<Show> shows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (id != null) {
                ps.setInt(1, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shows.add(map(rs));
                }
            }
        }
        return shows;
    }

    private Show map(ResultSet rs) throws SQLException {
        Show show = new Show();
        show.setId(rs.getInt("show_id"));
        show.setMovieId(rs.getInt("movie_id"));
        show.setScreenId(rs.getInt("screen_id"));
        show.setMovieTitle(rs.getString("movie_title"));
        show.setTheaterName(rs.getString("theater_name"));
        show.setCity(rs.getString("city"));
        show.setScreenName(rs.getString("screen_name"));
        show.setShowDate(rs.getDate("show_date").toLocalDate());
        show.setShowTime(rs.getTime("show_time").toLocalTime());
        show.setPrice(rs.getBigDecimal("price"));
        show.setTotalRows(rs.getInt("total_rows"));
        show.setSeatsPerRow(rs.getInt("seats_per_row"));
        show.setSeatsLeft(rs.getInt("seats_left"));
        return show;
    }
}
