<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<%@ page import="hackmate.SessionUtil" %>
<%@ page import="hackmate.Team" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Find Teams | HackMate</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <nav>
        <div class="logo">HackMate</div>
        <div>
            <a href="index.html">Home</a>
            <%
                if (SessionUtil.isLoggedIn(request)) {
            %>
            <a href="dashboard">Dashboard</a>
            <a href="team-requests">Requests</a>
            <a href="logout">Logout</a>
            <%
                } else {
            %>
            <a href="login.jsp">Login</a>
            <a href="register.jsp">Register</a>
            <%
                }
            %>
        </div>
    </nav>

    <%
        @SuppressWarnings("unchecked")
        List<Team> teams = (List<Team>) request.getAttribute("teams");
        String skill = (String) request.getAttribute("skill");
        String q = (String) request.getAttribute("q");
        boolean loggedIn = SessionUtil.isLoggedIn(request);

        // Reached directly instead of through TeamsServlet, so no teams are loaded.
        if (teams == null) {
            response.sendRedirect(request.getContextPath() + "/teams");
            return;
        }
    %>

    <section class="page-head">
        <h1>Find your team</h1>
        <p>Browse the teams that are looking for teammates, and send them a request.</p>
    </section>

    <div class="content-pad">

        <form class="toolbar" method="get" action="<%= request.getContextPath() %>/teams">
            <div class="field">
                <label for="skill">Skill</label>
                <input type="text" id="skill" name="skill" maxlength="100"
                       value="<%= HtmlUtil.escape(skill) %>" placeholder="Java, React, Figma...">
            </div>
            <div class="field">
                <label for="q">Search</label>
                <input type="text" id="q" name="q" maxlength="100"
                       value="<%= HtmlUtil.escape(q) %>" placeholder="Team name or project idea">
            </div>
            <button type="submit" class="button">Filter</button>
        </form>

        <%
            if (teams == null || teams.isEmpty()) {
        %>
            <div class="panel">
                <p class="empty">No team matches your search yet.</p>
                <a class="button" href="create-team">Create the first one</a>
            </div>
        <%
            } else {
        %>
            <div class="team-cards">
                <%
                    for (Team team : teams) {
                %>
                        <div class="team-card">
                            <h3><%= HtmlUtil.escape(team.getTeamName()) %></h3>
                            <div class="project"><%= HtmlUtil.escape(team.getProjectTitle()) %></div>
                            <p class="desc"><%= HtmlUtil.escape(team.getProjectDescription()) %></p>

                            <div class="skills">
                                <%
                                    String[] required = team.getSkillList();
                                    for (String skillName : required) {
                                %>
                                        <span class="skill-tag"><%= HtmlUtil.escape(skillName) %></span>
                                <%
                                    }
                                %>
                            </div>

                            <div class="meta">
                                Led by <%= HtmlUtil.escape(team.getLeaderName()) %>
                                &nbsp;&middot;&nbsp; <%= team.getMemberCount() %> member(s)
                            </div>

                            <a class="button" href="team?id=<%= team.getTeamId() %>">View team</a>
                        </div>
                <%
                    }
                %>
            </div>
        <%
            }
        %>

    </div>

    <footer>
        <p>HackMate — Connect. Collaborate. Create.</p>
    </footer>

</body>
</html>
