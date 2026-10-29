<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"ADMIN".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Object studentCount = request.getAttribute("studentCount");
    Object roomCount = request.getAttribute("roomCount");
    Object availableRoomCount = request.getAttribute("availableRoomCount");
    Object pendingFeeCount = request.getAttribute("pendingFeeCount");
    Object openComplaintCount = request.getAttribute("openComplaintCount");
%>

<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Hostel Admin</div>
        <a class="active" href="<%= request.getContextPath() %>/AdminDashboardServlet">Dashboard</a>
        <a href="<%= request.getContextPath() %>/RoomServlet">Rooms</a>
        <a href="<%= request.getContextPath() %>/StudentServlet">Students</a>
        <a href="<%= request.getContextPath() %>/FeeServlet">Fee Payments</a>
        <a href="<%= request.getContextPath() %>/ComplaintServlet">Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar">
            <div>Administrator Dashboard</div>
            <div class="user-info">
                Welcome, <strong><%= session.getAttribute("username") %></strong>
                <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
            </div>
        </div>

        <div class="page-content">
            <h1>Dashboard</h1>

            <div class="card-grid">
                <a class="stat-card" href="<%= request.getContextPath() %>/StudentServlet">
                    <div class="icon">Students</div>
                    <div class="big-number"><%= studentCount == null ? 0 : studentCount %></div>
                    <div class="subtitle">Registered students</div>
                </a>

                <a class="stat-card" href="<%= request.getContextPath() %>/RoomServlet">
                    <div class="icon">Rooms</div>
                    <div class="big-number"><%= roomCount == null ? 0 : roomCount %></div>
                    <div class="subtitle">Total rooms</div>
                </a>

                <a class="stat-card" href="<%= request.getContextPath() %>/RoomServlet">
                    <div class="icon">Available</div>
                    <div class="big-number"><%= availableRoomCount == null ? 0 : availableRoomCount %></div>
                    <div class="subtitle">Rooms with free space</div>
                </a>

                <a class="stat-card" href="<%= request.getContextPath() %>/FeeServlet">
                    <div class="icon">Fees</div>
                    <div class="big-number"><%= pendingFeeCount == null ? 0 : pendingFeeCount %></div>
                    <div class="subtitle">Pending fee records</div>
                </a>

                <a class="stat-card" href="<%= request.getContextPath() %>/ComplaintServlet">
                    <div class="icon">Complaints</div>
                    <div class="big-number"><%= openComplaintCount == null ? 0 : openComplaintCount %></div>
                    <div class="subtitle">Open complaints</div>
                </a>
            </div>

            <div class="quick-links">
                <h2>Quick Actions</h2>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/RoomServlet?action=new">Add Room</a>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/StudentServlet?action=new">Admit Student</a>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/FeeServlet?action=new">Add Fee</a>
                <a class="btn btn-primary" href="<%= request.getContextPath() %>/ComplaintServlet?action=new">Raise Complaint</a>
            </div>
        </div>
    </main>
</div>
</body>
</html>
