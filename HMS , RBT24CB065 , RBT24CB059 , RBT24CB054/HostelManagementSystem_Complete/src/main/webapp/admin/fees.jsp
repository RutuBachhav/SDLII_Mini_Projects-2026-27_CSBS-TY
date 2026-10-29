<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.FeePayment" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Fee Payments</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"ADMIN".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<FeePayment> fees = (List<FeePayment>) request.getAttribute("fees");
%>
<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Hostel Admin</div>
        <a href="<%= request.getContextPath() %>/AdminDashboardServlet">Dashboard</a>
        <a href="<%= request.getContextPath() %>/RoomServlet">Rooms</a>
        <a href="<%= request.getContextPath() %>/StudentServlet">Students</a>
        <a class="active" href="<%= request.getContextPath() %>/FeeServlet">Fee Payments</a>
        <a href="<%= request.getContextPath() %>/ComplaintServlet">Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar"><div>Fee Management</div></div>
        <div class="page-content">
            <div class="page-heading">
                <h1>Fee Payments</h1>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/FeeServlet?action=new">+ Add Fee Record</a>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Student</th>
                        <th>Room</th>
                        <th>Month</th>
                        <th>Amount</th>
                        <th>Status</th>
                        <th>Payment Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% if (fees != null && !fees.isEmpty()) {
                    for (FeePayment f : fees) { %>
                    <tr>
                        <td><%= f.getStudent().getName() %></td>
                        <td><%= f.getStudent().getRoom() == null ? "-" : f.getStudent().getRoom().getRoomNo() %></td>
                        <td><%= f.getMonth() %></td>
                        <td>₹ <%= f.getAmount() %></td>
                        <td>
                            <span class="badge <%= "PAID".equals(f.getStatus()) ? "badge-green" : "badge-yellow" %>">
                                <%= f.getStatus() %>
                            </span>
                        </td>
                        <td><%= f.getPaymentDate() == null ? "-" : f.getPaymentDate() %></td>
                        <td>
                            <% if (!"PAID".equals(f.getStatus())) { %>
                                <a class="btn btn-primary btn-sm"
                                   href="<%= request.getContextPath() %>/FeeServlet?action=markPaid&id=<%= f.getId() %>">Mark Paid</a>
                            <% } %>
                            <a class="btn btn-danger btn-sm"
                               href="<%= request.getContextPath() %>/FeeServlet?action=delete&id=<%= f.getId() %>"
                               onclick="return confirm('Delete this fee record?');">Delete</a>
                        </td>
                    </tr>
                <% }} else { %>
                    <tr><td colspan="7" class="empty">No fee records found.</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
