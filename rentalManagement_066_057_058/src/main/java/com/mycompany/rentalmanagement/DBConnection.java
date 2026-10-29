package com.mycompany.rentalmanagement;
import java.io.*;import java.sql.*;import java.util.Properties;
public final class DBConnection {
 private static String url="jdbc:mysql://localhost:3306/driveflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", user="root", password="";
 static { try(InputStream in=DBConnection.class.getClassLoader().getResourceAsStream("db.properties")){if(in!=null){Properties p=new Properties();p.load(in);url=p.getProperty("db.url",url);user=p.getProperty("db.username",user);password=p.getProperty("db.password",password);}}catch(IOException e){throw new ExceptionInInitializerError(e);} try{Class.forName("com.mysql.cj.jdbc.Driver");}catch(ClassNotFoundException e){throw new ExceptionInInitializerError("MySQL JDBC driver missing: "+e.getMessage());} }
 private DBConnection(){} public static Connection getConnection() throws SQLException{return DriverManager.getConnection(url,user,password);}
}
