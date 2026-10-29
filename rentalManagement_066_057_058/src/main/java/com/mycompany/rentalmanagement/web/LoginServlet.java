package com.mycompany.rentalmanagement.web;
import com.mycompany.rentalmanagement.util.DBConnection;
import com.mycompany.rentalmanagement.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r,s);}
 @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
  String username=WebUtil.p(r,"username"),password=WebUtil.p(r,"password");
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT display_name,role FROM users WHERE username=? AND password_text=? AND active=TRUE")){
   p.setString(1,username);p.setString(2,password);
   try(ResultSet rs=p.executeQuery()){if(rs.next()){HttpSession old=r.getSession(false);if(old!=null)old.invalidate();HttpSession session=r.getSession(true);session.setAttribute("user",rs.getString("display_name"));session.setAttribute("role",rs.getString("role"));s.sendRedirect(r.getContextPath()+"/dashboard");return;}}
   r.setAttribute("error","Invalid username or password.");r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r,s);
  }catch(SQLException e){throw new ServletException("Login database error. Check DBConnection.java and run database/schema.sql.",e);}
 }
}
