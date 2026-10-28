package com.mycompany.rentalmanagement.filter;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebFilter;import jakarta.servlet.http.*;import java.io.IOException;
@WebFilter(urlPatterns={"/dashboard","/vehicles","/customers","/rentals","/returns","/payments","/reports","/logout"})
public class AuthFilter implements Filter {public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{HttpServletRequest r=(HttpServletRequest)req;HttpSession s=r.getSession(false);if(s==null||s.getAttribute("user")==null){((HttpServletResponse)res).sendRedirect(r.getContextPath()+"/login");return;}chain.doFilter(req,res);}}
