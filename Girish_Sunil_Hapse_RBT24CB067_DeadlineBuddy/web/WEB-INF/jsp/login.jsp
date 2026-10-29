<%--
    Sign-in.

    The page and taglib directives come first, before any c: tag, because a
    taglib declaration after the prefix has been used is what causes
    "PWC6307: The prefix c ... has been previously used".

    This page has its own layout rather than including head.jspf: there is no
    navigation or sign-out to draw for somebody who is not signed in yet.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in &middot; DeadlineBuddy</title>
    <link rel="stylesheet" href="<c:url value='/assets/vendor/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>">
</head>
<body>

<div class="auth-wrap">

    <div class="brand-row">
        <div class="brand">Deadline<span class="dot">Buddy</span></div>
        <div class="tagline">Assignments, practicals, submissions and exams in one place.</div>
    </div>

    <c:if test="${not empty flash}">
        <div class="alert alert-${flash.type}" role="status"><c:out value="${flash.text}"/></div>
    </c:if>

    <c:if test="${not empty errorSummary}">
        <div class="alert alert-danger" role="alert"><c:out value="${errorSummary}"/></div>
    </c:if>

    <div class="auth-card">
        <h1 class="h5 mb-3">Sign in</h1>

        <form method="post" action="<c:url value='/login'/>" novalidate>
            <input type="hidden" name="csrf" value="${csrfToken}">
            <input type="hidden" name="next" value="<c:out value='${next}'/>">

            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input class="form-control" type="email" id="email" name="email"
                       value="<c:out value='${email}'/>" autocomplete="username"
                       autofocus required>
            </div>

            <div class="mb-3">
                <label class="form-label" for="password">Password</label>
                <input class="form-control" type="password" id="password" name="password"
                       autocomplete="current-password" required>
            </div>

            <button type="submit" class="btn btn-accent w-100">Sign in</button>
        </form>
    </div>

    <p class="auth-alt">
        New here? <a href="<c:url value='/register'/>">Create an account</a>
    </p>
</div>

</body>
</html>
