<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.AuthorizationService" %>
<%@ page import="com.security.Permission" %>
<%@ page import="com.security.SessionAuth" %>
<%@ page import="com.web.Access" %>
<%@ page import="com.web.Layout" %>
<%
    if (!AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_UPDATE)) {
        Access.renderForbidden(request, response,
                "Your role cannot update rooms.");
        return;
    }
%>
<%= Layout.open("Update Room", "Update <span>Room</span>", "rooms") %>
<div class="narrow">
<div class="panel">
<div class="panel-head"><div>
<h3>&#9671; Find Room to Edit</h3>
<p>Enter the room ID to load its details</p>
</div></div>
<div class="panel-body">
<form action="<%= request.getContextPath() %>/getRoomForUpdate" method="post">
<%= Layout.csrfField() %>
<div class="form-group">
<label>Room ID</label>
<input type="number" name="roomId" min="1" placeholder="e.g. 1" required>
</div>
<p class="hint">Tip: open <a href="<%= request.getContextPath() %>/viewRoom">All Rooms</a> to find the correct Room ID first.</p>
<button class="btn btn-primary btn-block" type="submit">Retrieve Room</button>
</form>
<div class="btn-row">
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/viewRoom">View All Rooms</a>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/index.jsp">Dashboard</a>
</div>
</div>
</div>
</div>
<%= Layout.close() %>
