<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@ include file="_head.jsp" %>
<%
    String loginError = (String) request.getAttribute("loginError");
    String email = (String) request.getAttribute("email");
    if (email == null) { email = ""; }
    String next = (String) request.getAttribute("next");
    if (next == null) {
        next = request.getParameter("next");
        if (next == null) { next = ""; }
    }
    String noticeParam = request.getParameter("notice");
%>
<div class="auth-wrap">
    <div class="page-head">
        <h1>Sign in</h1>
        <p>Use your patient or doctor account to continue.</p>
    </div>
    <% if ("signed-out".equals(noticeParam)) { %>
        <div class="notice notice-success" role="status">
            <p><span class="tag">Done — </span>You have been signed out.</p>
            <button type="button" class="dismiss" data-dismiss aria-label="Dismiss message">&times;</button>
        </div>
    <% } %>
    <% if (loginError != null) { %>
        <div class="notice notice-error" role="alert">
            <p id="login-err"><%= ValidationUtil.esc(loginError) %></p>
        </div>
    <% } %>
    <div class="panel">
        <form class="form" action="<%= _ctx %>/login" method="post" novalidate>
            <input type="hidden" name="next" value="<%= ValidationUtil.esc(next) %>">
            <div class="field">
                <label for="email">Email address</label>
                <input id="email" name="email" type="email" inputmode="email" autocomplete="email"
                       value="<%= ValidationUtil.esc(email) %>" required
                       <%= loginError != null ? "aria-describedby=\"login-err\" aria-invalid=\"true\"" : "" %>>
            </div>
            <div class="field">
                <label for="password">Password</label>
                <input id="password" name="password" type="password" autocomplete="current-password" required
                       <%= loginError != null ? "aria-describedby=\"login-err\" aria-invalid=\"true\"" : "" %>>
            </div>
            <button class="btn" type="submit">Sign in</button>
        </form>
    </div>
    <p class="auth-alt">New to CityCare? <a href="<%= _ctx %>/register">Create a patient account</a>.</p>
</div>
<%@ include file="_foot.jsp" %>
