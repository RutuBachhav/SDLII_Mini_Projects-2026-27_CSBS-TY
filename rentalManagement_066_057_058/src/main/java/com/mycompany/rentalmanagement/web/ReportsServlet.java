package com.mycompany.rentalmanagement.web;
import com.mycompany.rentalmanagement.util.DBConnection;import jakarta.servlet.ServletException;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;
import java.io.IOException;import java.sql.*;
@WebServlet("/reports")
public class ReportsServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try(Connection c=DBConnection.getConnection();Statement st=c.createStatement()){
   try(ResultSet x=st.executeQuery("SELECT COUNT(*) total, COALESCE(SUM(total_amount),0) rentalsTotal, COALESCE(SUM(late_fee),0) lateTotal FROM rentals WHERE status='COMPLETED'")){x.next();r.setAttribute("completed",x.getInt("total"));r.setAttribute("rentalTotal",x.getBigDecimal("rentalsTotal"));r.setAttribute("lateTotal",x.getBigDecimal("lateTotal"));}
   try(ResultSet x=st.executeQuery("SELECT COALESCE(SUM(amount),0) paid FROM payments WHERE payment_status='PAID'")){x.next();r.setAttribute("paid",x.getBigDecimal("paid"));}
   try(ResultSet x=st.executeQuery("SELECT r.rental_id,c.full_name,v.registration_no,r.rental_date,r.expected_return_date,r.actual_return_date,r.total_amount,r.late_fee,r.status FROM rentals r JOIN customers c ON c.customer_id=r.customer_id JOIN vehicles v ON v.vehicle_id=r.vehicle_id ORDER BY r.rental_id DESC")){java.util.List<java.util.Map<String,Object>> rows=new java.util.ArrayList<>();while(x.next()){var m=new java.util.LinkedHashMap<String,Object>();for(String k:new String[]{"rental_id","rental_date","expected_return_date","actual_return_date","total_amount","late_fee","status"})m.put(k,x.getObject(k));m.put("customer",x.getString("full_name"));m.put("vehicle",x.getString("registration_no"));rows.add(m);}r.setAttribute("rows",rows);}
   r.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(r,s);
  }catch(SQLException e){throw new ServletException("Could not load reports.",e);}
 }
}
