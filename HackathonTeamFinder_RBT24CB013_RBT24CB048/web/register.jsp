<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account | HackMate</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <nav>
        <div class="logo">HackMate</div>
        <div>
            <a href="index.html">Home</a>
            <a href="#features">Features</a>
            <a href="login.jsp">Login</a>
        </div>
    </nav>

    <div class="auth-wrap">
        <div class="form-card">
            <h2>Create your account</h2>
            <p class="sub">Join HackMate and find teammates for your next hackathon.</p>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
                <div class="alert alert-error"><%= HtmlUtil.escape(error) %></div>
            <%
                }
                String fullName = (String) request.getAttribute("fullName");
                String email = (String) request.getAttribute("email");
                String skills = (String) request.getAttribute("skills");
            %>

            <form method="post" action="<%= request.getContextPath() %>/register">

                <div class="field">
                    <label for="fullName">Full name</label>
                    <input type="text" id="fullName" name="fullName" maxlength="100"
                           value="<%= HtmlUtil.escape(fullName) %>" required>
                </div>

                <div class="field">
                    <label for="email">Email address</label>
                    <input type="email" id="email" name="email" maxlength="100"
                           value="<%= HtmlUtil.escape(email) %>" required>
                </div>

                <div class="field">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" maxlength="72" required>
                    <div class="help">At least 8 characters. Stored as a secure hash, never as text.</div>
                </div>

                <div class="field">
                    <label for="confirmPassword">Confirm password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" maxlength="72" required>
                </div>

                <div class="field">
                    <label for="skills">Skills</label>
                    <input type="text" id="skills" name="skills" maxlength="255"
                           value="<%= HtmlUtil.escape(skills) %>"
                           placeholder="Java, React, Machine Learning">
                    <div class="help">Separate your skills with commas.</div>
                </div>

                <button type="submit" class="button">Create account</button>
            </form>

            <p class="hint">Already have an account? <a href="login.jsp">Log in</a></p>
        </div>
    </div>

    <footer>
        <p>HackMate — Connect. Collaborate. Create.</p>
    </footer>

</body>
</html>
