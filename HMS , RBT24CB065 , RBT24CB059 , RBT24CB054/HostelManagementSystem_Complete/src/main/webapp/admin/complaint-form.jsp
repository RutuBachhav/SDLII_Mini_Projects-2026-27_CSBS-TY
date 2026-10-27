<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Student" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Raise Complaint</title>
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
        <a href="<%= request.getContextPath() %>/StudentServlet">Students</a>
        <a href="<%= request.getContextPath() %>/FeeServlet">Fee Payments</a>
        <a class="active" href="<%= request.getContextPath() %>/ComplaintServlet">Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar"><div>Complaint Management</div></div>
        <div class="page-content">
            <h1>Raise Complaint</h1>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <div class="form-box">
                <form action="<%= request.getContextPath() %>/ComplaintServlet" method="post">
                    <div class="form-group">
                        <label>Student</label>
                        <select name="studentId" required>
                            <option value="">-- Select Student --</option>
                            <% if (students != null) {
                                for (Student s : students) { %>
                                <option value="<%= s.getId() %>"><%= s.getName() %></option>
                            <% }} %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" rows="5" maxlength="500" required></textarea>
                    </div>

                    <button class="btn btn-primary" type="submit">Submit Complaint</button>
                    <a class="btn btn-light" href="<%= request.getContextPath() %>/ComplaintServlet">Cancel</a>
                </form>
            </div>
        </div>
    </main>
</div>
</body>
</html>
