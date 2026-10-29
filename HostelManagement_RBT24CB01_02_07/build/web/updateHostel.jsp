<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.AuthorizationService" %>
<%@ page import="com.security.Permission" %>
<%@ page import="com.security.SessionAuth" %>
<%@ page import="com.web.Access" %>
<%@ page import="com.web.Layout" %>
<%
    if (!AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.HOSTEL_UPDATE)) {
        Access.renderForbidden(request, response,
                "Your role cannot update hostels.");
        return;
    }
%>
<%= Layout.open("Update Hostel", "Update <span>Hostel</span>", "hostels") %>
<div class="narrow">
<div class="panel">
<div class="panel-head"><div>
<h3>&#9671; Find Hostel to Edit</h3>
<p>Enter the hostel ID to load its details</p>
</div></div>
<div class="panel-body">
<form action="<%= request.getContextPath() %>/getHostelForUpdate" method="post">
<%= Layout.csrfField() %>
<div class="form-group">
<label>Hostel ID</label>
<input type="number" name="hostelId" min="1" placeholder="e.g. 2" required>
</div>
<p class="hint">Tip: open <a href="<%= request.getContextPath() %>/viewHostel">All Hostels</a> to find the correct Hostel ID first.</p>
<button class="btn btn-primary btn-block" type="submit">Retrieve Hostel</button>
</form>
<div class="btn-row">
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/viewHostel">View All Hostels</a>
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/index.jsp">Dashboard</a>
</div>
</div>
</div>
</div>
<%= Layout.close() %>
