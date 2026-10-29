<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="com.util.Html" %>
<%
    int code = response.getStatus();
    if (code < 400) {
        code = 404;
    }
    String message = code == 404 ? "That page is not on this site." : "Something went wrong while loading this page.";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title><%= code %> · Cinebook</title>
    <jsp:include page="/includes/head.jsp"/>
</head>
<body>
<main class="error-page">
    <div>
        <p class="kicker"><%= code %></p>
        <h1><%= Html.esc(message) %></h1>
        <a class="btn btn-green" href="<%= request.getContextPath() %>/home">Back home</a>
    </div>
</main>
</body>
</html>
