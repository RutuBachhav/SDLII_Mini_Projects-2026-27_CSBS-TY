package com.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates movie_booking and the demo catalogue the first time the app starts.
 */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void init() throws SQLException {
        try (Connection server = DBConnection.getServerConnection();
             Statement st = server.createStatement()) {
            st.executeUpdate("""
                    CREATE DATABASE IF NOT EXISTS movie_booking
                    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
                    """);
        }

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(80) NOT NULL,
                        email VARCHAR(120) NOT NULL UNIQUE,
                        password_hash CHAR(64) NOT NULL,
                        phone VARCHAR(20),
                        role VARCHAR(10) NOT NULL DEFAULT 'USER',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS movies (
                        movie_id INT PRIMARY KEY AUTO_INCREMENT,
                        title VARCHAR(150) NOT NULL,
                        description TEXT,
                        genre VARCHAR(60) NOT NULL,
                        language VARCHAR(40) NOT NULL,
                        duration_min INT NOT NULL,
                        certificate VARCHAR(10) NOT NULL,
                        poster_color VARCHAR(7) NOT NULL,
                        status VARCHAR(20) NOT NULL
                    ) ENGINE=InnoDB
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS theaters (
                        theater_id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(120) NOT NULL,
                        city VARCHAR(60) NOT NULL,
                        address VARCHAR(200) NOT NULL
                    ) ENGINE=InnoDB
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS screens (
                        screen_id INT PRIMARY KEY AUTO_INCREMENT,
                        theater_id INT NOT NULL,
                        name VARCHAR(40) NOT NULL,
                        total_rows INT NOT NULL,
                        seats_per_row INT NOT NULL,
                        CONSTRAINT fk_screen_theater FOREIGN KEY (theater_id) REFERENCES theaters(theater_id)
                    ) ENGINE=InnoDB
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS shows (
                        show_id INT PRIMARY KEY AUTO_INCREMENT,
                        movie_id INT NOT NULL,
                        screen_id INT NOT NULL,
                        show_date DATE NOT NULL,
                        show_time TIME NOT NULL,
                        price DECIMAL(8,2) NOT NULL,
                        CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movies(movie_id),
                        CONSTRAINT fk_show_screen FOREIGN KEY (screen_id) REFERENCES screens(screen_id)
                    ) ENGINE=InnoDB
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        booking_id INT PRIMARY KEY AUTO_INCREMENT,
                        user_id INT NOT NULL,
                        show_id INT NOT NULL,
                        booking_code VARCHAR(12) NOT NULL UNIQUE,
                        total_amount DECIMAL(10,2) NOT NULL,
                        seat_count INT NOT NULL,
                        seat_summary VARCHAR(200) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
                        booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(user_id),
                        CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows(show_id)
                    ) ENGINE=InnoDB
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS booking_seats (
                        seat_id INT PRIMARY KEY AUTO_INCREMENT,
                        booking_id INT NOT NULL,
                        show_id INT NOT NULL,
                        seat_label VARCHAR(8) NOT NULL,
                        UNIQUE KEY uniq_show_seat (show_id, seat_label),
                        CONSTRAINT fk_seat_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
                        CONSTRAINT fk_seat_show FOREIGN KEY (show_id) REFERENCES shows(show_id)
                    ) ENGINE=InnoDB
                    """);

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM movies")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seed(con);
                }
            }
        }
    }

    private static void seed(Connection con) throws SQLException {
        con.setAutoCommit(false);
        try {
            int adminId = insertUser(con, "Admin", "admin@cinebook.com", "admin123", "9000000001", "ADMIN");
            int userId = insertUser(con, "Demo User", "user@cinebook.com", "user123", "9000000002", "USER");
            int guestId = insertUser(con, "Guest Buyer", "guest@cinebook.com", "user123", "9000000003", "USER");

            String[][] movies = {
                    {"Inception", "A thief enters dreams to plant an idea that will not stay buried.", "Sci-Fi", "English", "148", "UA", "#12344d", "NOW_SHOWING"},
                    {"Interstellar", "Pilots cross a wormhole looking for a home when Earth begins to fail.", "Sci-Fi", "English", "169", "UA", "#3a2a1a", "NOW_SHOWING"},
                    {"The Dark Knight", "Gotham's masked protector faces a criminal who wants the city to break.", "Action", "English", "152", "UA", "#3d2b4a", "NOW_SHOWING"},
                    {"Dune", "An heir arrives on a desert planet where spice, prophecy, and empire collide.", "Sci-Fi", "English", "155", "UA", "#8a5a2b", "NOW_SHOWING"},
                    {"Parasite", "Two families share one house and a secret that cannot survive the rain.", "Thriller", "Korean", "132", "A", "#1f4d3a", "NOW_SHOWING"},
                    {"Spirited Away", "A girl trapped in a spirit bathhouse has to remember who she still is.", "Animation", "Japanese", "125", "U", "#2a6f8f", "NOW_SHOWING"},
                    {"La La Land", "A pianist and an actress fall in love while chasing separate careers.", "Musical", "English", "128", "U", "#6b2d4a", "NOW_SHOWING"},
                    {"Whiplash", "A drummer pushes past injury for a conductor who only respects perfection.", "Drama", "English", "107", "A", "#7a1e1e", "NOW_SHOWING"},
                    {"Oppenheimer", "The story of the scientist who built the bomb and then had to live with it.", "Biography", "English", "180", "UA", "#4a4638", "COMING_SOON"},
                    {"Past Lives", "Two childhood friends meet again and measure the lives they did not live.", "Romance", "Korean", "106", "U", "#2c4a3e", "COMING_SOON"}
            };
            String movieSql = """
                    INSERT INTO movies (title, description, genre, language, duration_min, certificate, poster_color, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            List<Integer> nowShowing = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(movieSql, Statement.RETURN_GENERATED_KEYS)) {
                for (String[] movie : movies) {
                    ps.setString(1, movie[0]);
                    ps.setString(2, movie[1]);
                    ps.setString(3, movie[2]);
                    ps.setString(4, movie[3]);
                    ps.setInt(5, Integer.parseInt(movie[4]));
                    ps.setString(6, movie[5]);
                    ps.setString(7, movie[6]);
                    ps.setString(8, movie[7]);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next() && "NOW_SHOWING".equals(movie[7])) {
                            nowShowing.add(keys.getInt(1));
                        }
                    }
                }
            }

            int aurora = insertTheater(con, "Aurora Grand", "Pune", "Lane 7, Koregaon Park");
            int nova = insertTheater(con, "Nova Multiplex", "Pune", "Baner Road");
            List<Integer> screenIds = new ArrayList<>();
            screenIds.add(insertScreen(con, aurora, "Screen 1", 8, 10));
            screenIds.add(insertScreen(con, aurora, "Screen 2", 8, 10));
            screenIds.add(insertScreen(con, nova, "Screen 1", 8, 10));
            screenIds.add(insertScreen(con, nova, "Screen 2", 8, 10));

            LocalTime[] times = {
                    LocalTime.of(11, 0),
                    LocalTime.of(14, 30),
                    LocalTime.of(18, 0),
                    LocalTime.of(21, 15)
            };
            BigDecimal[] prices = {
                    new BigDecimal("180.00"),
                    new BigDecimal("250.00"),
                    new BigDecimal("320.00")
            };
            String showSql = "INSERT INTO shows (movie_id, screen_id, show_date, show_time, price) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(showSql)) {
                for (int i = 0; i < nowShowing.size(); i++) {
                    for (int day = 0; day < 7; day++) {
                        int screenId = screenIds.get((i + day) % screenIds.size());
                        BigDecimal price = prices[(i + day) % prices.length];
                        ps.setInt(1, nowShowing.get(i));
                        ps.setInt(2, screenId);
                        ps.setDate(3, Date.valueOf(LocalDate.now().plusDays(day)));
                        ps.setTime(4, Time.valueOf(times[(i / 2 + day) % times.length]));
                        ps.setBigDecimal(5, price);
                        ps.executeUpdate();
                    }
                }
            }

            int[] upcoming = upcomingShowIds(con);
            if (upcoming[0] > 0) {
                seedBooking(con, userId, upcoming[0], new String[]{"A5", "A6"});
                seedBooking(con, guestId, upcoming[0], new String[]{"B2", "B3", "B4", "E8", "E9"});
            }
            if (upcoming[1] > 0) {
                seedBooking(con, guestId, upcoming[1], new String[]{"C4", "C5", "D1"});
            }
            if (adminId < 0) {
                throw new SQLException("Could not create the admin account.");
            }
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    private static int[] upcomingShowIds(Connection con) throws SQLException {
        int[] ids = {-1, -1};
        String sql = """
                SELECT show_id FROM shows
                WHERE show_date > CURDATE()
                   OR (show_date = CURDATE() AND show_time > CURTIME())
                ORDER BY show_date, show_time, show_id
                LIMIT 2
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                ids[0] = rs.getInt(1);
            }
            if (rs.next()) {
                ids[1] = rs.getInt(1);
            }
        }
        return ids;
    }

    private static int insertUser(Connection con, String name, String email, String password, String phone, String role)
            throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, phone, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, phone);
            ps.setString(5, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not create user " + email);
    }

    private static int insertTheater(Connection con, String name, String city, String address) throws SQLException {
        String sql = "INSERT INTO theaters (name, city, address) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, city);
            ps.setString(3, address);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not create theater " + name);
    }

    private static int insertScreen(Connection con, int theaterId, String name, int rows, int seatsPerRow) throws SQLException {
        String sql = "INSERT INTO screens (theater_id, name, total_rows, seats_per_row) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, theaterId);
            ps.setString(2, name);
            ps.setInt(3, rows);
            ps.setInt(4, seatsPerRow);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not create screen " + name);
    }

    private static void seedBooking(Connection con, int userId, int showId, String[] seats) throws SQLException {
        BigDecimal price;
        try (PreparedStatement ps = con.prepareStatement("SELECT price FROM shows WHERE show_id = ?")) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Show " + showId + " was not created.");
                }
                price = rs.getBigDecimal(1);
            }
        }
        BigDecimal total = price.multiply(BigDecimal.valueOf(seats.length));
        String summary = String.join(", ", seats);
        String code = "CBSEED" + showId + userId;
        String sql = """
                INSERT INTO bookings (user_id, show_id, booking_code, total_amount, seat_count, seat_summary, status)
                VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED')
                """;
        int bookingId;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, showId);
            ps.setString(3, code);
            ps.setBigDecimal(4, total);
            ps.setInt(5, seats.length);
            ps.setString(6, summary);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Could not seed a booking.");
                }
                bookingId = keys.getInt(1);
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO booking_seats (booking_id, show_id, seat_label) VALUES (?, ?, ?)")) {
            for (String seat : seats) {
                ps.setInt(1, bookingId);
                ps.setInt(2, showId);
                ps.setString(3, seat);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static void main(String[] args) throws Exception {
        init();
        System.out.println("movie_booking is ready.");
    }
}
