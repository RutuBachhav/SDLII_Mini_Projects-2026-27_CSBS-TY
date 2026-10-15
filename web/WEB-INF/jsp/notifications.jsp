<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Alerts" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <div class="row-between">
        <div>
            <h1>Alerts</h1>
            <p>Matches, claims, decisions and returns. Stored in MySQL, so they survive a sign-out.</p>
        </div>
        <c:if test="${not empty items}">
            <form method="post" action="${ctx}/notifications/read" style="margin:0">
                <input type="hidden" name="csrf" value="${csrf}">
                <button class="btn btn-ghost btn-sm" type="submit">Mark all as read</button>
            </form>
        </c:if>
    </div>
</div>

<c:choose>
    <c:when test="${empty items}">
        <div class="empty">
            <h3>No alerts yet</h3>
            <p>
                You will hear from FoundIt when a report of yours is matched, when somebody claims an
                item you found, and when a claim of yours is decided.
            </p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="notif">
            <c:forEach items="${items}" var="n">
                <div class="notif-row ${n.read ? '' : 'unread'}">
                    <span>
                        <c:choose>
                            <c:when test="${not empty n.link}">
                                <a href="${ctx}${n.link}"><c:out value="${n.message}" /></a>
                            </c:when>
                            <c:otherwise><c:out value="${n.message}" /></c:otherwise>
                        </c:choose>
                    </span>
                    <span class="d-flex align-items-center gap-2">
                        <span class="when">${n.whenAgo}</span>
                        <c:if test="${not n.read}">
                            <form method="post" action="${ctx}/notifications/read" style="margin:0">
                                <input type="hidden" name="csrf" value="${csrf}">
                                <input type="hidden" name="id" value="${n.id}">
                                <button class="btn btn-ghost btn-sm" type="submit">Mark read</button>
                            </form>
                        </c:if>
                    </span>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="_foot.jspf" %>
