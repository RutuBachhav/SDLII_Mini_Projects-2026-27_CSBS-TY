<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.hostelmanagementsystem.entity.Complaint" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>My Complaints</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%
    if (!"STUDENT".equals(session.getAttribute("role"))) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Complaint> complaints = (List<Complaint>) request.getAttribute("complaints");
%>
<div class="app-layout">
    <aside class="sidebar">
        <div class="brand">Student Panel</div>
        <a href="<%= request.getContextPath() %>/StudentDashboardServlet">Dashboard</a>
        <a href="<%= request.getContextPath() %>/MyFeesServlet">My Fees</a>
        <a class="active" href="<%= request.getContextPath() %>/MyComplaintsServlet">My Complaints</a>
        <a href="<%= request.getContextPath() %>/LogoutServlet">Logout</a>
    </aside>

    <main class="main-content">
        <div class="topbar"><div>My Complaints</div></div>
        <div class="page-content">
            <h1>My Complaints</h1>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
            <% } %>

            <div class="form-box complaint-box">
                <h2>Raise New Complaint</h2>
                <form action="<%= request.getContextPath() %>/MyComplaintsServlet" method="post">
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" rows="4" maxlength="500" required></textarea>
                    </div>
                    <button class="btn btn-primary" type="submit">Submit Complaint</button>
                </form>
            </div>

            <h2 class="section-heading">Previous Complaints</h2>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>Description</th>
                        <th>Status</th>
                        <th>Date Raised</th>
                        <th>Date Resolved</th>
                    </tr>
                </thead>
                <tbody>
                <% if (complaints != null && !complaints.isEmpty()) {
                    for (Complaint c : complaints) { %>
                    <tr>
                        <td><%= c.getDescription() %></td>
                        <td>
                            <span class="badge <%= "RESOLVED".equals(c.getStatus()) ? "badge-green" : "badge-red" %>">
                                <%= c.getStatus() %>
                            </span>
                        </td>
                        <td><%= c.getDateRaised() == null ? "-" : c.getDateRaised() %></td>
                        <td><%= c.getDateResolved() == null ? "-" : c.getDateResolved() %></td>
                    </tr>
                <% }} else { %>
                    <tr><td colspan="4" class="empty">No complaints found.</td></tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>
