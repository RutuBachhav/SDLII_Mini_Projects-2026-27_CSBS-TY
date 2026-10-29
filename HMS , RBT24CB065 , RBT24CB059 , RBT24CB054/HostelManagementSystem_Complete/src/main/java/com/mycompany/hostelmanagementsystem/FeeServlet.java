package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(name = "FeeServlet", urlPatterns = {"/FeeServlet"})
public class FeeServlet extends HttpServlet {

    private final FeeDAO feeDAO = new FeeDAO();

    private boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(request.getSession().getAttribute("role"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "new":
                    request.setAttribute("students", feeDAO.getAllStudents());
                    request.getRequestDispatcher("/admin/fee-form.jsp").forward(request, response);
                    break;

                case "markPaid":
                    feeDAO.markAsPaid(Integer.parseInt(request.getParameter("id")));
                    response.sendRedirect(request.getContextPath() + "/FeeServlet");
                    break;

                case "delete":
                    feeDAO.deleteFeeRecord(Integer.parseInt(request.getParameter("id")));
                    response.sendRedirect(request.getContextPath() + "/FeeServlet");
                    break;

                default:
                    request.setAttribute("fees", feeDAO.getAllFees());
                    request.getRequestDispatcher("/admin/fees.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("fees", feeDAO.getAllFees());
            request.getRequestDispatcher("/admin/fees.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            Integer studentId = Integer.parseInt(request.getParameter("studentId"));
            String month = request.getParameter("month");
            BigDecimal amount = new BigDecimal(request.getParameter("amount"));

            feeDAO.addFeeRecord(studentId, month, amount);
            response.sendRedirect(request.getContextPath() + "/FeeServlet");

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("students", feeDAO.getAllStudents());
            request.getRequestDispatcher("/admin/fee-form.jsp").forward(request, response);
        }
    }
}
