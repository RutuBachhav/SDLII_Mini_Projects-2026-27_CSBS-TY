package com.mycompany.rentalmanagement.web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
 @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException{HttpSession session=r.getSession(false);if(session!=null)session.invalidate();s.sendRedirect(r.getContextPath()+"/login");}
}
