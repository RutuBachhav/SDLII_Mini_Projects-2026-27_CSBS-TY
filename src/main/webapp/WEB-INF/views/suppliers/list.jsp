<%@ page import="com.clothfactory.model.Supplier" %>
<%@ page import="com.clothfactory.util.HtmlEscaper" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% List<Supplier> suppliers = (List<Supplier>) request.getAttribute("suppliers"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Suppliers | Cloth Factory Inventory</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="<%= request.getContextPath() %>/assets/css/app.css" rel="stylesheet">
</head>
<body><main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4 gap-3 flex-wrap"><div><p class="eyebrow mb-1">Procurement</p><h1 class="h2 mb-0">Suppliers</h1></div><a class="btn btn-dark" href="<%= request.getContextPath() %>/suppliers/new">Add supplier</a></div>
    <% if (request.getParameter("message") != null) { %><div class="alert alert-info"><%= HtmlEscaper.escape(request.getParameter("message")) %></div><% } %>
    <form class="row g-2 mb-4" method="get"><div class="col-md-10"><label class="visually-hidden" for="search">Search</label><input class="form-control" id="search" name="search" value="<%= HtmlEscaper.escape(request.getAttribute("search")) %>" placeholder="Search supplier, phone, or email"></div><div class="col-md-2"><button class="btn btn-outline-dark w-100" type="submit">Search</button></div></form>
    <div class="table-responsive bg-white shadow-sm"><table class="table table-hover align-middle mb-0"><thead><tr><th>Supplier</th><th>Contact</th><th>Email</th><th>Address</th><th class="text-end">Actions</th></tr></thead><tbody>
    <% for (Supplier supplier : suppliers) { %><tr><td><strong><%= HtmlEscaper.escape(supplier.getSupplierName()) %></strong></td><td><%= HtmlEscaper.escape(supplier.getContactNumber()) %></td><td><%= HtmlEscaper.escape(supplier.getEmail()) %></td><td><%= HtmlEscaper.escape(supplier.getAddress()) %></td><td class="text-end"><a class="btn btn-sm btn-outline-secondary" href="<%= request.getContextPath() %>/suppliers/edit?id=<%= supplier.getSupplierId() %>">Edit</a><form class="d-inline" method="post" action="<%= request.getContextPath() %>/suppliers/delete"><input type="hidden" name="id" value="<%= supplier.getSupplierId() %>"><button class="btn btn-sm btn-outline-danger" type="submit" onclick="return confirm('Delete this supplier if it has no purchases?')">Delete</button></form></td></tr><% } %>
    <% if (suppliers.isEmpty()) { %><tr><td colspan="5" class="text-center py-4 text-muted">No suppliers found.</td></tr><% } %>
    </tbody></table></div>
</main></body></html>