
package com.courierbay.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (ADMIN_USERNAME.equals(username)
                && ADMIN_PASSWORD.equals(password)) {

            HttpSession session = request.getSession();
            session.setAttribute("admin", username);

            response.sendRedirect("AdminDashboardServlet");

        } else {

            response.setContentType("text/html;charset=UTF-8");

            try (PrintWriter out = response.getWriter()) {

                out.println("<html><head><title>Login Failed</title></head>");
                out.println("<body style='font-family:Arial;text-align:center;padding:60px;'>");
                out.println("<h1 style='color:red;'>Invalid Login!</h1>");
                out.println("<p>Incorrect username or password.</p>");
                out.println("<a href='admin-login.html'>Try Again</a>");
                out.println("</body></html>");
            }
        }
    }
}