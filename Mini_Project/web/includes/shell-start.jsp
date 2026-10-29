<%@ page import="com.util.Html" %>
<%
    String pageTitle = (String) request.getAttribute("pageTitle");
    if (pageTitle == null) {
        pageTitle = "Cinebook";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title><%= Html.esc(pageTitle) %> · Cinebook</title>
    <jsp:include page="/includes/head.jsp"/>
</head>
<body>
<div class="page">
    <jsp:include page="/includes/sidebar.jsp"/>
    <main class="content">
        <h1 class="page-title"><%= Html.esc(pageTitle) %></h1>
        <jsp:include page="/includes/flash.jsp"/>
