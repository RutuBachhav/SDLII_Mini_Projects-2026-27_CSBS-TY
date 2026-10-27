<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Student" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Student Dashboard</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"STUDENT".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Student student = (Student) request.getAttribute("student");
%>

<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Student Panel</div>
        <a class="active" href="<%= request.getContextPath() %>/StudentDashboardServlet">Dashboard</a>
        <a href="<%= request.getContextPath() %>/MyFeesServlet">My Fees</a>
        <a href="<%= request.getContextPath() %>/MyComplaintsServlet">My Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar">
            <div>Student Dashboard</div>
            <div class="user-info">
                Welcome, <strong><%= session.getAttribute("username") %></strong>
                <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
            </div>
        </div>

        <div class="page-content">
            <h1>Welcome</h1>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <% if (student != null) { %>
                <div class="profile-card">
                    <h2><%= student.getName() %></h2>
                    <p>Contact: <%= student.getContact() == null ? "-" : student.getContact() %></p>
                    <p>Guardian: <%= student.getGuardianContact() == null ? "-" : student.getGuardianContact() %></p>
                    <p>Room: <strong><%= student.getRoom() == null ? "Not assigned" : student.getRoom().getRoomNo() %></strong></p>
                    <p>Admission Date: <%= student.getAdmissionDate() == null ? "-" : student.getAdmissionDate() %></p>
                </div>

                <div class="card-grid">
                    <a class="stat-card" href="<%= request.getContextPath() %>/MyFeesServlet">
                        <div class="icon">Pending Fees</div>
                        <div class="big-number"><%= request.getAttribute("pendingFees") %></div>
                        <div class="subtitle">View your fee records</div>
                    </a>

                    <a class="stat-card" href="<%= request.getContextPath() %>/MyComplaintsServlet">
                        <div class="icon">Open Complaints</div>
                        <div class="big-number"><%= request.getAttribute("openComplaints") %></div>
                        <div class="subtitle">View or raise complaints</div>
                    </a>
                </div>
            <% } %>
        </div>
    </main>
</div>
</body>
</html>
