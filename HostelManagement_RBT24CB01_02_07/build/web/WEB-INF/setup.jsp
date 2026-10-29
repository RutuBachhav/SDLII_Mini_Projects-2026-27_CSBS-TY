<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.CsrfUtil" %>
<%@ page import="com.web.Layout" %>
<%
    String token = CsrfUtil.ensureToken(session);
    String error = (String) request.getAttribute("error");
    String username = (String) request.getAttribute("username");
    if (username == null) {
        username = "";
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>First-Time Setup - Hostel Management</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="auth-body">
<div class="auth-wrap">
<div class="auth-card">
<div class="auth-brand">
<span class="mark">&#127976;</span>
<h1>Hostel Management</h1>
</div>
<h2>First-time setup</h2>
<p class="auth-text">Create the first Administrator account. This page disables
itself automatically once any account exists.</p>

<% if (error != null) { %>
<div class="auth-alert"><%= Layout.esc(error) %></div>
<% } %>

<form action="<%= request.getContextPath() %>/setup" method="post" autocomplete="off">
<input type="hidden" name="<%= CsrfUtil.PARAM %>" value="<%= Layout.esc(token) %>">
<div class="form-group">
<label for="setupKey">One-time setup key</label>
<input id="setupKey" type="password" name="setupKey" required
       autocomplete="off" autofocus>
</div>
<div class="form-group">
<label for="username">Administrator username</label>
<input id="username" type="text" name="username"
       value="<%= Layout.esc(username) %>" minlength="3" maxlength="60"
       pattern="[A-Za-z0-9._\-]{3,60}" required autocomplete="username">
</div>
<div class="form-group">
<label for="password">Password</label>
<input id="password" type="password" name="password" minlength="10"
       required autocomplete="new-password">
</div>
<div class="form-group">
<label for="confirmPassword">Confirm password</label>
<input id="confirmPassword" type="password" name="confirmPassword"
       minlength="10" required autocomplete="new-password">
</div>
<button class="btn btn-primary btn-block" type="submit">Create Admin Account</button>
</form>

<p class="auth-hint">
The one-time key is written to the GlassFish server log as
<code>RBAC_SETUP_KEY=...</code>. Password must be at least 10 characters and
include a letter and a digit. The key is deleted after setup.
</p>
<p class="auth-footer">
<a href="<%= request.getContextPath() %>/login">Back to sign in</a>
</p>
</div>
</div>
</body>
</html>
