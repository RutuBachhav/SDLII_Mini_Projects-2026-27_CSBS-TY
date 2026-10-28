package com.mycompany.rentalmanagement.web;
import com.mycompany.rentalmanagement.util.DBConnection;
import com.mycompany.rentalmanagement.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;import java.sql.*;
@WebServlet("/customers")
public class CustomerServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  String a=WebUtil.p(r,"action");
  try(Connection c=DBConnection.getConnection()){
   if("delete".equals(a)){try(PreparedStatement p=c.prepareStatement("DELETE FROM customers WHERE customer_id=?")){p.setInt(1,WebUtil.intParam(r,"id"));if(p.executeUpdate()==0)r.getSession().setAttribute("flashError","Customer not found.");else r.getSession().setAttribute("flash","Customer deleted.");}catch(SQLException e){r.getSession().setAttribute("flashError","Customer cannot be deleted because rental history refers to this customer.");}s.sendRedirect(r.getContextPath()+"/customers");return;}
   if("edit".equals(a)){try(PreparedStatement p=c.prepareStatement("SELECT * FROM customers WHERE customer_id=?")){p.setInt(1,WebUtil.intParam(r,"id"));try(ResultSet x=p.executeQuery()){if(x.next()){r.setAttribute("edit",x.getInt("customer_id"));r.setAttribute("name",x.getString("full_name"));r.setAttribute("phone",x.getString("phone"));r.setAttribute("email",x.getString("email"));r.setAttribute("address",x.getString("address"));r.setAttribute("license",x.getString("license_no"));}}}}
   String q="%"+WebUtil.p(r,"q")+"%";r.setAttribute("q",WebUtil.p(r,"q"));
   try(PreparedStatement p=c.prepareStatement("SELECT * FROM customers WHERE full_name LIKE ? OR phone LIKE ? OR license_no LIKE ? ORDER BY customer_id DESC")){p.setString(1,q);p.setString(2,q);p.setString(3,q);try(ResultSet x=p.executeQuery()){java.util.List<java.util.Map<String,Object>> rows=new java.util.ArrayList<>();while(x.next()){var m=new java.util.LinkedHashMap<String,Object>();m.put("id",x.getInt("customer_id"));m.put("name",x.getString("full_name"));m.put("phone",x.getString("phone"));m.put("email",x.getString("email"));m.put("address",x.getString("address"));m.put("license",x.getString("license_no"));rows.add(m);}r.setAttribute("rows",rows);}}
   r.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(r,s);
  }catch(SQLException|NumberFormatException e){throw new ServletException("Could not load customers.",e);}
 }
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  String id=WebUtil.p(r,"id");
  String sql=id.isBlank()?"INSERT INTO customers(full_name,phone,email,address,license_no) VALUES(?,?,?,?,?)":"UPDATE customers SET full_name=?,phone=?,email=?,address=?,license_no=? WHERE customer_id=?";
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,WebUtil.p(r,"name"));p.setString(2,WebUtil.p(r,"phone"));p.setString(3,WebUtil.p(r,"email"));p.setString(4,WebUtil.p(r,"address"));p.setString(5,WebUtil.p(r,"license"));if(!id.isBlank())p.setInt(6,Integer.parseInt(id));p.executeUpdate();r.getSession().setAttribute("flash","Customer saved successfully.");}
  catch(SQLException|NumberFormatException e){r.getSession().setAttribute("flashError","Could not save customer. Check required fields and unique license number.");}
  s.sendRedirect(r.getContextPath()+"/customers");
 }
}
