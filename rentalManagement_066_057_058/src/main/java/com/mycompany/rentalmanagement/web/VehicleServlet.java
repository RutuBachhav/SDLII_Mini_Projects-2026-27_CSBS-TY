package com.mycompany.rentalmanagement.web;
import com.mycompany.rentalmanagement.util.DBConnection;
import com.mycompany.rentalmanagement.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
@WebServlet("/vehicles")
public class VehicleServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  String action=WebUtil.p(r,"action");
  try(Connection c=DBConnection.getConnection()){
   if("delete".equals(action)){try(PreparedStatement p=c.prepareStatement("DELETE FROM vehicles WHERE vehicle_id=? AND status<>'RENTED'")){p.setInt(1,WebUtil.intParam(r,"id"));if(p.executeUpdate()==0)r.getSession().setAttribute("flashError","Vehicle is rented or does not exist.");else r.getSession().setAttribute("flash","Vehicle deleted.");}s.sendRedirect(r.getContextPath()+"/vehicles");return;}
   if("edit".equals(action)){try(PreparedStatement p=c.prepareStatement("SELECT * FROM vehicles WHERE vehicle_id=?")){p.setInt(1,WebUtil.intParam(r,"id"));try(ResultSet rs=p.executeQuery()){if(rs.next()){r.setAttribute("edit",rs.getInt("vehicle_id"));r.setAttribute("reg",rs.getString("registration_no"));r.setAttribute("name",rs.getString("vehicle_name"));r.setAttribute("brand",rs.getString("brand"));r.setAttribute("type",rs.getString("vehicle_type"));r.setAttribute("year",rs.getObject("model_year"));r.setAttribute("rate",rs.getBigDecimal("daily_rate"));r.setAttribute("status",rs.getString("status"));}}}}
   r.setAttribute("q",WebUtil.p(r,"q"));
   try(PreparedStatement p=c.prepareStatement("SELECT * FROM vehicles WHERE registration_no LIKE ? OR vehicle_name LIKE ? OR brand LIKE ? ORDER BY vehicle_id DESC")){String q="%"+WebUtil.p(r,"q")+"%";for(int i=1;i<=3;i++)p.setString(i,q);try(ResultSet rs=p.executeQuery()){java.util.List<java.util.Map<String,Object>> rows=new java.util.ArrayList<>();while(rs.next()){java.util.Map<String,Object> m=new java.util.LinkedHashMap<>();m.put("id",rs.getInt("vehicle_id"));m.put("reg",rs.getString("registration_no"));m.put("name",rs.getString("vehicle_name"));m.put("brand",rs.getString("brand"));m.put("type",rs.getString("vehicle_type"));m.put("year",rs.getObject("model_year"));m.put("rate",rs.getBigDecimal("daily_rate"));m.put("status",rs.getString("status"));rows.add(m);}r.setAttribute("rows",rows);}}
   r.getRequestDispatcher("/WEB-INF/views/vehicles.jsp").forward(r,s);
  }catch(SQLException|NumberFormatException e){throw new ServletException("Vehicle operation failed. Check database setup and input.",e);}
 }
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  try(Connection c=DBConnection.getConnection()){
   String id=WebUtil.p(r,"id");String sql=id.isBlank()?"INSERT INTO vehicles(registration_no,vehicle_name,brand,vehicle_type,model_year,daily_rate,status) VALUES(?,?,?,?,?,?,?)":"UPDATE vehicles SET registration_no=?,vehicle_name=?,brand=?,vehicle_type=?,model_year=?,daily_rate=?,status=? WHERE vehicle_id=?";
   try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,WebUtil.p(r,"registrationNo"));p.setString(2,WebUtil.p(r,"vehicleName"));p.setString(3,WebUtil.p(r,"brand"));p.setString(4,WebUtil.p(r,"vehicleType"));String y=WebUtil.p(r,"modelYear");if(y.isBlank())p.setNull(5,Types.SMALLINT);else p.setInt(5,Integer.parseInt(y));BigDecimal rate=new BigDecimal(WebUtil.p(r,"dailyRate"));if(rate.signum()<0)throw new IllegalArgumentException("Rate cannot be negative.");p.setBigDecimal(6,rate);p.setString(7,WebUtil.p(r,"status"));if(!id.isBlank())p.setInt(8,Integer.parseInt(id));p.executeUpdate();}
   r.getSession().setAttribute("flash","Vehicle saved successfully.");s.sendRedirect(r.getContextPath()+"/vehicles");
  }catch(SQLException|IllegalArgumentException e){r.getSession().setAttribute("flashError","Could not save vehicle. Check required values and make sure registration number is unique.");s.sendRedirect(r.getContextPath()+"/vehicles");}
 }
}
