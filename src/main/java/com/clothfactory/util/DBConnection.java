package com.clothfactory.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/cloth_factory?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "cloth_app";
    private static final String DEFAULT_PASSWORD = "cloth_app_dev_password";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL JDBC driver is not available", exception);
        }
        return DriverManager.getConnection(
                environmentValue("DB_URL", DEFAULT_URL),
                environmentValue("DB_USER", DEFAULT_USER),
                environmentValue("DB_PASSWORD", DEFAULT_PASSWORD));
    }

    private static String environmentValue(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}