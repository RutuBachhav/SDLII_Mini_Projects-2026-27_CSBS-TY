<%--
    Registration. Own layout, same reasoning as login.jsp.

    Each field shows the message the server attached to it: ${errors.fullName}
    and so on. The HTML required / type=email attributes are there to save a
    round trip, but AuthService validates everything again regardless.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Create an account &middot; DeadlineBuddy</title>
    <link rel="stylesheet" href="<c:url value='/assets/vendor/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>">
</head>
<body>

<div class="auth-wrap">

    <div class="brand-row">
        <div class="brand">Deadline<span class="dot">Buddy</span></div>
        <div class="tagline">Keep every deadline for the term in one place.</div>
    </div>

    <c:if test="${not empty errorSummary}">
        <div class="alert alert-danger" role="alert"><c:out value="${errorSummary}"/></div>
    </c:if>

    <div class="auth-card">
        <h1 class="h5 mb-3">Create an account</h1>

        <form method="post" action="<c:url value='/register'/>" novalidate>
            <input type="hidden" name="csrf" value="${csrfToken}">

            <div class="mb-3">
                <label class="form-label" for="fullName">Full name</label>
                <input class="form-control ${not empty errors.fullName ? 'is-invalid' : ''}"
                       type="text" id="fullName" name="fullName" maxlength="100"
                       value="<c:out value='${fullName}'/>" autocomplete="name" autofocus required>
                <c:if test="${not empty errors.fullName}">
                    <div class="field-error"><c:out value="${errors.fullName}"/></div>
                </c:if>
            </div>

            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input class="form-control ${not empty errors.email ? 'is-invalid' : ''}"
                       type="email" id="email" name="email" maxlength="190"
                       value="<c:out value='${email}'/>" autocomplete="username" required>
                <c:if test="${not empty errors.email}">
                    <div class="field-error"><c:out value="${errors.email}"/></div>
                </c:if>
            </div>

            <div class="mb-3">
                <label class="form-label" for="password">Password</label>
                <input class="form-control ${not empty errors.password ? 'is-invalid' : ''}"
                       type="password" id="password" name="password"
                       autocomplete="new-password" required>
                <c:choose>
                    <c:when test="${not empty errors.password}">
                        <div class="field-error"><c:out value="${errors.password}"/></div>
                    </c:when>
                    <c:otherwise>
                        <div class="hint">At least 8 characters.</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="mb-3">
                <label class="form-label" for="confirm">Confirm password</label>
                <input class="form-control ${not empty errors.confirm ? 'is-invalid' : ''}"
                       type="password" id="confirm" name="confirm"
                       autocomplete="new-password" required>
                <c:if test="${not empty errors.confirm}">
                    <div class="field-error"><c:out value="${errors.confirm}"/></div>
                </c:if>
            </div>

            <button type="submit" class="btn btn-accent w-100">Create account</button>
        </form>
    </div>

    <p class="auth-alt">
        Already registered? <a href="<c:url value='/login'/>">Sign in</a>
    </p>
</div>

</body>
</html>
