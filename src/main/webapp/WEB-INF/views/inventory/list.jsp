<%@ page import="com.clothfactory.model.StockTransaction" %>
<%@ page import="com.clothfactory.util.HtmlEscaper" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% List<StockTransaction> transactions = (List<StockTransaction>) request.getAttribute("transactions"); %>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Inventory | Cloth Factory Inventory</title><link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"><link href="<%= request.getContextPath() %>/assets/css/app.css" rel="stylesheet"></head>
<body><main class="container py-4">
<div class="d-flex justify-content-between align-items-center mb-4 gap-3 flex-wrap"><div><p class="eyebrow mb-1">Stock control</p><h1 class="h2 mb-0">Inventory movements</h1></div><div class="d-flex gap-2"><a class="btn btn-outline-dark" href="<%= request.getContextPath() %>/inventory/new?type=STOCK_OUT">Record stock-out</a><a class="btn btn-dark" href="<%= request.getContextPath() %>/inventory/new?type=STOCK_IN">Record stock-in</a></div></div>
<div class="table-responsive bg-white shadow-sm"><table class="table table-hover align-middle mb-0"><thead><tr><th>Date</th><th>Product</th><th>Type</th><th>Quantity</th><th>Reason</th></tr></thead><tbody>
<% for (StockTransaction transaction : transactions) { %><tr><td><%= transaction.getTransactionDate() == null ? "" : transaction.getTransactionDate().toString().replace('T', ' ') %></td><td><strong><%= HtmlEscaper.escape(transaction.getProductName()) %></strong><br><small class="text-muted">per <%= HtmlEscaper.escape(transaction.getUnit()) %></small></td><td><span class="badge <%= transaction.getType() == StockTransaction.Type.STOCK_IN ? "text-bg-success" : "text-bg-warning" %>"><%= transaction.getType().name().replace('_', ' ') %></span></td><td><%= transaction.getQuantity() %></td><td><%= HtmlEscaper.escape(transaction.getReason()) %></td></tr><% } %>
<% if (transactions.isEmpty()) { %><tr><td colspan="5" class="text-center py-4 text-muted">No stock movements recorded.</td></tr><% } %>
</tbody></table></div></main></body></html>