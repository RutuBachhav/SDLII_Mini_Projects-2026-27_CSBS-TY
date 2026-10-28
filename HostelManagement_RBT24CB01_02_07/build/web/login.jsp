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
    boolean setupDone = "1".equals(request.getParameter("setup"));
    boolean loggedOut = "1".equals(request.getParameter("logout"));
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Sign In - Hostel Management</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="auth-body">
<div class="auth-wrap">
<div class="auth-card">
<div class="auth-brand">
<span class="mark">&#127976;</span>
<h1>Hostel Management</h1>
</div>
<h2>Welcome back</h2>
<p class="auth-text">Sign in with the account assigned to you by an administrator.</p>

<% if (error != null) { %>
<div class="auth-alert"><%= Layout.esc(error) %></div>
<% } %>
<% if (setupDone) { %>
<div class="auth-note">Setup complete. Sign in with your new Admin account.</div>
<% } %>
<% if (loggedOut) { %>
<div class="auth-note">You have been signed out securely.</div>
<% } %>

<form action="<%= request.getContextPath() %>/login" method="post" autocomplete="on">
<input type="hidden" name="<%= CsrfUtil.PARAM %>" value="<%= Layout.esc(token) %>">
<div class="form-group">
<label for="username">Username</label>
<input id="username" type="text" name="username" value="<%= Layout.esc(username) %>"
       maxlength="60" required autofocus autocomplete="username">
</div>
<div class="form-group">
<label for="password">Password</label>
<input id="password" type="password" name="password" required
       autocomplete="current-password">
</div>
<button class="btn btn-primary btn-block" type="submit">Sign In</button>
</form>

<p class="auth-hint">
<b>Role-based access.</b> Admins manage hostels, rooms and users. Wardens manage
rooms. Viewers have read-only access. If you need an account, contact an
administrator.
</p>
</div>
</div>
</body>
</html>
