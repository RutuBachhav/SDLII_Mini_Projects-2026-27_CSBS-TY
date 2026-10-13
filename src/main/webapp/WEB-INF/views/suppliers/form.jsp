<%@ page import="com.clothfactory.model.Supplier" %>
<%@ page import="com.clothfactory.util.HtmlEscaper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% Supplier supplier = (Supplier) request.getAttribute("supplier"); boolean editing = supplier.getSupplierId() != 0; %>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title><%= editing ? "Edit" : "Add" %> Supplier | Cloth Factory Inventory</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="<%= request.getContextPath() %>/assets/css/app.css" rel="stylesheet"></head>
<body><main class="container py-4"><div class="mb-4"><p class="eyebrow mb-1">Procurement</p><h1 class="h2"><%= editing ? "Edit supplier" : "Add supplier" %></h1></div>
<% if (request.getAttribute("error") != null) { %><div class="alert alert-danger"><%= HtmlEscaper.escape(request.getAttribute("error")) %></div><% } %>
<form method="post" action="<%= request.getContextPath() %>/suppliers/save" class="row g-3 bg-white p-4 shadow-sm"><input type="hidden" name="supplierId" value="<%= supplier.getSupplierId() %>">
<div class="col-md-6"><label class="form-label" for="supplierName">Supplier name</label><input class="form-control" id="supplierName" name="supplierName" maxlength="120" required value="<%= HtmlEscaper.escape(supplier.getSupplierName()) %>"></div>
<div class="col-md-6"><label class="form-label" for="contactNumber">Contact number</label><input class="form-control" id="contactNumber" name="contactNumber" maxlength="30" required value="<%= HtmlEscaper.escape(supplier.getContactNumber()) %>"></div>
<div class="col-md-6"><label class="form-label" for="email">Email</label><input class="form-control" id="email" name="email" type="email" maxlength="150" value="<%= HtmlEscaper.escape(supplier.getEmail()) %>"></div>
<div class="col-md-6"><label class="form-label" for="address">Address</label><input class="form-control" id="address" name="address" maxlength="255" value="<%= HtmlEscaper.escape(supplier.getAddress()) %>"></div>
<div class="col-12 d-flex gap-2"><button class="btn btn-dark" type="submit"><%= editing ? "Save changes" : "Create supplier" %></button><a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/suppliers">Cancel</a></div></form>
</main></body></html>