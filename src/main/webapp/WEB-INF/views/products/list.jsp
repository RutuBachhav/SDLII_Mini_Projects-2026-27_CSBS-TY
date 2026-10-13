<%@ page import="com.clothfactory.model.Product" %>
<%@ page import="com.clothfactory.util.HtmlEscaper" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% List<Product> products = (List<Product>) request.getAttribute("products"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Products | Cloth Factory Inventory</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4 gap-3 flex-wrap">
        <div><p class="eyebrow mb-1">Catalog</p><h1 class="h2 mb-0">Products</h1></div>
        <a class="btn btn-dark" href="<%= request.getContextPath() %>/products/new">Add product</a>
    </div>
    <% if (request.getParameter("message") != null) { %><div class="alert alert-info"><%= HtmlEscaper.escape(request.getParameter("message")) %></div><% } %>
    <form class="row g-2 mb-4" method="get">
        <div class="col-md-6"><label class="visually-hidden" for="search">Search</label><input class="form-control" id="search" name="search" value="<%= HtmlEscaper.escape(request.getAttribute("search")) %>" placeholder="Search name or code"></div>
        <div class="col-md-4"><label class="visually-hidden" for="category">Category</label><select class="form-select" id="category" name="category"><option value="">All categories</option><% for (Product.Category category : Product.Category.values()) { %><option value="<%= category.name() %>" <%= category.name().equals(request.getAttribute("selectedCategory")) ? "selected" : "" %>><%= category.name().replace('_', ' ') %></option><% } %></select></div>
        <div class="col-md-2"><button class="btn btn-outline-dark w-100" type="submit">Filter</button></div>
    </form>
    <div class="table-responsive bg-white shadow-sm">
        <table class="table table-hover align-middle mb-0"><thead><tr><th>Code</th><th>Name</th><th>Category</th><th>Stock</th><th>Minimum</th><th>Price</th><th class="text-end">Actions</th></tr></thead><tbody>
        <% for (Product product : products) { %><tr><td><%= HtmlEscaper.escape(product.getProductCode()) %></td><td><strong><%= HtmlEscaper.escape(product.getProductName()) %></strong><br><small class="text-muted">per <%= HtmlEscaper.escape(product.getUnit()) %></small></td><td><%= product.getCategory().name().replace('_', ' ') %></td><td><%= product.getCurrentQuantity() %></td><td><%= product.getMinimumStock() %></td><td><%= product.getUnitPrice() %></td><td class="text-end"><a class="btn btn-sm btn-outline-secondary" href="<%= request.getContextPath() %>/products/edit?id=<%= product.getProductId() %>">Edit</a><form class="d-inline" method="post" action="<%= request.getContextPath() %>/products/delete"><input type="hidden" name="id" value="<%= product.getProductId() %>"><button class="btn btn-sm btn-outline-danger" type="submit" onclick="return confirm('Delete this unused product?')">Delete</button></form></td></tr><% } %>
        <% if (products.isEmpty()) { %><tr><td colspan="7" class="text-center py-4 text-muted">No products found.</td></tr><% } %>
        </tbody></table>
    </div>
</main>
</body>
</html>