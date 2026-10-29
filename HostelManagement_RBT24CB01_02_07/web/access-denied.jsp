<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.web.Layout" %>
<%
    response.setStatus(403);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Access Denied - Hostel Management</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="auth-body">
<div class="auth-wrap">
<div class="auth-card">
<div class="auth-brand">
<span class="mark">&#127976;</span>
<h1>Hostel Management</h1>
</div>
<h2>Access denied</h2>
<p class="auth-text">Your account does not have permission to open this page.
If you believe this is a mistake, contact an administrator.</p>
<a class="btn btn-primary btn-block" href="<%= request.getContextPath() %>/index.jsp">Return to Dashboard</a>
<p class="auth-footer"><a href="<%= request.getContextPath() %>/login">Switch account</a></p>
</div>
</div>
</body>
</html>
