<%-- Context root just forwards to the dashboard (the login filter sends guests to /login). --%>
<% response.sendRedirect(request.getContextPath() + "/dashboard"); %>
