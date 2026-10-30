<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | HackMate</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <nav>
        <div class="logo">HackMate</div>
        <div>
            <a href="index.html">Home</a>
            <a href="#features">Features</a>
            <a href="register.jsp">Register</a>
        </div>
    </nav>

    <div class="auth-wrap">
        <div class="form-card">
            <h2>Welcome back</h2>
            <p class="sub">Log in to see your teams and join requests.</p>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
                <div class="alert alert-error"><%= HtmlUtil.escape(error) %></div>
            <%
                }
                String success = (String) session.getAttribute("flashSuccess");
                if (success != null) {
                    session.removeAttribute("flashSuccess");
            %>
                <div class="alert alert-success"><%= HtmlUtil.escape(success) %></div>
            <%
                }
                String email = (String) request.getAttribute("email");
            %>

            <form method="post" action="<%= request.getContextPath() %>/login">

                <div class="field">
                    <label for="email">Email address</label>
                    <input type="email" id="email" name="email" maxlength="100"
                           value="<%= HtmlUtil.escape(email) %>" required autofocus>
                </div>

                <div class="field">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" maxlength="72" required>
                </div>

                <button type="submit" class="button">Log in</button>
            </form>

            <p class="hint">No account yet? <a href="register.jsp">Create one</a></p>
        </div>
    </div>

    <footer>
        <p>HackMate — Connect. Collaborate. Create.</p>
    </footer>

</body>
</html>
