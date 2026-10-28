package com.mycompany.rentalmanagement.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private DBConnection() {}
    public static Connection getConnection() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("MySQL JDBC driver is missing from deployment.", e); }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
