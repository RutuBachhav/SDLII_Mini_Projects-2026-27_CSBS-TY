package com.parkease.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {
    private static final String URL =
        "jdbc:mysql://localhost:3306/parkease_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String DB_USER = "root";
    // Change this to your MySQL password. If your root account has no password, keep it empty.
    private static final String DB_PASSWORD = "root@123#";

    private DBUtil() { }

    public static Connection getConnection() throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(
        URL, DB_USER, DB_PASSWORD
    );
}
}
