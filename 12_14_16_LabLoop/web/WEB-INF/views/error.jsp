<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${code} · LabLoop</title>
    <link rel="stylesheet" href="${ctx}/assets/labloop.css">
</head>
<body>
<div style="min-height:100vh;display:grid;place-items:center;padding:24px">
    <div class="card" style="max-width:460px;width:100%;padding:28px;text-align:center">
        <div class="brand-mark" style="margin:0 auto 14px;color:#fff">LL</div>
        <h1 style="font-size:40px;letter-spacing:-1px">${code}</h1>
        <p style="color:var(--ink-2);margin:6px 0 18px">
            <c:choose>
                <c:when test="${code == 404}">That page does not exist in LabLoop.</c:when>
                <c:when test="${code == 403}"><c:out value="${requestScope['jakarta.servlet.error.message']}" default="You don't have permission for that." /></c:when>
                <c:otherwise>Something went wrong on the server. Check the GlassFish output window for details.</c:otherwise>
            </c:choose>
        </p>
        <a class="btn" href="${ctx}/dashboard">Back to dashboard</a>
    </div>
</div>
</body>
</html>
