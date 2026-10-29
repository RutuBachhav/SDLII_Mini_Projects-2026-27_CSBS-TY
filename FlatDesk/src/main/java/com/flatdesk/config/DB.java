package com.flatdesk.config;

import java.sql.*;

public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/flatdesk_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "XYZ123kau@";

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
