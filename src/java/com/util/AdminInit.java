package com.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Sets the administrator password, once, from the command line.
 *
 * The seeded administrator row has an unusable password hash, so no shipped
 * credential exists anywhere in this project. Running this command is what turns
 * that row into an account somebody can sign in to.
 *
 *   java -cp "build/web/WEB-INF/classes;build/web/WEB-INF/lib/mysql-connector-j-9.7.0.jar" \
 *        com.util.AdminInit admin@foundit.local "a password you choose"
 *
 * It will promote an existing account or create the row if it is missing, and it
 * refuses a password shorter than 10 characters.
 */
public final class AdminInit {

    private AdminInit() {
    }

    public static void main(String[] args) throws SQLException {
        if (args.length != 2) {
            System.out.println("usage: AdminInit <email> <password>");
            System.exit(2);
        }
        String email = args[0].trim().toLowerCase();
        String password = args[1];
        if (password.length() < 10) {
            System.out.println("Refused: choose a password of at least 10 characters.");
            System.exit(2);
        }

        String hash = Passwords.hash(password);
        try (Connection c = Db.open()) {
            System.out.println("Database: " + Db.describe());
            Integer id = findUser(c, email);
            if (id == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users (name, email, password_hash, role) "
                        + "VALUES ('Campus Administrator', ?, ?, 'ADMIN')")) {
                    ps.setString(1, email);
                    ps.setString(2, hash);
                    ps.executeUpdate();
                }
                System.out.println("Created administrator " + email);
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE users SET password_hash = ?, role = 'ADMIN', active = 1 "
                        + "WHERE id = ?")) {
                    ps.setString(1, hash);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                System.out.println("Updated administrator " + email);
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO audit_log (action, entity, detail) "
                    + "VALUES ('ADMIN_PASSWORD_SET', 'user', ?)")) {
                ps.setString(1, email);
                ps.executeUpdate();
            }
        }
        System.out.println("Done. Sign in at /FoundIt/login");
    }

    private static Integer findUser(Connection c, String email) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
