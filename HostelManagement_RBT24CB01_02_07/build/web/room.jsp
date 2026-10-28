<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.AuthorizationService" %>
<%@ page import="com.security.Permission" %>
<%@ page import="com.security.SessionAuth" %>
<%@ page import="com.web.Layout" %>
<%
    boolean canCreate = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_CREATE);
    boolean canUpdate = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_UPDATE);
    boolean canDelete = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_DELETE);
%>
<%= Layout.open("Rooms", "Room <span>Module</span>", "rooms") %>
<div class="panel">
<div class="panel-head"><div>
<h3>&#128719; Rooms</h3>
<p>Everything your role can do with rooms</p>
</div></div>
<div class="panel-body">
<div class="mod-grid">
<% if (canCreate) { %>
<a href="<%= request.getContextPath() %>/addRoom.jsp" class="mod-link"><span class="ico bg-teal">&#10010;</span><span><b>Add Room</b><span>Register a room in a hostel</span></span></a>
<% } %>
<a href="<%= request.getContextPath() %>/viewRoom" class="mod-link"><span class="ico bg-blue">&#9776;</span><span><b>View All Rooms</b><span>Browse rooms with live status</span></span></a>
<% if (canUpdate) { %>
<a href="<%= request.getContextPath() %>/updateRoom.jsp" class="mod-link"><span class="ico bg-amber">&#9671;</span><span><b>Update Room</b><span>Edit details &amp; reassign hostel</span></span></a>
<% } %>
<% if (canDelete) { %>
<a href="<%= request.getContextPath() %>/deleteRoom" class="mod-link"><span class="ico bg-red">&minus;</span><span><b>Delete Room</b><span>Remove a room in one click</span></span></a>
<% } %>
</div>
<% if (!canCreate && !canUpdate && !canDelete) { %>
<p class="hint">Your role has read-only access to rooms.</p>
<% } %>
<div class="btn-row">
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/index.jsp">Back to Dashboard</a>
</div>
</div>
</div>
<%= Layout.close() %>
