<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Room" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Rooms</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"ADMIN".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Room> rooms = (List<Room>) request.getAttribute("rooms");
%>
<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Hostel Admin</div>
        <a href="<%= request.getContextPath() %>/AdminDashboardServlet">Dashboard</a>
        <a class="active" href="<%= request.getContextPath() %>/RoomServlet">Rooms</a>
        <a href="<%= request.getContextPath() %>/StudentServlet">Students</a>
        <a href="<%= request.getContextPath() %>/FeeServlet">Fee Payments</a>
        <a href="<%= request.getContextPath() %>/ComplaintServlet">Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar">
            <div>Room Management</div>
            <div class="user-info">Welcome, <strong><%= session.getAttribute("username") %></strong></div>
        </div>

        <div class="page-content">
            <div class="page-heading">
                <h1>Rooms</h1>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/RoomServlet?action=new">+ Add Room</a>
            </div>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Room No.</th>
                        <th>Type</th>
                        <th>Capacity</th>
                        <th>Occupancy</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% if (rooms != null && !rooms.isEmpty()) {
                    for (Room r : rooms) { %>
                    <tr>
                        <td><strong><%= r.getRoomNo() %></strong></td>
                        <td><%= r.getRoomType() %></td>
                        <td><%= r.getCapacity() %></td>
                        <td><%= r.getCurrentOccupancy() %> / <%= r.getCapacity() %></td>
                        <td>
                            <span class="badge <%= "FULL".equals(r.getStatus()) ? "badge-red" : "badge-green" %>">
                                <%= r.getStatus() %>
                            </span>
                        </td>
                        <td>
                            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/RoomServlet?action=edit&id=<%= r.getId() %>">Edit</a>
                            <a class="btn btn-danger btn-sm" href="<%= request.getContextPath() %>/RoomServlet?action=delete&id=<%= r.getId() %>"
                               onclick="return confirm('Delete this room? Only empty rooms can be deleted.');">Delete</a>
                        </td>
                    </tr>
                <% }} else { %>
                    <tr><td colspan="6" class="empty">No rooms found.</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
