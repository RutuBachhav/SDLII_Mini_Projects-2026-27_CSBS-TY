<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.AuthorizationService" %>
<%@ page import="com.security.Permission" %>
<%@ page import="com.security.SessionAuth" %>
<%@ page import="com.web.Layout" %>
<%
    String role = SessionAuth.displayRole(session);
    boolean canAddHostel = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.HOSTEL_CREATE);
    boolean canAddRoom = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_CREATE);
    boolean canManageUsers = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.USER_MANAGE);
    boolean canSearchRooms = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_SEARCH);
    boolean canManageRequests = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_REQUEST_MANAGE);
%>
<%= Layout.open("Dashboard", Layout.esc(role) + " <span>Dashboard</span>", "dashboard") %>
<div class="hero">
<h1>Welcome to Hostel Management</h1>
<p>Manage hostels, rooms, occupancy and fees from one secure dashboard.
You are signed in with <b><%= Layout.esc(role) %></b> access.</p>
</div>
<div class="cards">
<div class="card">
<h3><span class="ico">&#127976;</span> Hostel Module</h3>
<p>Browse the full hostel directory with live room counts.
<% if (canAddHostel) { %>
Administrators can register hostels, update their details, and safely remove
empty buildings.
<% } else { %>
Your role has read-only access to hostel records.
<% } %>
</p>
<div class="stats">
<div class="stat"><b>&#9776;</b><span>View directory</span></div>
<% if (canAddHostel) { %>
<div class="stat"><b>&#10010;</b><span>Add hostels</span></div>
<div class="stat"><b>&#9671;</b><span>Update &amp; delete</span></div>
<% } else { %>
<div class="stat"><b>&#128274;</b><span>Read-only access</span></div>
<% } %>
</div>
<div class="btn-row">
<a class="btn btn-primary" href="<%= request.getContextPath() %>/hostel.jsp">Open Hostels</a>
<% if (canAddHostel) { %>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/addHostel.jsp">+ Add Hostel</a>
<% } %>
</div>
</div>
<div class="card accent">
<h3><span class="ico">&#128719;</span> Room Module</h3>
<p>Track rooms with capacity, monthly fee and live status badges.
<% if (canAddRoom) { %>
Add rooms to hostels and keep their details current.
<% } else { %>
Your role can review room details but cannot change them.
<% } %>
</p>
<div class="stats">
<div class="stat"><b>&#9776;</b><span>View directory</span></div>
<% if (canAddRoom) { %>
<div class="stat"><b>&#10010;</b><span>Add rooms</span></div>
<div class="stat"><b>&#9671;</b><span>Update rooms</span></div>
<% } else { %>
<div class="stat"><b>&#128274;</b><span>Read-only access</span></div>
<% } %>
</div>
<div class="btn-row">
<a class="btn btn-primary" href="<%= request.getContextPath() %>/room.jsp">Open Rooms</a>
<% if (canAddRoom) { %>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/addRoom.jsp">+ Add Room</a>
<% } %>
</div>
</div>
</div>
<% if (canSearchRooms) { %>
<div class="panel mt">
<div class="panel-head"><div>
<h3>&#128269; Find Your Ideal Room</h3>
<p>Availability-based room selection</p>
</div></div>
<div class="panel-body">
<p class="hint" style="margin-top:0">Search genuinely available rooms by
hostel, budget, capacity, and room number. Submit a preference and a Warden
or Administrator can approve the allocation after rechecking capacity.</p>
<div class="btn-row" style="margin-top:0">
<a class="btn btn-primary" href="<%= request.getContextPath() %>/findRoom">Find a Room</a>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/myRoomRequests">My Room Requests</a>
<% if (canManageRequests) { %>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/manageRoomRequests">Manage Requests</a>
<% } %>
</div>
</div>
</div>
<% } %>
<% if (canManageUsers) { %>
<div class="panel mt">
<div class="panel-head"><div>
<h3>&#128100; Administration</h3>
<p>Manage accounts and role assignments</p>
</div></div>
<div class="panel-body">
<p class="hint" style="margin-top:0">Create Warden and Viewer accounts, and
activate or deactivate access without deleting audit history.</p>
<div class="btn-row" style="margin-top:0">
<a class="btn btn-primary" href="<%= request.getContextPath() %>/admin/users">Manage Users</a>
</div>
</div>
</div>
<% } %>
<%= Layout.close() %>
