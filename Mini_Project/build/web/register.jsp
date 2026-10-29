<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.util.Html" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Create account · Cinebook</title>
    <jsp:include page="/includes/head.jsp"/>
</head>
<body>
<main class="auth-page">
    <section class="auth-card">
        <a class="wordmark" href="<%= ctx %>/home">cinebook</a>
        <h1>Create account</h1>
        <p class="muted">Members can pick seats and keep their tickets.</p>
        <% if (error != null) { %><div class="banner err"><%= Html.esc(error) %></div><% } %>
        <form class="stack" method="post" action="<%= ctx %>/register">
            <label class="field"><span>Name</span><input name="name" value="<%= Html.esc(request.getAttribute("name")) %>" required></label>
            <label class="field"><span>Email</span><input type="email" name="email" value="<%= Html.esc(request.getAttribute("email")) %>" required></label>
            <label class="field"><span>Phone</span><input name="phone" value="<%= Html.esc(request.getAttribute("phone")) %>" placeholder="Optional"></label>
            <label class="field"><span>Password</span><input type="password" name="password" required></label>
            <label class="field"><span>Confirm</span><input type="password" name="confirm" required></label>
            <button class="btn btn-green" type="submit">Create account</button>
        </form>
        <p class="hint">Already have an account? <a href="<%= ctx %>/login"><strong>Log in</strong></a></p>
    </section>
</main>
</body>
</html>
