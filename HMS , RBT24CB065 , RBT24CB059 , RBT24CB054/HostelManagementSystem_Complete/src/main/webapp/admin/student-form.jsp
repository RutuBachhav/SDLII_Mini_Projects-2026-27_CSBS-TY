<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Room" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admit Student</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"ADMIN".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Room> availableRooms = (List<Room>) request.getAttribute("availableRooms");
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
            <h1>Admit New Student</h1>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <div class="form-box form-wide">
                <form action="<%= request.getContextPath() %>/StudentServlet" method="post">
                    <div class="form-section-title">Student Details</div>

                    <div class="form-group">
                        <label>Student Name</label>
                        <input type="text" name="name" required>
                    </div>

                    <div class="form-group">
                        <label>Contact Number</label>
                        <input type="text" name="contact" pattern="[0-9+\- ]{10,15}" required>
                    </div>

                    <div class="form-group">
                        <label>Guardian Contact</label>
                        <input type="text" name="guardianContact" pattern="[0-9+\- ]{10,15}" required>
                    </div>

                    <div class="form-group">
                        <label>Assign Room</label>
                        <select name="roomId" required>
                            <option value="">-- Select Available Room --</option>
                            <% if (availableRooms != null) {
                                for (Room r : availableRooms) { %>
                                <option value="<%= r.getId() %>">
                                    <%= r.getRoomNo() %> - <%= r.getRoomType() %>
                                    (<%= r.getCapacity() - r.getCurrentOccupancy() %> slot(s) left)
                                </option>
                            <% }} %>
                        </select>
                    </div>

                    <div class="form-section-title">Student Login Account</div>

                    <div class="form-group">
                        <label>Username</label>
                        <input type="text" name="username" minlength="3" maxlength="50" required>
                    </div>

                    <div class="form-group">
                        <label>Password</label>
                        <input type="password" name="password" minlength="4" required>
                    </div>

                    <button class="btn btn-primary" type="submit">Admit Student</button>
                    <a class="btn btn-light" href="<%= request.getContextPath() %>/StudentServlet">Cancel</a>
                </form>
            </div>
        </div>
    </main>
</div>
</body>
</html>
