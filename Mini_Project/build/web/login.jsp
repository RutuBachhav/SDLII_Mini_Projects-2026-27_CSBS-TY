<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.util.Html" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String email = (String) request.getAttribute("email");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Log in · Cinebook</title>
    <jsp:include page="/includes/head.jsp"/>
</head>
<body>
<main class="auth-page">
    <section class="auth-card">
        <a class="wordmark" href="<%= ctx %>/home">cinebook</a>
        <h1>Log in</h1>
        <p class="muted">Book a seat with the same account on this browser.</p>
        <% if (error != null) { %><div class="banner err"><%= Html.esc(error) %></div><% } %>
        <form class="stack" method="post" action="<%= ctx %>/login">
            <label class="field"><span>Email</span><input type="email" name="email" value="<%= Html.esc(email) %>" required></label>
            <label class="field"><span>Password</span><input type="password" name="password" required></label>
            <button class="btn btn-green" type="submit">Log in</button>
        </form>
        <p class="hint">New here? <a href="<%= ctx %>/register"><strong>Create an account</strong></a></p>
        <div class="demo">
            <div><strong>Member</strong> · user@cinebook.com · user123</div>
            <div><strong>Admin</strong> · admin@cinebook.com · admin123</div>
        </div>
    </section>
</main>
</body>
</html>
