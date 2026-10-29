package com.mycompany.rentalmanagement.filter;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebFilter;import java.io.IOException;
@WebFilter("/*") public class EncodingFilter implements Filter {public void doFilter(ServletRequest r,ServletResponse s,FilterChain c)throws IOException,ServletException{r.setCharacterEncoding("UTF-8");s.setCharacterEncoding("UTF-8");c.doFilter(r,s);}}
