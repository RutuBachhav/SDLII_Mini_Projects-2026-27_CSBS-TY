<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Student" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Students</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"ADMIN".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Student> students = (List<Student>) request.getAttribute("students");
%>
<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Hostel Admin</div>
        <a href="<%= request.getContextPath() %>/AdminDashboardServlet">Dashboard</a>
        <a href="<%= request.getContextPath() %>/RoomServlet">Rooms</a>
        <a class="active" href="<%= request.getContextPath() %>/StudentServlet">Students</a>
        <a href="<%= request.getContextPath() %>/FeeServlet">Fee Payments</a>
        <a href="<%= request.getContextPath() %>/ComplaintServlet">Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar"><div>Student Management</div></div>
        <div class="page-content">
            <div class="page-heading">
                <h1>Students</h1>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/StudentServlet?action=new">+ Admit Student</a>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Username</th>
                        <th>Contact</th>
                        <th>Guardian</th>
                        <th>Room</th>
                        <th>Admission Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% if (students != null && !students.isEmpty()) {
                    for (Student s : students) { %>
                    <tr>
                        <td><strong><%= s.getName() %></strong></td>
                        <td><%= s.getUser() == null ? "-" : s.getUser().getUsername() %></td>
                        <td><%= s.getContact() == null ? "-" : s.getContact() %></td>
                        <td><%= s.getGuardianContact() == null ? "-" : s.getGuardianContact() %></td>
                        <td><%= s.getRoom() == null ? "-" : s.getRoom().getRoomNo() %></td>
                        <td><%= s.getAdmissionDate() == null ? "-" : s.getAdmissionDate() %></td>
                        <td>
                            <a class="btn btn-danger btn-sm"
                               href="<%= request.getContextPath() %>/StudentServlet?action=delete&id=<%= s.getId() %>"
                               onclick="return confirm('Remove this student? Their fee and complaint records will also be deleted.');">Remove</a>
                        </td>
                    </tr>
                <% }} else { %>
                    <tr><td colspan="7" class="empty">No students found.</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
