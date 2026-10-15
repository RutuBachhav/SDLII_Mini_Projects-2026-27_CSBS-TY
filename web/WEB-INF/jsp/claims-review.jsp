<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="review" scope="request" />
<c:set var="pageTitle" value="Claims to review" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <h1>Claims to review</h1>
    <p>
        Claims people have made against items you reported as found. Read the private description,
        compare it with the detail you recorded, then decide.
    </p>
</div>

<c:choose>
    <c:when test="${empty claims}">
        <div class="empty">
            <h3>Nothing to review</h3>
            <p>
                When somebody claims an item you found, it lands here and you get an alert. Nothing
                is handed over automatically.
            </p>
            <div class="actions">
                <a class="btn btn-ghost" href="${ctx}/dashboard">Back to your board</a>
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
                                claimed by <c:out value="${cl.claimantName}" /> &middot; ${cl.createdAgo}
                            </p>
                        </div>
                        <span class="chip chip-${fn:toLowerCase(cl.status)}">${fn:toLowerCase(cl.status)}</span>
                    </div>

                    <div class="private-box">
                        <span class="label">Their description &mdash; private</span>
                        <c:out value="${cl.evidence}" />
                    </div>

                    <c:if test="${cl.status ne 'REJECTED'}">
                        <p class="hint mt-2 mb-0">Contact: <span class="strong"><c:out value="${cl.contact}" /></span></p>
                    </c:if>

                    <c:choose>
                        <c:when test="${cl.status eq 'PENDING'}">
                            <form method="post" action="${ctx}/claims/decide" class="mt-3">
                                <input type="hidden" name="csrf" value="${csrf}">
                                <input type="hidden" name="claimId" value="${cl.id}">
                                <input type="hidden" name="back" value="/claims/review">
                                <div class="field">
                                    <label for="note-${cl.id}">Note (optional)</label>
                                    <input type="text" id="note-${cl.id}" name="note" maxlength="255"
                                           placeholder="Matched the scratch on the lid / description did not match">
                                </div>
                                <div class="actions" style="margin:0">
                                    <button class="btn" type="submit" name="decision" value="approve">
                                        Approve this claim
                                    </button>
                                    <button class="btn btn-danger" type="submit" name="decision" value="reject">
                                        Reject
                                    </button>
                                </div>
                            </form>
                        </c:when>
                        <c:when test="${cl.status eq 'APPROVED'}">
                            <div class="alert alert-note mt-3 mb-0">
                                Approved${not empty cl.reviewNote ? ':' : ''}
                                <c:out value="${cl.reviewNote}" />
                                Record the return once the item has actually changed hands.
                            </div>
                            <form method="post" action="${ctx}/claims/return" class="mt-3">
                                <input type="hidden" name="csrf" value="${csrf}">
                                <input type="hidden" name="claimId" value="${cl.id}">
                                <input type="hidden" name="back" value="/claims/review">
                                <div class="field">
                                    <label for="rnote-${cl.id}">Handover note (optional)</label>
                                    <input type="text" id="rnote-${cl.id}" name="note" maxlength="255"
                                           placeholder="Handed over at the hostel office">
                                </div>
                                <div class="actions" style="margin:0">
                                    <button class="btn" type="submit">Record the return</button>
                                </div>
                            </form>
                        </c:when>
                        <c:when test="${cl.status eq 'COMPLETED'}">
                            <div class="alert alert-success mt-3 mb-0">
                                Returned on ${cl.reviewedLabel}. The report is marked resolved and is
                                out of matching.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-note mt-3 mb-0">
                                Rejected on ${cl.reviewedLabel}.
                                <c:if test="${not empty cl.reviewNote}">
                                    Reason: <c:out value="${cl.reviewNote}" />
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="_foot.jspf" %>
