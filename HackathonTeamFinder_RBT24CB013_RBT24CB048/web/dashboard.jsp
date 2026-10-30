<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<%@ page import="hackmate.User" %>
<%@ page import="hackmate.Team" %>
<%@ page import="hackmate.TeamRequest" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | HackMate</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <nav>
        <div class="logo">HackMate</div>
        <div>
            <a href="dashboard">Dashboard</a>
            <a href="teams">Find Teams</a>
            <a href="team-requests">Requests</a>
            <a href="profile">Profile</a>
            <a href="logout">Logout</a>
        </div>
    </nav>

    <%
        User user = (User) request.getAttribute("user");
        List<Team> teams = (List<Team>) request.getAttribute("teams");
        @SuppressWarnings("unchecked")
        List<TeamRequest> myRequests = (List<TeamRequest>) request.getAttribute("myRequests");
        Object ledCount = request.getAttribute("teamsLedCount");
        int teamsLed = ledCount instanceof Integer ? (Integer) ledCount : 0;

        // Reached directly instead of through DashboardServlet, so no user is loaded.
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    %>

    <section class="page-head">
        <h1>Hello, <%= HtmlUtil.escape(user.getFullName()) %></h1>
        <p>This is your HackMate dashboard. Manage your profile and keep track of your teams.</p>
    </section>

    <div style="padding: 0 8%;">

        <div class="panel">
            <h3>My profile</h3>
            <p style="color:#686b80; margin-bottom:14px;"><%= HtmlUtil.escape(user.getEmail()) %></p>

            <%
                String[] skills = user.getSkillList();
                if (skills.length == 0) {
            %>
                <p class="empty">You have not added any skills yet. Add them so other students can find you.</p>
            <%
                } else {
                    for (String skill : skills) {
            %>
                        <span class="skill-tag"><%= HtmlUtil.escape(skill) %></span>
            <%
                    }
                }
            %>
            <div style="margin-top:16px;">
                <a class="button small" href="profile">Edit profile</a>
            </div>
        </div>

        <div class="panel">
            <h3>My teams <span style="color:#8689a0; font-size:15px; font-weight:normal;">
                    (<%= teamsLed %> led by you)</span></h3>

            <%
                if (teams == null || teams.isEmpty()) {
            %>
                <p class="empty">You are not part of any team yet.</p>
                <a class="button" href="teams">Browse teams</a>
            <%
                } else {
                    for (Team team : teams) {
            %>
                        <div style="padding:14px 0; border-bottom:1px solid #eef0f8;">
                            <strong><%= HtmlUtil.escape(team.getTeamName()) %></strong>
                            <span style="color:#8689a0;"> &mdash; <%= HtmlUtil.escape(team.getProjectTitle()) %></span>
                            <div style="color:#686b80; font-size:14px; margin-top:4px;">
                                Led by <%= HtmlUtil.escape(team.getLeaderName()) %>
                                &nbsp;&middot;&nbsp; <%= team.getMemberCount() %> member(s)
                            </div>
                            <%
                                String[] required = team.getSkillList();
                                if (required.length > 0) {
                            %>
                                <div style="margin-top:8px;">
                                    <% for (String skill : required) { %>
                                        <span class="skill-tag"><%= HtmlUtil.escape(skill) %></span>
                                    <% } %>
                                </div>
                            <%
                                }
                            %>
                        </div>
            <%
                    }
                }
            %>
        </div>

        <div class="panel">
            <h3>My join requests</h3>

            <%
                if (myRequests == null || myRequests.isEmpty()) {
            %>
                <p class="empty">You have not asked to join any team yet.</p>
                <a class="button" href="teams">Browse teams</a>
            <%
                } else {
                    for (TeamRequest joinRequest : myRequests) {
            %>
                        <div style="padding:14px 0; border-bottom:1px solid #eef0f8; display:flex; justify-content:space-between; align-items:center; gap:12px; flex-wrap:wrap;">
                            <div>
                                <strong><%= HtmlUtil.escape(joinRequest.getTeamName()) %></strong>
                                <div style="color:#686b80; font-size:14px; margin-top:4px;">
                                    <%= HtmlUtil.escape(joinRequest.getProjectTitle()) %>
                                </div>
                            </div>
                            <%
                                if (TeamRequest.STATUS_PENDING.equals(joinRequest.getStatus())) {
                            %>
                                <span class="badge badge-pending">Pending</span>
                            <%
                                } else if (TeamRequest.STATUS_ACCEPTED.equals(joinRequest.getStatus())) {
                            %>
                                <span class="badge badge-accepted">Accepted</span>
                            <%
                                } else {
                            %>
                                <span class="badge badge-rejected">Rejected</span>
                            <%
                                }
                            %>
                        </div>
            <%
                    }
                }
            %>
        </div>

    </div>

    <footer>
        <p>HackMate — Connect. Collaborate. Create.</p>
    </footer>

</body>
</html>
