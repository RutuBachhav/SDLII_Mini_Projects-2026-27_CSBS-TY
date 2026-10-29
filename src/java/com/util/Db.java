package com.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC access point. Plain DriverManager, because a college lab project does
 * not need a pool, and one less moving part means one less thing to configure.
 *
 * Every caller uses try-with-resources, so connections, statements and result
 * sets are always closed.
 */
public final class Db {

    private static final String URL = Config.get("foundit.db.url",
            "jdbc:mysql://localhost:3306/foundit_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            + "&characterEncoding=utf8");
    private static final String USER = Config.get("foundit.db.user", "root");
    private static final String PASSWORD = Config.get("foundit.db.password", "");

    static {
        try {
            Class.forName(Config.get("foundit.db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "MySQL Connector/J is not on the classpath. "
                    + "Check WEB-INF/lib/mysql-connector-j-9.7.0.jar", e);
        }
    }

    private Db() {
    }

    /** A connection in auto-commit mode, for single statements. */
    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** A connection with auto-commit off, for multi-statement workflows. */
    public static Connection openTransaction() throws SQLException {
        Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
        c.setAutoCommit(false);
        return c;
    }

    /** Rolls back quietly; used in the catch block of a transaction. */
    public static void rollback(Connection c) {
        if (c != null) {
            try {
                c.rollback();
            } catch (SQLException ignored) {
                // nothing useful can be done here
            }
        }
    }

    /** Describes the configured connection without revealing the password. */
    public static String describe() {
        return URL.split("\\?")[0] + " as " + USER;
    }
}
