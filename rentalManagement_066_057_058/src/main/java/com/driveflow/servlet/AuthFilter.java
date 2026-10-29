package com.driveflow.servlet;
import javax.servlet.*; import javax.servlet.http.*; import java.io.IOException;
public class AuthFilter implements Filter {
 public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
  HttpServletRequest req=(HttpServletRequest)request; HttpServletResponse resp=(HttpServletResponse)response;
  String uri=req.getRequestURI(); String ctx=req.getContextPath();
  if(uri.startsWith(ctx+"/css/")||uri.startsWith(ctx+"/js/")||uri.startsWith(ctx+"/login")){chain.doFilter(request,response);return;}
  if(req.getSession(false)==null||req.getSession(false).getAttribute("user")==null){resp.sendRedirect(ctx+"/login");return;} chain.doFilter(request,response);
 }
}
