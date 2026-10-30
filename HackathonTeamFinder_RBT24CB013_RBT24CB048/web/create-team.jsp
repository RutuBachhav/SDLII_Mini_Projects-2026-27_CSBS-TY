<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<%@ page import="hackmate.SessionUtil" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create a Team | HackMate</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <nav>
        <div class="logo">HackMate</div>
        <div>
            <a href="index.html">Home</a>
            <a href="teams">Find Teams</a>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </nav>

    <div class="auth-wrap">
        <div class="form-card">
            <h2>Create a team</h2>
            <p class="sub">Describe your project idea and the skills you are looking for.
                You will be the leader of this team.</p>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
                <div class="alert alert-error"><%= HtmlUtil.escape(error) %></div>
            <%
                }
                String teamName = (String) request.getAttribute("teamName");
                String projectTitle = (String) request.getAttribute("projectTitle");
                String description = (String) request.getAttribute("description");
                String requiredSkills = (String) request.getAttribute("requiredSkills");
            %>

            <form method="post" action="<%= request.getContextPath() %>/create-team">

                <div class="field">
                    <label for="teamName">Team name</label>
                    <input type="text" id="teamName" name="teamName" maxlength="100"
                           value="<%= HtmlUtil.escape(teamName) %>"
                           placeholder="Code Warriors" required>
                </div>

                <div class="field">
                    <label for="projectTitle">Project title</label>
                    <input type="text" id="projectTitle" name="projectTitle" maxlength="150"
                           value="<%= HtmlUtil.escape(projectTitle) %>"
                           placeholder="Campus navigation app" required>
                </div>

                <div class="field">
                    <label for="description">Project description</label>
                    <textarea id="description" name="description" rows="5" maxlength="2000"
                              placeholder="What are you building and why?" required><%= HtmlUtil.escape(description) %></textarea>
                </div>

                <div class="field">
                    <label for="requiredSkills">Skills you need</label>
                    <input type="text" id="requiredSkills" name="requiredSkills" maxlength="255"
                           value="<%= HtmlUtil.escape(requiredSkills) %>"
                           placeholder="Java, React, PostgreSQL">
                    <div class="help">Separate the skills with commas. Students can filter the team list by skill.</div>
                </div>

                <button type="submit" class="button">Create team</button>
            </form>

            <p class="hint"><a href="teams">Browse existing teams</a></p>
        </div>
    </div>

    <footer>
        <p>HackMate — Connect. Collaborate. Create.</p>
    </footer>

</body>
</html>
