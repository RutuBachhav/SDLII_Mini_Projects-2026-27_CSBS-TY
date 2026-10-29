package com.mycompany.rentalmanagement.web;
import com.mycompany.rentalmanagement.util.DBConnection;import com.mycompany.rentalmanagement.util.WebUtil;
import jakarta.servlet.ServletException;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;
import java.io.IOException;import java.math.BigDecimal;import java.sql.*;
@WebServlet("/payments")
public class PaymentServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try(Connection c=DBConnection.getConnection()){
   java.util.List<java.util.Map<String,Object>> rentals=new java.util.ArrayList<>();try(Statement st=c.createStatement();ResultSet x=st.executeQuery("SELECT r.rental_id,c.full_name,v.registration_no,r.total_amount FROM rentals r JOIN customers c ON c.customer_id=r.customer_id JOIN vehicles v ON v.vehicle_id=r.vehicle_id WHERE r.status IN ('ACTIVE','COMPLETED') ORDER BY r.rental_id DESC")){while(x.next()){var m=new java.util.LinkedHashMap<String,Object>();m.put("id",x.getInt(1));m.put("label","Rental #"+x.getInt(1)+" — "+x.getString(2)+" — "+x.getString(3));m.put("amount",x.getBigDecimal(4));rentals.add(m);}}r.setAttribute("rentals",rentals);
   try(Statement st=c.createStatement();ResultSet x=st.executeQuery("SELECT p.*,c.full_name,v.registration_no FROM payments p JOIN rentals r ON r.rental_id=p.rental_id JOIN customers c ON c.customer_id=r.customer_id JOIN vehicles v ON v.vehicle_id=r.vehicle_id ORDER BY p.payment_id DESC")){java.util.List<java.util.Map<String,Object>> rows=new java.util.ArrayList<>();while(x.next()){var m=new java.util.LinkedHashMap<String,Object>();m.put("id",x.getInt("payment_id"));m.put("rentalId",x.getInt("rental_id"));m.put("amount",x.getBigDecimal("amount"));m.put("date",x.getTimestamp("payment_date"));m.put("method",x.getString("payment_method"));m.put("status",x.getString("payment_status"));m.put("customer",x.getString("full_name"));m.put("vehicle",x.getString("registration_no"));rows.add(m);}r.setAttribute("rows",rows);}
   r.getRequestDispatcher("/WEB-INF/views/payments.jsp").forward(r,s);
  }catch(SQLException e){throw new ServletException("Could not load payments.",e);}
 }
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO payments(rental_id,amount,payment_method,payment_status,reference_note) VALUES(?,?,?,?,?)")){
   int rentalId=WebUtil.intParam(r,"rentalId");BigDecimal amount=new BigDecimal(WebUtil.p(r,"amount"));if(amount.signum()<=0)throw new IllegalArgumentException("Payment amount must be greater than zero.");
   p.setInt(1,rentalId);p.setBigDecimal(2,amount);p.setString(3,WebUtil.p(r,"method"));p.setString(4,WebUtil.p(r,"status"));p.setString(5,WebUtil.p(r,"reference"));p.executeUpdate();r.getSession().setAttribute("flash","Payment recorded.");
  }catch(Exception e){r.getSession().setAttribute("flashError","Could not record payment. "+e.getMessage());}
  s.sendRedirect(r.getContextPath()+"/payments");
 }
}
