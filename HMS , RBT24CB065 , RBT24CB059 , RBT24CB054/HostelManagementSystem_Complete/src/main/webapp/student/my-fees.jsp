<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.FeePayment" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Fees</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"STUDENT".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<FeePayment> fees = (List<FeePayment>) request.getAttribute("fees");
%>
<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Student Panel</div>
        <a href="<%= request.getContextPath() %>/StudentDashboardServlet">Dashboard</a>
        <a class="active" href="<%= request.getContextPath() %>/MyFeesServlet">My Fees</a>
        <a href="<%= request.getContextPath() %>/MyComplaintsServlet">My Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar"><div>My Fees</div></div>
        <div class="page-content">
            <h1>My Fee Records</h1>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Month</th>
                        <th>Amount</th>
                        <th>Status</th>
                        <th>Payment Date</th>
                    </tr>
                </thead>
                <tbody>
                <% if (fees != null && !fees.isEmpty()) {
                    for (FeePayment f : fees) { %>
                    <tr>
                        <td><%= f.getMonth() %></td>
                        <td>₹ <%= f.getAmount() %></td>
                        <td>
                            <span class="badge <%= "PAID".equals(f.getStatus()) ? "badge-green" : "badge-yellow" %>">
                                <%= f.getStatus() %>
                            </span>
                        </td>
                        <td><%= f.getPaymentDate() == null ? "-" : f.getPaymentDate() %></td>
                    </tr>
                <% }} else { %>
                    <tr><td colspan="4" class="empty">No fee records found.</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
