package com.mycompany.rentalmanagement.dao;
import com.mycompany.rentalmanagement.DBConnection;import java.sql.*;
public final class DAOUtil {private DAOUtil(){} public static Connection open() throws SQLException{return DBConnection.getConnection();} public static String like(String s){return "%"+(s==null?"":s.trim())+"%";} }
