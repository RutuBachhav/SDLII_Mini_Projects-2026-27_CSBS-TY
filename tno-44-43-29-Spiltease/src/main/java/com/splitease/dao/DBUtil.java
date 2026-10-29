package com.splitease.dao;

import com.splitease.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens JDBC connections to MySQL. Credentials live in db.properties
 * (src/main/resources), never inside the Java code.
 */
public final class DBUtil {

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on the classpath");
            }
            CONFIG.load(in);
            // Explicit load: needed when the driver jar sits inside the WAR on GlassFish
            Class.forName(CONFIG.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() { }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    CONFIG.getProperty("db.url"),
                    CONFIG.getProperty("db.user"),
                    CONFIG.getProperty("db.password"));
        } catch (SQLException e) {
            throw ApiException.serverError("Cannot connect to the database: " + e.getMessage(), e);
        }
    }

    /** Wraps a checked SQLException into the API's unchecked exception. */
    public static ApiException wrap(SQLException e) {
        if ("23000".equals(e.getSQLState())) {      // integrity constraint (duplicate e-mail etc.)
            return ApiException.conflict("Duplicate or invalid reference: " + e.getMessage());
        }
        return ApiException.serverError("Database error: " + e.getMessage(), e);
    }
}
