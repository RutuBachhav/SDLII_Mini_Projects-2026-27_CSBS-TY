<%@ page import="com.clothfactory.model.Product" %>
<%@ page import="com.clothfactory.util.HtmlEscaper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% Product product = (Product) request.getAttribute("product"); boolean editing = product.getProductId() != 0; %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= editing ? "Edit" : "Add" %> Product | Cloth Factory Inventory</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="<%= request.getContextPath() %>/assets/css/app.css" rel="stylesheet">
</head>
<body><main class="container py-4"><div class="mb-4"><p class="eyebrow mb-1">Catalog</p><h1 class="h2"><%= editing ? "Edit product" : "Add product" %></h1></div>
<% if (request.getAttribute("error") != null) { %><div class="alert alert-danger"><%= HtmlEscaper.escape(request.getAttribute("error")) %></div><% } %>
<form method="post" action="<%= request.getContextPath() %>/products/save" class="row g-3 bg-white p-4 shadow-sm"><input type="hidden" name="productId" value="<%= product.getProductId() %>">
<div class="col-md-6"><label class="form-label" for="productCode">Product code</label><input class="form-control" id="productCode" name="productCode" maxlength="30" required value="<%= HtmlEscaper.escape(product.getProductCode()) %>"></div>
<div class="col-md-6"><label class="form-label" for="productName">Product name</label><input class="form-control" id="productName" name="productName" maxlength="120" required value="<%= HtmlEscaper.escape(product.getProductName()) %>"></div>
<div class="col-md-4"><label class="form-label" for="category">Category</label><select class="form-select" id="category" name="category" required><option value="">Choose...</option><% for (Product.Category category : Product.Category.values()) { %><option value="<%= category.name() %>" <%= category == product.getCategory() ? "selected" : "" %>><%= category.name().replace('_', ' ') %></option><% } %></select></div>
<div class="col-md-4"><label class="form-label" for="unit">Unit</label><input class="form-control" id="unit" name="unit" maxlength="20" required value="<%= HtmlEscaper.escape(product.getUnit()) %>"></div>
<div class="col-md-4"><label class="form-label" for="unitPrice">Unit price</label><input class="form-control" id="unitPrice" name="unitPrice" type="number" min="0" step="0.01" required value="<%= product.getUnitPrice() == null ? "" : product.getUnitPrice() %>"></div>
<div class="col-md-6"><label class="form-label" for="currentQuantity">Current quantity</label><input class="form-control" id="currentQuantity" name="currentQuantity" type="number" min="0" step="0.001" required value="<%= product.getCurrentQuantity() == null ? "" : product.getCurrentQuantity() %>"></div>
<div class="col-md-6"><label class="form-label" for="minimumStock">Minimum stock</label><input class="form-control" id="minimumStock" name="minimumStock" type="number" min="0" step="0.001" required value="<%= product.getMinimumStock() == null ? "" : product.getMinimumStock() %>"></div>
<div class="col-12 d-flex gap-2"><button class="btn btn-dark" type="submit"><%= editing ? "Save changes" : "Create product" %></button><a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/products">Cancel</a></div></form></main></body></html>