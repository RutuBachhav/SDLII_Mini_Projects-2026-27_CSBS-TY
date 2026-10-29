<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.AuthorizationService" %>
<%@ page import="com.security.Permission" %>
<%@ page import="com.security.SessionAuth" %>
<%@ page import="com.web.Access" %>
<%@ page import="com.web.Layout" %>
<%
    if (!AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.ROOM_CREATE)) {
        Access.renderForbidden(request, response,
                "Your role cannot add rooms.");
        return;
    }
%>
<%= Layout.open("Add Room", "Add <span>Room</span>", "rooms") %>
<div class="narrow">
<div class="panel">
<div class="panel-head"><div>
<h3>&#10010; New Room</h3>
<p>Assign it to an existing hostel</p>
</div></div>
<div class="panel-body">
<form action="<%= request.getContextPath() %>/saveRoom" method="post">
<%= Layout.csrfField() %>
<div class="form-group">
<label>Room Number</label>
<input type="text" name="roomNumber" placeholder="e.g. A-101" required>
</div>
<div class="form-group">
<label>Capacity (persons)</label>
<input type="number" name="capacity" min="1" placeholder="e.g. 3" required>
</div>
<div class="form-group">
<label>Monthly Fee (&#8377;)</label>
<input type="number" name="monthlyFee" step="0.01" min="0.01" placeholder="e.g. 5500" required>
</div>
<div class="form-group">
<label>Status</label>
<select name="status" required>
<option value="AVAILABLE">AVAILABLE</option>
<option value="OCCUPIED">OCCUPIED</option>
</select>
</div>
<div class="form-group">
<label>Hostel ID</label>
<input type="number" name="hostelId" min="1" placeholder="Check the hostel ID in All Hostels" required>
</div>
<p class="hint">Tip: open <a href="<%= request.getContextPath() %>/viewHostel">All Hostels</a> to find the correct Hostel ID first.</p>
<button class="btn btn-primary btn-block" type="submit">Save Room</button>
</form>
<div class="btn-row">
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/viewRoom">View All Rooms</a>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/index.jsp">Dashboard</a>
</div>
</div>
</div>
</div>
<%= Layout.close() %>
