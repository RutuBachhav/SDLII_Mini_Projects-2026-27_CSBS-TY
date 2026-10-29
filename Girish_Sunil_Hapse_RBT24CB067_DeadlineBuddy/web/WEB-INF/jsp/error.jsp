<%--
    The one error page.

    Reached three ways: a servlet forwards here through BaseServlet with a title
    and message, CsrfFilter forwards here with a real 403, or the container
    sends an unmapped URL or an uncaught failure here through the error-page
    mappings in web.xml. The last case has no message of its own, so one is
    chosen from the status code.

    Standalone layout on purpose: an error can happen before anyone has signed
    in, and this page should not depend on a session or draw a sign-out button
    for a visitor who has none.

    Nothing from the exception is printed. The stack trace is in the server log.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="status" value="${empty requestScope['jakarta.servlet.error.status_code']
                             ? 0 : requestScope['jakarta.servlet.error.status_code']}"/>
<c:choose>
    <c:when test="${not empty errorTitle}">
        <c:set var="title" value="${errorTitle}"/>
        <c:set var="message" value="${errorMessage}"/>
    </c:when>
    <c:when test="${status eq 404}">
        <c:set var="title" value="Page not found"/>
        <c:set var="message" value="There is nothing at that address."/>
    </c:when>
    <c:when test="${status eq 403}">
        <c:set var="title" value="Not allowed"/>
        <c:set var="message" value="That request was rejected."/>
    </c:when>
    <c:when test="${status eq 405}">
        <c:set var="title" value="Wrong method"/>
        <c:set var="message" value="That address does not accept this kind of request."/>
    </c:when>
    <c:otherwise>
        <c:set var="title" value="Something went wrong"/>
        <c:set var="message" value="DeadlineBuddy hit an unexpected problem. Nothing was changed."/>
    </c:otherwise>
</c:choose>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${title}"/> &middot; DeadlineBuddy</title>
    <link rel="stylesheet" href="<c:url value='/assets/vendor/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>">
</head>
<body>

<div class="auth-wrap">
    <div class="brand-row">
        <a class="brand" href="<c:url value='/dashboard'/>">Deadline<span class="dot">Buddy</span></a>
    </div>

    <div class="auth-card">
        <c:if test="${status gt 0}">
            <div class="eyebrow mb-1">Error ${status}</div>
        </c:if>
        <h1 class="h5 mb-2"><c:out value="${title}"/></h1>
        <p class="text-muted-2 mb-4" style="font-size: .9375rem"><c:out value="${message}"/></p>

        <div class="d-flex gap-2">
            <a class="btn btn-accent" href="<c:url value='/dashboard'/>">Go to the dashboard</a>
            <a class="btn btn-quiet" href="<c:url value='/tasks'/>">Task list</a>
        </div>
    </div>
</div>

</body>
</html>
