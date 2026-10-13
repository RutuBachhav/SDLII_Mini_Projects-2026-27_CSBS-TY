package com.clothfactory.servlet;

import com.clothfactory.dao.SupplierDAO;
import com.clothfactory.model.Supplier;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/suppliers/*")
public class SupplierServlet extends HttpServlet {
    private final SupplierDAO supplierDAO = new SupplierDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        try {
            if (path == null || "/".equals(path)) {
                request.setAttribute("suppliers", supplierDAO.findAll(request.getParameter("search")));
                request.setAttribute("search", request.getParameter("search") == null ? "" : request.getParameter("search"));
                request.getRequestDispatcher("/WEB-INF/views/suppliers/list.jsp").forward(request, response);
            } else if ("/new".equals(path)) {
                showForm(request, response, new Supplier());
            } else if ("/edit".equals(path)) {
                Supplier supplier = supplierDAO.findById(parseId(request.getParameter("id")));
                if (supplier == null) response.sendError(HttpServletResponse.SC_NOT_FOUND);
                else showForm(request, response, supplier);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid supplier id");
        } catch (SQLException exception) {
            throw new ServletException("Unable to read suppliers", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if ("/save".equals(request.getPathInfo())) save(request, response);
            else if ("/delete".equals(request.getPathInfo())) delete(request, response);
            else response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid supplier data");
        } catch (SQLException exception) {
            throw new ServletException("Unable to save supplier", exception);
        }
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Supplier supplier = readSupplier(request);
        String error = validate(supplier);
        if (error != null) {
            request.setAttribute("supplier", supplier);
            request.setAttribute("error", error);
            request.getRequestDispatcher("/WEB-INF/views/suppliers/form.jsp").forward(request, response);
            return;
        }
        if (supplier.getSupplierId() == 0) supplierDAO.create(supplier);
        else if (!supplierDAO.update(supplier)) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        response.sendRedirect(request.getContextPath() + "/suppliers");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        boolean deleted = supplierDAO.deleteIfUnused(parseId(request.getParameter("id")));
        String message = deleted ? "Supplier deleted" : "Supplier cannot be deleted because it has purchase records";
        response.sendRedirect(request.getContextPath() + "/suppliers?message=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    private static Supplier readSupplier(HttpServletRequest request) {
        Supplier supplier = new Supplier();
        String id = request.getParameter("supplierId");
        supplier.setSupplierId(id == null || id.isBlank() ? 0 : Long.parseLong(id));
        supplier.setSupplierName(trim(request.getParameter("supplierName")));
        supplier.setContactNumber(trim(request.getParameter("contactNumber")));
        supplier.setEmail(trim(request.getParameter("email")));
        supplier.setAddress(trim(request.getParameter("address")));
        return supplier;
    }

    private static String validate(Supplier supplier) {
        if (supplier.getSupplierName() == null || supplier.getSupplierName().isBlank() || supplier.getSupplierName().length() > 120) return "Supplier name is required and must be at most 120 characters.";
        if (supplier.getContactNumber() == null || supplier.getContactNumber().isBlank() || supplier.getContactNumber().length() > 30) return "Contact number is required and must be at most 30 characters.";
        if (supplier.getEmail() != null && supplier.getEmail().length() > 150) return "Email must be at most 150 characters.";
        if (supplier.getAddress() != null && supplier.getAddress().length() > 255) return "Address must be at most 255 characters.";
        return null;
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Supplier supplier) throws ServletException, IOException {
        request.setAttribute("supplier", supplier);
        request.getRequestDispatcher("/WEB-INF/views/suppliers/form.jsp").forward(request, response);
    }

    private static long parseId(String value) { if (value == null || value.isBlank()) throw new NumberFormatException(); return Long.parseLong(value); }
    private static String trim(String value) { return value == null ? null : value.trim(); }
}