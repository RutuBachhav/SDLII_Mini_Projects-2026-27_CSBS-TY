<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.hospital.module.ValidationUtil"%>
<%@ include file="_head.jsp" %>
<%
    String errTitle = (String) request.getAttribute("errorTitle");
    if (errTitle == null) { errTitle = "Something went wrong"; }
    String errMsg = (String) request.getAttribute("errorMessage");
    if (errMsg == null) { errMsg = "The page you asked for could not be shown. Please try again."; }
    String back = (String) request.getAttribute("backLink");
    if (back == null) { back = _ctx + "/"; }
%>
<div class="page-head">
    <h1><%= ValidationUtil.esc(errTitle) %></h1>
</div>
<div class="empty">
    <p><%= ValidationUtil.esc(errMsg) %></p>
    <p><a class="btn btn-secondary" href="<%= back %>">Go back</a></p>
</div>
<%@ include file="_foot.jsp" %>
