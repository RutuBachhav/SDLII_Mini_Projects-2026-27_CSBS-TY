<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Create an account" scope="request" />
<%@ include file="_head.jspf" %>

<div class="row justify-content-center">
    <div class="col-12 col-md-7 col-lg-5">
        <div class="page-head">
            <h1>Create an account</h1>
            <p>Your name is shown on the reports you file. Your email is not.</p>
        </div>

        <c:if test="${not empty errors}">
            <div class="alert alert-error">
                <strong>Please fix these</strong>
                <ul>
                    <c:forEach items="${errors}" var="e"><li><c:out value="${e}" /></li></c:forEach>
                </ul>
            </div>
        </c:if>

        <div class="card">
            <form method="post" action="${ctx}/register">
                <input type="hidden" name="csrf" value="${csrf}">
                <div class="field">
                    <label for="name">Full name</label>
                    <input type="text" id="name" name="name" required autofocus maxlength="80"
                           value="${fn:escapeXml(name)}">
                </div>
                <div class="field">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" required maxlength="160"
                           value="${fn:escapeXml(email)}">
                    <span class="hint">One account per address.</span>
                </div>
                <div class="form-grid">
                    <div class="field">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required minlength="8">
                        <span class="hint">At least 8 characters.</span>
                    </div>
                    <div class="field">
                        <label for="confirm">Repeat password</label>
                        <input type="password" id="confirm" name="confirm" required minlength="8">
                    </div>
                </div>
                <div class="actions">
                    <button class="btn" type="submit">Create account</button>
                    <a class="btn btn-ghost" href="${ctx}/login">I already have one</a>
                </div>
            </form>
        </div>

        <p class="hint mt-3">
            Passwords are stored as PBKDF2-HMAC-SHA256 hashes with a per-account salt. New accounts
            are always ordinary users; the administrator role is never granted by this form.
        </p>
    </div>
</div>

<%@ include file="_foot.jspf" %>
