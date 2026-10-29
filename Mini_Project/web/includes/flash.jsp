<%@ page import="com.util.Html" %>
<%
    String flash = (String) session.getAttribute("flash");
    String flashType = (String) session.getAttribute("flashType");
    if (flash != null) {
        session.removeAttribute("flash");
        session.removeAttribute("flashType");
    }
    String pageError = (String) request.getAttribute("error");
    if (flashType == null) {
        flashType = "ok";
    }
%>
<% if (flash != null) { %>
<div class="banner <%= "err".equals(flashType) ? "err" : "ok" %>"><%= Html.esc(flash) %></div>
<% } %>
<% if (pageError != null) { %>
<div class="banner err"><%= Html.esc(pageError) %></div>
<% } %>
