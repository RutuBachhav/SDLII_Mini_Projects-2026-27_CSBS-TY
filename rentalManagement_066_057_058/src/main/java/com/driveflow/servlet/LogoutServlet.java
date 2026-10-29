package com.driveflow.servlet;
import javax.servlet.http.*; import java.io.IOException;
public class LogoutServlet extends HttpServlet { protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{req.getSession().invalidate();resp.sendRedirect(req.getContextPath()+"/login");} }
