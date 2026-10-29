package com.mycompany.rentalmanagement.web;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebFilter(urlPatterns={"/*"})
public class AuthFilter implements Filter {
 @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{
  HttpServletRequest r=(HttpServletRequest)req; HttpServletResponse s=(HttpServletResponse)res;
  String path=r.getRequestURI().substring(r.getContextPath().length());
  if(path.equals("/login")||path.equals("/login.jsp")||path.startsWith("/assets/")||path.equals("/favicon.ico")){chain.doFilter(req,res);return;}
  if(r.getSession(false)==null||r.getSession(false).getAttribute("user")==null){s.sendRedirect(r.getContextPath()+"/login");return;}
  chain.doFilter(req,res);
 }
}
