<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Sign in" scope="request" />
<%@ include file="_head.jspf" %>

<div class="row justify-content-center">
    <div class="col-12 col-md-7 col-lg-5">
        <div class="page-head">
            <h1>Sign in</h1>
            <p>Reporting, claiming and reviewing all need an account.</p>
        </div>

        <c:if test="${not empty errors}">
            <div class="alert alert-error">
                <c:forEach items="${errors}" var="e"><c:out value="${e}" /></c:forEach>
            </div>
        </c:if>

        <div class="card">
            <form method="post" action="${ctx}/login">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="next" value="${fn:escapeXml(param.next)}">
                <div class="field">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" required autofocus
                           value="${fn:escapeXml(email)}">
                </div>
                <div class="field">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" required>
                </div>
                <div class="actions">
                    <button class="btn" type="submit">Sign in</button>
                    <a class="btn btn-ghost" href="${ctx}/register">Create an account</a>
                </div>
            </form>
        </div>

        <p class="hint mt-3">
            Sample student accounts from the seed data use the password
            <span class="mono">Demo@1234</span>, for example
            <span class="mono">aarti@campus.edu</span>. The administrator account has no password
            until somebody sets one with the AdminInit tool.
        </p>
    </div>
</div>

<%@ include file="_foot.jspf" %>
