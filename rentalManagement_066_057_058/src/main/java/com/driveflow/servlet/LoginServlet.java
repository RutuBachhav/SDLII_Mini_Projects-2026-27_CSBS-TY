package com.driveflow.servlet;
import javax.servlet.*; import javax.servlet.http.*; import java.io.IOException;
public class LoginServlet extends HttpServlet {
 protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{req.getRequestDispatcher("/views/login.jsp").forward(req,resp);}
 protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  String u=req.getParameter("username"),p=req.getParameter("password");
  if("admin".equals(u)&&"admin123".equals(p)){req.getSession().setAttribute("user","Administrator");resp.sendRedirect(req.getContextPath()+"/dashboard");}
  else {req.setAttribute("error","Invalid username or password. Use admin / admin123.");try{req.getRequestDispatcher("/views/login.jsp").forward(req,resp);}catch(ServletException e){throw new IOException(e);}}
 }
}
