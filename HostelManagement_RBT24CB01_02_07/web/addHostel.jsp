<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.AuthorizationService" %>
<%@ page import="com.security.Permission" %>
<%@ page import="com.security.SessionAuth" %>
<%@ page import="com.web.Access" %>
<%@ page import="com.web.Layout" %>
<%
    if (!AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.HOSTEL_CREATE)) {
        Access.renderForbidden(request, response,
                "Only administrators can add hostels.");
        return;
    }
%>
<%= Layout.open("Add Hostel", "Add <span>Hostel</span>", "hostels") %>
<div class="narrow">
<div class="panel">
<div class="panel-head"><div>
<h3>&#10010; New Hostel</h3>
<p>Fill in the building details</p>
</div></div>
<div class="panel-body">
<form action="<%= request.getContextPath() %>/saveHostel" method="post">
<%= Layout.csrfField() %>
<div class="form-group">
<label>Hostel Name</label>
<input type="text" name="hostelName" placeholder="e.g. Lake View Hostel" required>
</div>
<div class="form-group">
<label>Address</label>
<input type="text" name="address" placeholder="e.g. 1 Lake Road, Campus Area">
</div>
<div class="form-group">
<label>Total Floors</label>
<input type="number" name="totalFloors" min="1" placeholder="e.g. 4" required>
</div>
<button class="btn btn-primary btn-block" type="submit">Add Hostel</button>
</form>
<div class="btn-row">
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/viewHostel">View All Hostels</a>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/index.jsp">Dashboard</a>
</div>
</div>
</div>
</div>
<%= Layout.close() %>
