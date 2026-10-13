<%@ page import="com.clothfactory.model.Product" %>
<%@ page import="com.clothfactory.model.StockTransaction" %>
<%@ page import="com.clothfactory.util.HtmlEscaper" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% StockTransaction.Type type = (StockTransaction.Type) request.getAttribute("type"); List<Product> products = (List<Product>) request.getAttribute("products"); %>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Record <%= type.name().replace('_', ' ') %> | Cloth Factory Inventory</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="<%= request.getContextPath() %>/assets/css/app.css" rel="stylesheet"></head>
<body><main class="container py-4"><div class="mb-4"><p class="eyebrow mb-1">Stock control</p><h1 class="h2">Record <%= type.name().replace('_', ' ').toLowerCase() %></h1></div>
<% if (request.getAttribute("error") != null) { %><div class="alert alert-danger"><%= HtmlEscaper.escape(request.getAttribute("error")) %></div><% } %>
<form method="post" action="<%= request.getContextPath() %>/inventory/move" class="row g-3 bg-white p-4 shadow-sm"><input type="hidden" name="type" value="<%= type.name() %>">
<div class="col-md-6"><label class="form-label" for="productId">Product</label><select class="form-select" id="productId" name="productId" required><option value="">Choose a product...</option><% for (Product product : products) { %><option value="<%= product.getProductId() %>"><%= HtmlEscaper.escape(product.getProductName()) %> (<%= product.getCurrentQuantity() %> <%= HtmlEscaper.escape(product.getUnit()) %> available)</option><% } %></select></div>
<div class="col-md-6"><label class="form-label" for="quantity">Quantity</label><input class="form-control" id="quantity" name="quantity" type="number" min="0.001" step="0.001" required></div>
<div class="col-12"><label class="form-label" for="reason">Reason</label><input class="form-control" id="reason" name="reason" maxlength="255" required placeholder="e.g. Production batch or supplier receipt"></div>
<div class="col-12 d-flex gap-2"><button class="btn btn-dark" type="submit">Record movement</button><a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/inventory">Cancel</a></div></form></main></body></html>