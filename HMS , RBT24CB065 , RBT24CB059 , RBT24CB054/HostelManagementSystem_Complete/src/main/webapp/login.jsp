<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hostel Management System - Login</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="login-page">
    <div class="login-box">
        <div class="login-brand">Hostel Management System</div>
        <h1>Sign in</h1>
        <p class="login-subtitle">Use your hostel account to continue.</p>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %>/LoginServlet" method="post">
            <div class="form-group">
                <label>Username</label>
                <input type="text" name="username" autocomplete="username" required>
            </div>

            <div class="form-group">
                <label>Password</label>
                <input type="password" name="password" autocomplete="current-password" required>
            </div>

            <button type="submit" class="btn btn-primary btn-full">Login</button>
        </form>

        <div class="demo-login">
            <strong>Default admin:</strong> admin / admin123
        </div>
    </div>
</body>
</html>
