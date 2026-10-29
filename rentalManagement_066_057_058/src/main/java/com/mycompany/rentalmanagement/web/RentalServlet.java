package com.mycompany.rentalmanagement.web;
import com.mycompany.rentalmanagement.util.DBConnection;import com.mycompany.rentalmanagement.util.WebUtil;
import jakarta.servlet.ServletException;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;
import java.io.IOException;import java.sql.*;import java.math.BigDecimal;import java.time.LocalDate;import java.time.temporal.ChronoUnit;
@WebServlet("/rentals")
public class RentalServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try(Connection c=DBConnection.getConnection()){
   java.util.List<java.util.Map<String,Object>> customers=new java.util.ArrayList<>();try(Statement st=c.createStatement();ResultSet x=st.executeQuery("SELECT customer_id,full_name,phone FROM customers ORDER BY full_name")){while(x.next()){var m=new java.util.LinkedHashMap<String,Object>();m.put("id",x.getInt(1));m.put("name",x.getString(2));m.put("phone",x.getString(3));customers.add(m);}}r.setAttribute("customers",customers);
   java.util.List<java.util.Map<String,Object>> vehicles=new java.util.ArrayList<>();try(Statement st=c.createStatement();ResultSet x=st.executeQuery("SELECT vehicle_id,registration_no,brand,vehicle_name,daily_rate FROM vehicles WHERE status='AVAILABLE' ORDER BY brand,vehicle_name")){while(x.next()){var m=new java.util.LinkedHashMap<String,Object>();m.put("id",x.getInt(1));m.put("label",x.getString(2)+" — "+x.getString(3)+" "+x.getString(4));m.put("rate",x.getBigDecimal(5));vehicles.add(m);}}r.setAttribute("vehicles",vehicles);
   String q="%"+WebUtil.p(r,"q")+"%";try(PreparedStatement p=c.prepareStatement("SELECT r.*,c.full_name,v.registration_no,v.brand,v.vehicle_name FROM rentals r JOIN customers c ON c.customer_id=r.customer_id JOIN vehicles v ON v.vehicle_id=r.vehicle_id WHERE c.full_name LIKE ? OR v.registration_no LIKE ? ORDER BY r.rental_id DESC")){p.setString(1,q);p.setString(2,q);try(ResultSet x=p.executeQuery()){java.util.List<java.util.Map<String,Object>> rows=new java.util.ArrayList<>();while(x.next()){var m=new java.util.LinkedHashMap<String,Object>();for(String k:new String[]{"rental_id","rental_date","expected_return_date","actual_return_date","daily_rate_at_booking","total_amount","late_fee","status"})m.put(k,x.getObject(k));m.put("customer",x.getString("full_name"));m.put("vehicle",x.getString("registration_no")+" "+x.getString("brand")+" "+x.getString("vehicle_name"));rows.add(m);}r.setAttribute("rows",rows);}}
   r.getRequestDispatcher("/WEB-INF/views/rentals.jsp").forward(r,s);
  }catch(SQLException e){throw new ServletException("Could not load rentals.",e);}
 }
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  String action=WebUtil.p(r,"action");
  try(Connection c=DBConnection.getConnection()){
   if("return".equals(action)){int id=WebUtil.intParam(r,"rentalId");c.setAutoCommit(false);try{
    LocalDate actual=LocalDate.now();LocalDate expected;
    BigDecimal rate;int vehicleId;
    try(PreparedStatement p=c.prepareStatement("SELECT vehicle_id,expected_return_date,daily_rate_at_booking,status FROM rentals WHERE rental_id=? FOR UPDATE")){p.setInt(1,id);try(ResultSet x=p.executeQuery()){if(!x.next()||!"ACTIVE".equals(x.getString("status")))throw new SQLException("Rental is not active.");vehicleId=x.getInt("vehicle_id");expected=x.getDate("expected_return_date").toLocalDate();rate=x.getBigDecimal("daily_rate_at_booking");}}
    long lateDays=Math.max(0,ChronoUnit.DAYS.between(expected,actual));BigDecimal fee=rate.multiply(BigDecimal.valueOf(lateDays)).multiply(new BigDecimal("0.50"));
    try(PreparedStatement p=c.prepareStatement("UPDATE rentals SET actual_return_date=?,late_fee=?,status='COMPLETED',total_amount=total_amount+? WHERE rental_id=?")){p.setDate(1,Date.valueOf(actual));p.setBigDecimal(2,fee);p.setBigDecimal(3,fee);p.setInt(4,id);p.executeUpdate();}
    try(PreparedStatement p=c.prepareStatement("UPDATE vehicles SET status='AVAILABLE' WHERE vehicle_id=?")){p.setInt(1,vehicleId);p.executeUpdate();}
    c.commit();r.getSession().setAttribute("flash","Vehicle returned. Late fee added: ₹"+fee);
   }catch(Exception ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
   }else{
    int customerId=WebUtil.intParam(r,"customerId"),vehicleId=WebUtil.intParam(r,"vehicleId");LocalDate start=LocalDate.parse(WebUtil.p(r,"rentalDate")),end=LocalDate.parse(WebUtil.p(r,"returnDate"));
    if(end.isBefore(start))throw new IllegalArgumentException("Return date must be on or after rental date.");
    c.setAutoCommit(false);try{
     BigDecimal rate;
     try(PreparedStatement p=c.prepareStatement("SELECT daily_rate FROM vehicles WHERE vehicle_id=? AND status='AVAILABLE' FOR UPDATE")){p.setInt(1,vehicleId);try(ResultSet x=p.executeQuery()){if(!x.next())throw new SQLException("Vehicle is no longer available.");rate=x.getBigDecimal(1);}}
     try(PreparedStatement p=c.prepareStatement("SELECT customer_id FROM customers WHERE customer_id=?")){p.setInt(1,customerId);try(ResultSet x=p.executeQuery()){if(!x.next())throw new SQLException("Choose a valid customer.");}}
     long days=Math.max(1,ChronoUnit.DAYS.between(start,end));BigDecimal total=rate.multiply(BigDecimal.valueOf(days));
     int rentalId;
     try(PreparedStatement p=c.prepareStatement("INSERT INTO rentals(customer_id,vehicle_id,rental_date,expected_return_date,daily_rate_at_booking,total_amount,status) VALUES(?,?,?,?,?,?,'ACTIVE')",Statement.RETURN_GENERATED_KEYS)){p.setInt(1,customerId);p.setInt(2,vehicleId);p.setDate(3,Date.valueOf(start));p.setDate(4,Date.valueOf(end));p.setBigDecimal(5,rate);p.setBigDecimal(6,total);p.executeUpdate();try(ResultSet x=p.getGeneratedKeys()){x.next();rentalId=x.getInt(1);}}
     try(PreparedStatement p=c.prepareStatement("UPDATE vehicles SET status='RENTED' WHERE vehicle_id=?")){p.setInt(1,vehicleId);p.executeUpdate();}
     c.commit();r.getSession().setAttribute("flash","Rental #"+rentalId+" created. Base amount: ₹"+total);
    }catch(Exception ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
   }
  }catch(Exception e){r.getSession().setAttribute("flashError","Rental action failed: "+e.getMessage());}
  s.sendRedirect(r.getContextPath()+"/rentals");
 }
}
