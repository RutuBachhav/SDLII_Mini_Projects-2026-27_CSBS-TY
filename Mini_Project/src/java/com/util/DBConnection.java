package com.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC connection opened from source code. No NetBeans connection wizard,
 * no GlassFish JDBC pool, and no persistence.xml.
 * Change USER and PASSWORD here if the local MySQL account is different.
 */
public final class DBConnection {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String SERVER_URL =
            "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    private static final String URL =
            "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    private static final String USER = "root";

    private static final String PASSWORD = "adity@jAmgeiz3mysql";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC driver not found. Put mysql-connector-j in web/WEB-INF/lib. " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    /** Connection to the server, used only to create the database on first run. */
    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
