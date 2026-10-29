<%--
    Entry point. The welcome file, so "/" lands here.

    Sends a signed-in student to the dashboard and everyone else to the sign-in
    page. Nothing is rendered, which is why there is no markup below.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
    <c:when test="${not empty sessionScope.userId}">
        <c:redirect url="/dashboard"/>
    </c:when>
    <c:otherwise>
        <c:redirect url="/login"/>
    </c:otherwise>
</c:choose>
