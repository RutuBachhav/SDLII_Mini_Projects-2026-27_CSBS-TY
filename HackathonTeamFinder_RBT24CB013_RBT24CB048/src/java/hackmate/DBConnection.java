package hackmate;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author shekh
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection()
        throws SQLException {
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        throw new SQLException(
            "MySQL Connector/J driver was not found.", e);
    }

    // sslMode=DISABLED: GlassFish sets a global -Djavax.net.ssl.keyStore pointing at
    // its keystore.p12, whose password does not match. JSSE then fails to initialise
    // and MySQL Connector/J's default TLS negotiation aborts the connection with
    // "Cannot open .../keystore.p12 [keystore password was incorrect]".
    // Disabling TLS for the local loopback connection avoids the broken keystore.
    // allowPublicKeyRetrieval is required by MySQL 8's caching_sha2_password auth.
    String url = "jdbc:mysql://localhost:3306/hackmate"
            + "?sslMode=DISABLED&allowPublicKeyRetrieval=true";
    String username = "Username";
    String password = "Your Password";

    return DriverManager.getConnection(url, username, password);
    }
}