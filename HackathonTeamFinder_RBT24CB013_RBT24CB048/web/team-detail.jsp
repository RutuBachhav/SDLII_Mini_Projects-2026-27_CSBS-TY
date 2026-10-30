<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<%@ page import="hackmate.SessionUtil" %>
<%@ page import="hackmate.Team" %>
<%@ page import="hackmate.User" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Team | HackMate</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <nav>
        <div class="logo">HackMate</div>
        <div>
            <a href="index.html">Home</a>
            <a href="teams">Find Teams</a>
            <%
                if (SessionUtil.isLoggedIn(request)) {
            %>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
            <%
                } else {
            %>
            <a href="login.jsp">Login</a>
            <%
                }
            %>
        </div>
    </nav>

    <%
        Team team = (Team) request.getAttribute("team");
        @SuppressWarnings("unchecked")
        List<User> members = (List<User>) request.getAttribute("members");
        boolean loggedIn = SessionUtil.isLoggedIn(request);
        boolean isMember = Boolean.TRUE.equals(request.getAttribute("isMember"));
        boolean isLeader = Boolean.TRUE.equals(request.getAttribute("isLeader"));
        String requestStatus = (String) request.getAttribute("requestStatus");

        // Reached directly instead of through TeamDetailServlet, so no team is loaded.
        if (team == null) {
            response.sendRedirect(request.getContextPath() + "/teams");
            return;
        }
    %>

    <section class="detail-head">
        <h1><%= HtmlUtil.escape(team.getTeamName()) %></h1>
        <div class="project"><%= HtmlUtil.escape(team.getProjectTitle()) %></div>
        <div class="meta">
            Led by <%= HtmlUtil.escape(team.getLeaderName()) %>
            &nbsp;&middot;&nbsp; <%= team.getMemberCount() %> member(s)
        </div>
    </section>

    <div class="content-pad">

        <%
            String flashSuccess = (String) session.getAttribute("flashSuccess");
            if (flashSuccess != null) {
                session.removeAttribute("flashSuccess");
        %>
            <div class="alert alert-success"><%= HtmlUtil.escape(flashSuccess) %></div>
        <%
            }
            String flashError = (String) session.getAttribute("flashError");
            if (flashError != null) {
                session.removeAttribute("flashError");
        %>
            <div class="alert alert-error"><%= HtmlUtil.escape(flashError) %></div>
        <%
            }
        %>

        <div class="panel">
            <h3>About this project</h3>
            <p style="color:#686b80; line-height:1.7;"><%= HtmlUtil.escape(team.getProjectDescription()) %></p>

            <div style="margin-top:18px;">
                <% String[] required = team.getSkillList(); %>
                <% for (String skillName : required) { %>
                    <span class="skill-tag"><%= HtmlUtil.escape(skillName) %></span>
                <% } %>
            </div>
        </div>

        <div class="panel">
            <h3>Members</h3>
            <ul class="member-list">
                <%
                    for (User member : members) {
                %>
                        <li>
                            <div>
                                <div class="member-name">
                                    <%= HtmlUtil.escape(member.getFullName()) %>
                                    <%
                                        if (member.getUserId() == team.getTeamLeaderId()) {
                                    %>
                                        <span class="badge badge-accepted">Leader</span>
                                    <%
                                        }
                                    %>
                                </div>
                                <div class="member-email"><%= HtmlUtil.escape(member.getEmail()) %></div>
                            </div>
                            <div>
                                <%
                                    String[] memberSkills = member.getSkillList();
                                    for (String skillName : memberSkills) {
                                %>
                                        <span class="skill-tag"><%= HtmlUtil.escape(skillName) %></span>
                                <%
                                    }
                                %>
                            </div>
                        </li>
                <%
                    }
                %>
            </ul>
        </div>

        <%
            if (isLeader) {
        %>
            <div class="panel">
                <h3>Join requests</h3>
                <p class="empty">Students who want to join your team send you a request.</p>
                <a class="button" href="team-requests">Manage requests</a>
            </div>
        <%
            } else if (isMember) {
        %>
            <div class="panel">
                <p class="empty">You are a member of this team.</p>
            </div>
        <%
            } else if ("Pending".equals(requestStatus)) {
        %>
            <div class="panel">
                <p class="empty">Your request is waiting for the team leader.</p>
            </div>
        <%
            } else if ("Rejected".equals(requestStatus)) {
        %>
            <div class="panel">
                <p class="empty">Your last request was rejected. You can send a new one.</p>
            </div>
        <%
            }
        %>

        <%
            if (!isMember && !"Pending".equals(requestStatus)) {
        %>
            <div class="panel">
                <h3>Join this team</h3>
                <%
                    if (loggedIn) {
                %>
                        <form method="post" action="<%= request.getContextPath() %>/join-team">
                            <input type="hidden" name="teamId" value="<%= team.getTeamId() %>">
                            <div class="field">
                                <label for="message">Why do you want to join?</label>
                                <textarea id="message" name="message" rows="4" maxlength="1000"
                                          placeholder="Tell the leader what you can bring to the team."></textarea>
                            </div>
                            <button type="submit" class="button">Request to join</button>
                        </form>
                <%
                    } else {
                %>
                        <p class="empty">You need an account to request to join a team.</p>
                        <a class="button" href="login.jsp">Log in</a>
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
