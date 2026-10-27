<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Complaint" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Complaints</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"ADMIN".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Complaint> complaints = (List<Complaint>) request.getAttribute("complaints");
%>
<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Hostel Admin</div>
        <a href="<%= request.getContextPath() %>/AdminDashboardServlet">Dashboard</a>
        <a href="<%= request.getContextPath() %>/RoomServlet">Rooms</a>
        <a href="<%= request.getContextPath() %>/StudentServlet">Students</a>
        <a href="<%= request.getContextPath() %>/FeeServlet">Fee Payments</a>
        <a class="active" href="<%= request.getContextPath() %>/ComplaintServlet">Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar"><div>Complaint Management</div></div>
        <div class="page-content">
            <div class="page-heading">
                <h1>Complaints</h1>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/ComplaintServlet?action=new">+ Raise Complaint</a>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Student</th>
                        <th>Description</th>
                        <th>Status</th>
                        <th>Date Raised</th>
                        <th>Date Resolved</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% if (complaints != null && !complaints.isEmpty()) {
                    for (Complaint c : complaints) { %>
                    <tr>
                        <td><%= c.getStudent().getName() %></td>
                        <td><%= c.getDescription() %></td>
                        <td>
                            <span class="badge <%= "RESOLVED".equals(c.getStatus()) ? "badge-green" : "badge-red" %>">
                                <%= c.getStatus() %>
                            </span>
                        </td>
                        <td><%= c.getDateRaised() == null ? "-" : c.getDateRaised() %></td>
                        <td><%= c.getDateResolved() == null ? "-" : c.getDateResolved() %></td>
                        <td>
                            <% if (!"RESOLVED".equals(c.getStatus())) { %>
                                <a class="btn btn-primary btn-sm"
                                   href="<%= request.getContextPath() %>/ComplaintServlet?action=resolve&id=<%= c.getId() %>">Resolve</a>
                            <% } %>
                            <a class="btn btn-danger btn-sm"
                               href="<%= request.getContextPath() %>/ComplaintServlet?action=delete&id=<%= c.getId() %>"
                               onclick="return confirm('Delete this complaint?');">Delete</a>
                        </td>
                    </tr>
                <% }} else { %>
                    <tr><td colspan="6" class="empty">No complaints found.</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
