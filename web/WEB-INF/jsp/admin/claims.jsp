<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="admin" scope="request" />
<c:set var="pageTitle" value="Claims" scope="request" />
<%@ include file="../_head.jspf" %>

<div class="page-head">
    <h1>Claims</h1>
    <p>
        Every claim with its private description. An administrator can decide a claim the finder has
        not got to, and can record a return once an approved item has changed hands.
    </p>
</div>

<div class="tab-row">
    <a href="${ctx}/admin">Overview</a>
    <a href="${ctx}/admin/items">Reports</a>
    <a class="on" href="${ctx}/admin/claims">Claims</a>
    <a href="${ctx}/admin/users">Users</a>
    <a href="${ctx}/admin/categories">Categories</a>
</div>

<div class="tab-row">
    <a class="${status eq 'ALL' ? 'on' : ''}" href="${ctx}/admin/claims">All</a>
    <a class="${status eq 'PENDING' ? 'on' : ''}" href="${ctx}/admin/claims?status=PENDING">Pending</a>
    <a class="${status eq 'APPROVED' ? 'on' : ''}" href="${ctx}/admin/claims?status=APPROVED">Approved</a>
    <a class="${status eq 'COMPLETED' ? 'on' : ''}" href="${ctx}/admin/claims?status=COMPLETED">Completed</a>
    <a class="${status eq 'REJECTED' ? 'on' : ''}" href="${ctx}/admin/claims?status=REJECTED">Rejected</a>
</div>

<c:choose>
    <c:when test="${empty claims}">
        <div class="empty">
            <h3>No claims with that status</h3>
            <p>Claims appear as soon as somebody says a found item is theirs.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="d-grid gap-3">
            <c:forEach items="${claims}" var="cl">
                <div class="card">
                    <div class="card-head">
                        <div>
                            <h2><a href="${ctx}/items/view?id=${cl.itemId}"><c:out value="${cl.itemTitle}" /></a></h2>
                            <p class="hint mb-0">
                                <c:out value="${cl.claimantName}" /> claiming from
                                <c:out value="${cl.itemReporterName}" /> &middot; ${cl.createdLabel}
                            </p>
                        </div>
                        <span class="chip chip-${fn:toLowerCase(cl.status)}">${fn:toLowerCase(cl.status)}</span>
                    </div>

                    <div class="private-box">
                        <span class="label">Claimant's description &mdash; private</span>
                        <c:out value="${cl.evidence}" />
                    </div>

                    <dl class="facts mt-3">
                        <div><dt>Contact</dt><dd><c:out value="${cl.contact}" /></dd></div>
                        <c:if test="${not empty cl.reviewedByName}">
                            <div><dt>Reviewed by</dt><dd><c:out value="${cl.reviewedByName}" /> &middot; ${cl.reviewedLabel}</dd></div>
                        </c:if>
                        <c:if test="${not empty cl.reviewNote}">
                            <div><dt>Note</dt><dd><c:out value="${cl.reviewNote}" /></dd></div>
                        </c:if>
                        <div><dt>Return recorded</dt><dd>${cl.returnRecorded ? 'yes' : 'no'}</dd></div>
                    </dl>

                    <c:if test="${cl.status eq 'PENDING'}">
                        <form method="post" action="${ctx}/claims/decide" class="mt-3">
                            <input type="hidden" name="csrf" value="${csrf}">
                            <input type="hidden" name="claimId" value="${cl.id}">
                            <input type="hidden" name="back" value="/admin/claims">
                            <div class="field">
                                <label for="note-${cl.id}">Note</label>
                                <input type="text" id="note-${cl.id}" name="note" maxlength="255">
                            </div>
                            <div class="actions" style="margin:0">
                                <button class="btn" type="submit" name="decision" value="approve">Approve</button>
                                <button class="btn btn-danger" type="submit" name="decision" value="reject">Reject</button>
                            </div>
                        </form>
                    </c:if>
                    <c:if test="${cl.status eq 'APPROVED'}">
                        <form method="post" action="${ctx}/claims/return" class="mt-3">
                            <input type="hidden" name="csrf" value="${csrf}">
                            <input type="hidden" name="claimId" value="${cl.id}">
                            <input type="hidden" name="back" value="/admin/claims">
                            <div class="field">
                                <label for="rnote-${cl.id}">Handover note</label>
                                <input type="text" id="rnote-${cl.id}" name="note" maxlength="255">
                            </div>
                            <div class="actions" style="margin:0">
                                <button class="btn" type="submit">Record the return</button>
                            </div>
                        </form>
                    </c:if>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="../_foot.jspf" %>
