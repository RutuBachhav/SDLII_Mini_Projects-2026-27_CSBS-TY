<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@ include file="_head.jsp" %>
<%
    String nameError = (String) request.getAttribute("nameError");
    String emailError = (String) request.getAttribute("emailError");
    String passwordError = (String) request.getAttribute("passwordError");
    String confirmError = (String) request.getAttribute("confirmError");
    String fullName = attr(request, "fullName");
    String email = attr(request, "email");
%>
<%! private static String attr(javax.servlet.http.HttpServletRequest r, String k) {
        Object v = r.getAttribute(k);
        return v == null ? "" : String.valueOf(v);
    }
%>
<div class="auth-wrap">
    <div class="page-head">
        <h1>Create your account</h1>
        <p>One account for booking, tracking and managing your appointments. Doctor accounts are issued by the hospital — this form creates a patient account.</p>
    </div>
    <div class="panel">
        <form class="form" action="<%= _ctx %>/register" method="post" novalidate>
            <div class="field">
                <label for="fullName">Full name</label>
                <input id="fullName" name="fullName" type="text" autocomplete="name"
                       value="<%= ValidationUtil.esc(fullName) %>" required
                       aria-describedby="fullName-err"
                       <%= nameError != null ? "aria-invalid=\"true\"" : "" %>>
                <% if (nameError != null) { %><p class="error" id="fullName-err"><%= ValidationUtil.esc(nameError) %></p><% } %>
            </div>
            <div class="field">
                <label for="email">Email address</label>
                <p class="hint" id="email-hint">You will sign in with this address.</p>
                <input id="email" name="email" type="email" inputmode="email" autocomplete="email"
                       value="<%= ValidationUtil.esc(email) %>" required
                       aria-describedby="email-hint email-err"
                       <%= emailError != null ? "aria-invalid=\"true\"" : "" %>>
                <% if (emailError != null) { %><p class="error" id="email-err"><%= ValidationUtil.esc(emailError) %></p><% } %>
            </div>
            <div class="field">
                <label for="password">Password</label>
                <p class="hint" id="password-hint">At least 8 characters, with both letters and numbers.</p>
                <input id="password" name="password" type="password" autocomplete="new-password" required
                       aria-describedby="password-hint password-err"
                       <%= passwordError != null ? "aria-invalid=\"true\"" : "" %>>
                <% if (passwordError != null) { %><p class="error" id="password-err"><%= ValidationUtil.esc(passwordError) %></p><% } %>
            </div>
            <div class="field">
                <label for="confirmPassword">Confirm password</label>
                <input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" required
                       aria-describedby="confirm-err"
                       <%= confirmError != null ? "aria-invalid=\"true\"" : "" %>>
                <% if (confirmError != null) { %><p class="error" id="confirm-err"><%= ValidationUtil.esc(confirmError) %></p><% } %>
            </div>
            <button class="btn" type="submit">Create account</button>
        </form>
    </div>
    <p class="auth-alt">Already have an account? <a href="<%= _ctx %>/login">Sign in</a>.</p>
</div>
<%@ include file="_foot.jsp" %>
