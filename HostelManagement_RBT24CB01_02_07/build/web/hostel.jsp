<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.security.AuthorizationService" %>
<%@ page import="com.security.Permission" %>
<%@ page import="com.security.SessionAuth" %>
<%@ page import="com.web.Layout" %>
<%
    boolean canCreate = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.HOSTEL_CREATE);
    boolean canUpdate = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.HOSTEL_UPDATE);
    boolean canDelete = AuthorizationService.allowsAny(
            SessionAuth.roles(session), Permission.HOSTEL_DELETE);
%>
<%= Layout.open("Hostels", "Hostel <span>Module</span>", "hostels") %>
<div class="panel">
<div class="panel-head"><div>
<h3>&#127976; Hostels</h3>
<p>Everything your role can do with hostels</p>
</div></div>
<div class="panel-body">
<div class="mod-grid">
<% if (canCreate) { %>
<a href="<%= request.getContextPath() %>/addHostel.jsp" class="mod-link"><span class="ico bg-teal">&#10010;</span><span><b>Add Hostel</b><span>Register a new hostel building</span></span></a>
<% } %>
<a href="<%= request.getContextPath() %>/viewHostel" class="mod-link"><span class="ico bg-blue">&#9776;</span><span><b>View All Hostels</b><span>Browse the full directory</span></span></a>
<% if (canUpdate) { %>
<a href="<%= request.getContextPath() %>/updateHostel.jsp" class="mod-link"><span class="ico bg-amber">&#9671;</span><span><b>Update Hostel</b><span>Edit name, address &amp; floors</span></span></a>
<% } %>
<% if (canDelete) { %>
<a href="<%= request.getContextPath() %>/deleteHostel" class="mod-link"><span class="ico bg-red">&minus;</span><span><b>Delete Hostel</b><span>Remove an empty hostel</span></span></a>
<% } %>
</div>
<% if (!canCreate && !canUpdate && !canDelete) { %>
<p class="hint">Your role has read-only access to hostels.</p>
<% } %>
<div class="btn-row">
<a class="btn btn-secondary" href="<%= request.getContextPath() %>/index.jsp">Back to Dashboard</a>
</div>
</div>
</div>
<%= Layout.close() %>
