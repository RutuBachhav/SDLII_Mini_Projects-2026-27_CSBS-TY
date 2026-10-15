<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="mine" scope="request" />
<c:set var="pageTitle" value="My claims" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <h1>My claims</h1>
    <p>Claims you submitted, and what happened to them.</p>
</div>

<c:choose>
    <c:when test="${empty claims}">
        <div class="empty">
            <h3>You have not claimed anything</h3>
            <p>
                Found items that are still waiting for an owner are on the found board. Open one and
                use &ldquo;This is mine&rdquo; if you recognise it.
            </p>
            <div class="actions">
                <a class="btn" href="${ctx}/browse?type=FOUND">Browse found items</a>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <div class="d-grid gap-3">
            <c:forEach items="${claims}" var="cl">
                <div class="card">
                    <div class="card-head">
                        <div>
                            <h2>
                                <a href="${ctx}/items/view?id=${cl.itemId}"><c:out value="${cl.itemTitle}" /></a>
                            </h2>
                            <p class="hint mb-0">
                                held by <c:out value="${cl.itemReporterName}" /> &middot;
                                submitted ${cl.createdAgo}
                            </p>
                        </div>
                        <span class="chip chip-${fn:toLowerCase(cl.status)}">${fn:toLowerCase(cl.status)}</span>
                    </div>

                    <div class="private-box">
                        <span class="label">What you wrote</span>
                        <c:out value="${cl.evidence}" />
                    </div>

                    <c:if test="${not empty cl.reviewNote}">
                        <div class="alert ${cl.status eq 'REJECTED' ? 'alert-error' : 'alert-note'} mt-3 mb-0">
                            <strong>Reviewer's note:</strong> <c:out value="${cl.reviewNote}" />
                        </div>
                    </c:if>

                    <p class="hint mt-3 mb-0">
                        <c:choose>
                            <c:when test="${cl.status eq 'PENDING'}">
                                Waiting for <c:out value="${cl.itemReporterName}" /> to review it.
                                You will get an alert when they decide.
                            </c:when>
                            <c:when test="${cl.status eq 'APPROVED'}">
                                Approved. Arrange the handover with
                                <c:out value="${cl.itemReporterName}" />; they record the return
                                afterwards.
                            </c:when>
                            <c:when test="${cl.status eq 'COMPLETED'}">
                                Returned on ${cl.reviewedLabel}. Case closed.
                            </c:when>
                            <c:otherwise>
                                Rejected. If you think that was a mistake, file a lost report so the
                                two can be matched and reviewed again.
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="_foot.jspf" %>
