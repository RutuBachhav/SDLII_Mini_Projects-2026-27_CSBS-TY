<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="hackmate.HtmlUtil" %>
<%@ page import="hackmate.SessionUtil" %>
<%@ page import="hackmate.TeamRequest" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Join Requests | HackMate</title>
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

    <%
        @SuppressWarnings("unchecked")
        List<TeamRequest> requests = (List<TeamRequest>) request.getAttribute("requests");
        Object pendingObject = request.getAttribute("pendingCount");
        int pendingCount = pendingObject instanceof Integer ? (Integer) pendingObject : 0;

        // Reached directly instead of through TeamRequestsServlet, so no requests are loaded.
        if (requests == null) {
            response.sendRedirect(request.getContextPath() + "/team-requests");
            return;
        }
    %>

    <section class="page-head">
        <h1>Join requests</h1>
        <p>Students who asked to join the teams you lead. Accepting a request adds the
            student to the team straight away.</p>
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
            <h3>Requests (<%= pendingCount %> waiting)</h3>

            <%
                if (requests == null || requests.isEmpty()) {
            %>
                    <p class="empty">No join requests yet.</p>
            <%
                } else {
            %>
                    <%
                        for (TeamRequest joinRequest : requests) {
                            boolean pending = TeamRequest.STATUS_PENDING.equals(joinRequest.getStatus());
                    %>
                            <div class="request-row">
                                <div class="head">
                                    <div>
                                        <strong><%= HtmlUtil.escape(joinRequest.getRequesterName()) %></strong>
                                        <span class="member-email">&nbsp;<%= HtmlUtil.escape(joinRequest.getRequesterEmail()) %></span>
                                    </div>
                                    <%
                                        if (pending) {
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

                                <div class="meta" style="color:#8689a0; font-size:13px;">
                                    Wants to join <strong><%= HtmlUtil.escape(joinRequest.getTeamName()) %></strong>
                                    &nbsp;&middot;&nbsp; <%= HtmlUtil.escape(joinRequest.getProjectTitle()) %>
                                </div>

                                <%
                                    String[] skills = joinRequest.getRequesterSkills() == null
                                            ? new String[0]
                                            : joinRequest.getRequesterSkills().split(",");
                                    boolean hasSkills = false;
                                    for (String skill : skills) {
                                        if (!skill.trim().isEmpty()) {
                                            hasSkills = true;
                                            break;
                                        }
                                    }
                                %>
                                <% if (hasSkills) { %>
                                    <div style="margin-top:8px;">
                                        <% for (String skill : skills) { %>
                                            <% if (!skill.trim().isEmpty()) { %>
                                                <span class="skill-tag"><%= HtmlUtil.escape(skill.trim()) %></span>
                                            <% } %>
                                        <% } %>
                                    </div>
                                <% } %>

                                <%
                                    if (joinRequest.getMessage() != null && !joinRequest.getMessage().isEmpty()) {
                                %>
                                    <div class="message"><%= HtmlUtil.escape(joinRequest.getMessage()) %></div>
                                <%
                                    }
                                %>

                                <%
                                    if (pending) {
                                %>
                                    <div class="request-actions">
                                        <form method="post" action="<%= request.getContextPath() %>/team-requests">
                                            <input type="hidden" name="requestId" value="<%= joinRequest.getRequestId() %>">
                                            <input type="hidden" name="action" value="accept">
                                            <button type="submit" class="button small">Accept</button>
                                        </form>
                                        <form method="post" action="<%= request.getContextPath() %>/team-requests">
                                            <input type="hidden" name="requestId" value="<%= joinRequest.getRequestId() %>">
                                            <input type="hidden" name="action" value="reject">
                                            <button type="submit" class="button secondary small">Reject</button>
                                        </form>
                                    </div>
                                <%
                                    }
                                %>
                            </div>
                    <%
                        }
                    %>
            <%
                }
            %>
        </div>

    </div>

    <footer>
        <p>HackMate — Connect. Collaborate. Create.</p>
    </footer>

</body>
</html>
