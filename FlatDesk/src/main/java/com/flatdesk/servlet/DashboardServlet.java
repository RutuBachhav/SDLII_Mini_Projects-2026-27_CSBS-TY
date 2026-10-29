package com.flatdesk.servlet;
import com.flatdesk.config.DB; import com.google.gson.Gson; import jakarta.servlet.annotation.WebServlet; import jakarta.servlet.http.*;
import java.io.*; import java.sql.*; import java.util.*;
@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet{
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  resp.setContentType("application/json");
  try(Connection c=DB.getConnection()){
   Map<String,Object> m=new LinkedHashMap<>();
   for(String[] x:new String[][]{{"totalProperties","SELECT COUNT(*) FROM properties"},{"available","SELECT COUNT(*) FROM properties WHERE status='Available'"},{"rented","SELECT COUNT(*) FROM properties WHERE status='Rented'"},{"users","SELECT COUNT(*) FROM users"}}){
    try(Statement s=c.createStatement();ResultSet r=s.executeQuery(x[1])){r.next();m.put(x[0],r.getInt(1));}
   } new Gson().toJson(m,resp.getWriter());
  }catch(Exception e){resp.setStatus(500);new Gson().toJson(Map.of("error",e.getMessage()),resp.getWriter());}
 }
}
