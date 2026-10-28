package com.mycompany.rentalmanagement.web;
import com.mycompany.rentalmanagement.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
@WebServlet({"/","/dashboard"})
public class DashboardServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try(Connection c=DBConnection.getConnection();Statement st=c.createStatement()){
   r.setAttribute("vehicles",scalar(st,"SELECT COUNT(*) FROM vehicles"));
   r.setAttribute("available",scalar(st,"SELECT COUNT(*) FROM vehicles WHERE status='AVAILABLE'"));
   r.setAttribute("rented",scalar(st,"SELECT COUNT(*) FROM vehicles WHERE status='RENTED'"));
   r.setAttribute("customers",scalar(st,"SELECT COUNT(*) FROM customers"));
   r.setAttribute("activeRentals",scalar(st,"SELECT COUNT(*) FROM rentals WHERE status='ACTIVE'"));
   r.setAttribute("revenue",decimal(st,"SELECT COALESCE(SUM(amount),0) FROM payments WHERE payment_status='PAID'"));
   r.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(r,s);
  }catch(SQLException e){throw new ServletException("Dashboard could not load. Check MySQL connection.",e);}
 }
 private int scalar(Statement s,String q)throws SQLException{try(ResultSet r=s.executeQuery(q)){r.next();return r.getInt(1);}}
 private java.math.BigDecimal decimal(Statement s,String q)throws SQLException{try(ResultSet r=s.executeQuery(q)){r.next();return r.getBigDecimal(1);}}
}
