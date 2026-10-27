<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Room" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Room Form</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"ADMIN".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    Room room = (Room) request.getAttribute("room");
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
        <div class="topbar"><div>Room Management</div></div>
        <div class="page-content">
            <h1><%= room == null ? "Add Room" : "Edit Room" %></h1>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <div class="form-box">
                <form action="<%= request.getContextPath() %>/RoomServlet" method="post">
                    <% if (room != null) { %>
                        <input type="hidden" name="id" value="<%= room.getId() %>">
                    <% } %>

                    <div class="form-group">
                        <label>Room Number</label>
                        <input type="text" name="roomNo" value="<%= room == null ? "" : room.getRoomNo() %>" required>
                    </div>

                    <div class="form-group">
                        <label>Room Type</label>
                        <select name="roomType" required>
                            <option value="SINGLE" <%= room != null && "SINGLE".equals(room.getRoomType()) ? "selected" : "" %>>Single</option>
                            <option value="DOUBLE" <%= room != null && "DOUBLE".equals(room.getRoomType()) ? "selected" : "" %>>Double</option>
                            <option value="DORM" <%= room != null && "DORM".equals(room.getRoomType()) ? "selected" : "" %>>Dorm</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Capacity</label>
                        <input type="number" name="capacity" min="1"
                               value="<%= room == null ? "" : room.getCapacity() %>" required>
                    </div>

                    <button class="btn btn-primary" type="submit">Save Room</button>
                    <a class="btn btn-light" href="<%= request.getContextPath() %>/RoomServlet">Cancel</a>
                </form>
            </div>
        </div>
    </main>
</div>
</body>
</html>
