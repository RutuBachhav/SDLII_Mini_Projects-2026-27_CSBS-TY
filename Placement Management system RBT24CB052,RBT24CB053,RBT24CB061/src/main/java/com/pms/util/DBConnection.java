package com.pms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MySQL JDBC connection factory for the placement_db database.
 *
 * <p>Credentials are read from environment variables and never hardcoded:
 * <ul>
 *   <li>{@code PMS_DB_USER} - MySQL username</li>
 *   <li>{@code PMS_DB_PASSWORD} - MySQL password</li>
 * </ul>
 */
public final class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/placement_db?sslMode=DISABLED&allowPublicKeyRetrieval=true";

    private DBConnection() {
        // Utility class - prevent instantiation.
    }

    /**
     * Returns a new JDBC connection to placement_db.
     *
     * @return a live {@link Connection}; caller must close it (use try-with-resources)
     * @throws SQLException if credentials are missing or the connection fails.
     *         The exception message never contains the password.
     */
    public static Connection getConnection() throws SQLException {
        String user = System.getenv("PMS_DB_USER");
        String password = System.getenv("PMS_DB_PASSWORD");

        if (user == null || user.isBlank()) {
            throw new SQLException(
                    "Database credentials are not configured (PMS_DB_USER is missing or empty).");
        }
        if (password == null) {
            throw new SQLException(
                    "Database credentials are not configured (PMS_DB_PASSWORD is missing).");
        }

        // MySQL Connector/J is auto-registered via ServiceLoader (JDBC 4+),
        // so no Class.forName() is required.
        return DriverManager.getConnection(URL, user, password);
    }
}
