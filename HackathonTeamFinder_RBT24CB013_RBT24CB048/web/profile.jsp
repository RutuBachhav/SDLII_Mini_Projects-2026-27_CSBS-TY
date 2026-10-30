<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<%@ page import="hackmate.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile | HackMate</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <nav>
        <div class="logo">HackMate</div>
        <div>
            <a href="index.html">Home</a>
            <a href="teams">Find Teams</a>
            <a href="dashboard">Dashboard</a>
            <a href="team-requests">Requests</a>
            <a href="logout">Logout</a>
        </div>
    </nav>

    <%
        User user = (User) request.getAttribute("user");
        String error = (String) request.getAttribute("error");
        String flashSuccess = (String) session.getAttribute("flashSuccess");
        if (flashSuccess != null) {
            session.removeAttribute("flashSuccess");
        }

        String fullName = (String) request.getAttribute("fullName");
        if (fullName == null && user != null) {
            fullName = user.getFullName();
        }
        String skills = (String) request.getAttribute("skills");
        if (skills == null && user != null) {
            skills = user.getSkills();
        }
    %>

    <div class="auth-wrap">
        <div class="form-card">
            <h2>My profile</h2>
            <p class="sub">Keep your skills up to date so other students can find you.</p>

            <%
                if (flashSuccess != null) {
            %>
                <div class="alert alert-success"><%= HtmlUtil.escape(flashSuccess) %></div>
            <%
                }
                if (error != null) {
            %>
                <div class="alert alert-error"><%= HtmlUtil.escape(error) %></div>
            <%
                }
            %>

            <form method="post" action="<%= request.getContextPath() %>/profile">

                <div class="field">
                    <label for="email">Email address</label>
                    <input type="email" id="email" name="email"
                           value="<%= user != null ? HtmlUtil.escape(user.getEmail()) : "" %>" disabled>
                    <div class="help">The email address is your account name and cannot be changed.</div>
                </div>

                <div class="field">
                    <label for="fullName">Full name</label>
                    <input type="text" id="fullName" name="fullName" maxlength="100"
                           value="<%= HtmlUtil.escape(fullName) %>" required>
                </div>

                <div class="field">
                    <label for="skills">Skills</label>
                    <input type="text" id="skills" name="skills" maxlength="255"
                           value="<%= HtmlUtil.escape(skills) %>"
                           placeholder="Java, React, Machine Learning">
                    <div class="help">Separate your skills with commas.</div>
                </div>

                <h3 style="margin-top:26px; margin-bottom:10px; font-size:17px;">Change password</h3>

                <div class="field">
                    <label for="currentPassword">Current password</label>
                    <input type="password" id="currentPassword" name="currentPassword"
                           maxlength="72" autocomplete="current-password">
                    <div class="help">Leave all three fields empty to keep your current password.</div>
                </div>

                <div class="field">
                    <label for="newPassword">New password</label>
                    <input type="password" id="newPassword" name="newPassword"
                           maxlength="72" autocomplete="new-password">
                </div>

                <div class="field">
                    <label for="confirmPassword">Confirm new password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword"
                           maxlength="72" autocomplete="new-password">
                </div>

                <button type="submit" class="button">Save profile</button>
            </form>

            <p class="hint"><a href="dashboard">Back to the dashboard</a></p>
        </div>
    </div>

    <footer>
        <p>HackMate — Connect. Collaborate. Create.</p>
    </footer>

</body>
</html>
