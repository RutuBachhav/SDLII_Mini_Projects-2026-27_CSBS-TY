package com.dao;

import com.entity.Booking;
import com.entity.Show;
import com.util.BookingException;
import com.util.DBConnection;
import com.util.Seats;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BookingDAO {

    private static final String SELECT = """
            SELECT b.booking_id, b.user_id, b.show_id, b.booking_code, b.total_amount, b.seat_count,
                   b.seat_summary, b.status, b.booked_at, u.name AS user_name,
                   m.title AS movie_title, t.name AS theater_name, s.name AS screen_name,
                   sh.show_date, sh.show_time
            FROM bookings b
            JOIN users u ON u.user_id = b.user_id
            JOIN shows sh ON sh.show_id = b.show_id
            JOIN movies m ON m.movie_id = sh.movie_id
            JOIN screens s ON s.screen_id = sh.screen_id
            JOIN theaters t ON t.theater_id = s.theater_id
            """;

    public Set<String> takenSeats(int showId) throws SQLException {
        String sql = "SELECT seat_label FROM booking_seats WHERE show_id = ?";
        Set<String> taken = new HashSet<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    taken.add(rs.getString(1));
                }
            }
        }
        return taken;
    }

    public String book(int userId, int showId, String[] requested) throws SQLException, BookingException {
        ShowDAO showDAO = new ShowDAO();
        Show show = showDAO.findById(showId);
        if (show == null) {
            throw new BookingException("That show does not exist.");
        }
        LocalDateTime start = LocalDateTime.of(show.getShowDate(), show.getShowTime());
        if (!start.isAfter(LocalDateTime.now())) {
            throw new BookingException("This show has already started.");
        }
        List<String> seats = Seats.normalize(requested, show.getTotalRows(), show.getSeatsPerRow());
        if (seats.isEmpty()) {
            throw new BookingException("Select at least one seat.");
        }
        if (seats.size() > Seats.MAX_PER_BOOKING) {
            throw new BookingException("You can book up to " + Seats.MAX_PER_BOOKING + " seats at a time.");
        }

        String code = "CB" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        BigDecimal total = show.getPrice().multiply(BigDecimal.valueOf(seats.size()));
        String summary = String.join(", ", seats);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                lockShow(con, showId);
                ensureFree(con, showId, seats);
                int bookingId = insertBooking(con, userId, showId, code, total, seats.size(), summary);
                insertSeats(con, bookingId, showId, seats);
                con.commit();
                return code;
            } catch (SQLException e) {
                con.rollback();
                if (e.getErrorCode() == 1062) {
                    throw new BookingException("Those seats were just taken. Choose different ones.");
                }
                throw e;
            } catch (BookingException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<Booking> findByUser(int userId) throws SQLException {
        return query(SELECT + " WHERE b.user_id = ? ORDER BY b.booked_at DESC", userId);
    }

    public List<Booking> findAll() throws SQLException {
        return query(SELECT + " ORDER BY b.booked_at DESC", null);
    }

    public Booking findByCode(String code) throws SQLException {
        List<Booking> list = query(SELECT + " WHERE b.booking_code = ?", code);
        return list.isEmpty() ? null : list.get(0);
    }

    public void cancel(int bookingId, int userId, boolean admin) throws SQLException, BookingException {
        String sql = """
                SELECT b.user_id, b.status, sh.show_date, sh.show_time
                FROM bookings b
                JOIN shows sh ON sh.show_id = b.show_id
                WHERE b.booking_id = ?
                """;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int owner;
                String status;
                LocalDateTime start;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new BookingException("Booking not found.");
                        }
                        owner = rs.getInt("user_id");
                        status = rs.getString("status");
                        start = LocalDateTime.of(
                                rs.getDate("show_date").toLocalDate(),
                                rs.getTime("show_time").toLocalTime());
                    }
                }
                if (!admin && owner != userId) {
                    throw new BookingException("You can only cancel your own tickets.");
                }
                if (!"CONFIRMED".equals(status)) {
                    throw new BookingException("This booking is already cancelled.");
                }
                if (!start.isAfter(LocalDateTime.now())) {
                    throw new BookingException("This show has already started, so the ticket cannot be cancelled.");
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM booking_seats WHERE booking_id = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } catch (BookingException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void lockShow(Connection con, int showId) throws SQLException, BookingException {
        try (PreparedStatement ps = con.prepareStatement("SELECT show_id FROM shows WHERE show_id = ? FOR UPDATE")) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new BookingException("That show does not exist.");
                }
            }
        }
    }

    private void ensureFree(Connection con, int showId, List<String> seats) throws SQLException, BookingException {
        StringBuilder sql = new StringBuilder("SELECT seat_label FROM booking_seats WHERE show_id = ? AND seat_label IN (");
        for (int i = 0; i < seats.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(") FOR UPDATE");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setInt(1, showId);
            for (int i = 0; i < seats.size(); i++) {
                ps.setString(i + 2, seats.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new BookingException("Seat " + rs.getString(1) + " is already booked.");
                }
            }
        }
    }

    private int insertBooking(Connection con, int userId, int showId, String code, BigDecimal total, int count, String summary)
            throws SQLException {
        String sql = """
                INSERT INTO bookings (user_id, show_id, booking_code, total_amount, seat_count, seat_summary, status)
                VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED')
                """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, showId);
            ps.setString(3, code);
            ps.setBigDecimal(4, total);
            ps.setInt(5, count);
            ps.setString(6, summary);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not save the booking.");
    }

    private void insertSeats(Connection con, int bookingId, int showId, List<String> seats) throws SQLException {
        String sql = "INSERT INTO booking_seats (booking_id, show_id, seat_label) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String seat : seats) {
                ps.setInt(1, bookingId);
                ps.setInt(2, showId);
                ps.setString(3, seat);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Booking> query(String sql, Object param) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param instanceof Integer id) {
                ps.setInt(1, id);
            } else if (param instanceof String code) {
                ps.setString(1, code);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(map(rs));
                }
            }
        }
        return bookings;
    }

    private Booking map(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("booking_id"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setShowId(rs.getInt("show_id"));
        booking.setUserName(rs.getString("user_name"));
        booking.setBookingCode(rs.getString("booking_code"));
        booking.setMovieTitle(rs.getString("movie_title"));
        booking.setTheaterName(rs.getString("theater_name"));
        booking.setScreenName(rs.getString("screen_name"));
        booking.setShowDate(rs.getDate("show_date").toLocalDate());
        booking.setShowTime(rs.getTime("show_time").toLocalTime());
        booking.setTotalAmount(rs.getBigDecimal("total_amount"));
        booking.setSeatCount(rs.getInt("seat_count"));
        booking.setSeatSummary(rs.getString("seat_summary"));
        booking.setStatus(rs.getString("status"));
        Timestamp bookedAt = rs.getTimestamp("booked_at");
        if (bookedAt != null) {
            booking.setBookedAt(bookedAt.toLocalDateTime());
        }
        return booking;
    }
}
