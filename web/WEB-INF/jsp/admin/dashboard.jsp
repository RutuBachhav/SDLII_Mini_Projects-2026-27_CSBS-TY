<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="admin" scope="request" />
<c:set var="pageTitle" value="Administration" scope="request" />
<%@ include file="../_head.jspf" %>

<div class="page-head">
    <div class="row-between">
        <div>
            <h1>Administration</h1>
            <p>Live counts straight out of MySQL, with the moderation tools next to them.</p>
        </div>
        <form method="post" action="${ctx}/admin/rematch" style="margin:0">
            <input type="hidden" name="csrf" value="${csrf}">
            <button class="btn btn-ghost btn-sm" type="submit">Rebuild matching</button>
        </form>
    </div>
</div>

<div class="tab-row">
    <a class="on" href="${ctx}/admin">Overview</a>
    <a href="${ctx}/admin/items">Reports</a>
    <a href="${ctx}/admin/claims">Claims</a>
    <a href="${ctx}/admin/users">Users</a>
    <a href="${ctx}/admin/categories">Categories</a>
</div>

<div class="row g-3">
    <div class="col-6 col-md-4 col-xl-2">
        <div class="stat"><div class="label">Users</div><div class="value">${totalUsers}</div></div>
    </div>
    <div class="col-6 col-md-4 col-xl-2">
        <div class="stat"><div class="label">Open lost</div><div class="value">${openLost}</div></div>
    </div>
    <div class="col-6 col-md-4 col-xl-2">
        <div class="stat"><div class="label">Open found</div><div class="value">${openFound}</div></div>
    </div>
    <div class="col-6 col-md-4 col-xl-2">
        <div class="stat">
            <div class="label">Pending claims</div>
            <div class="value">${pendingClaims}</div>
            <div class="foot"><a href="${ctx}/admin/claims?status=PENDING">Review</a></div>
        </div>
    </div>
    <div class="col-6 col-md-4 col-xl-2">
        <div class="stat"><div class="label">Returns</div><div class="value">${completedReturns}</div></div>
    </div>
    <div class="col-6 col-md-4 col-xl-2">
        <div class="stat">
            <div class="label">Suggestions</div>
            <div class="value">${totalMatches}</div>
            <div class="foot">across ${totalReports} reports</div>
        </div>
    </div>
</div>

<div class="row g-3 section">
    <div class="col-12 col-lg-7">
        <div class="card card-flush">
            <div class="p-3 card-head mb-0">
                <h2>Latest claims</h2>
                <a class="hint" href="${ctx}/admin/claims">All claims &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty recentClaims}">
                    <p class="hint px-3 pb-3 mb-0">No claims have been submitted yet.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table class="data">
                            <thead><tr><th>Item</th><th>Claimant</th><th>When</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach items="${recentClaims}" var="cl" end="7">
                                <tr>
                                    <td><a href="${ctx}/items/view?id=${cl.itemId}"><c:out value="${cl.itemTitle}" /></a></td>
                                    <td><c:out value="${cl.claimantName}" /></td>
                                    <td class="mono">${cl.createdAgo}</td>
                                    <td><span class="chip chip-${fn:toLowerCase(cl.status)}">${fn:toLowerCase(cl.status)}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="col-12 col-lg-5">
        <div class="card">
            <h2>Recent activity</h2>
            <c:choose>
                <c:when test="${empty audit}">
                    <p class="hint mt-2 mb-0">No administrative history yet.</p>
                </c:when>
                <c:otherwise>
                    <div class="timeline mt-2">
                        <c:forEach items="${audit}" var="a">
                            <div class="timeline-row">
                                <span class="when">${a.whenLabel}</span>
                                <span>
                                    <span class="strong">${fn:toLowerCase(fn:replace(a.action, '_', ' '))}</span>
                                    <c:if test="${not empty a.actorName}">
                                        &middot; <c:out value="${a.actorName}" />
                                    </c:if>
                                    <c:if test="${not empty a.detail}">
                                        <br><span class="hint"><c:out value="${a.detail}" /></span>
                                    </c:if>
                                </span>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<section class="section">
    <div class="card-head">
        <h2>Newest reports</h2>
        <a class="hint" href="${ctx}/admin/items">Moderate &rarr;</a>
    </div>
    <c:choose>
        <c:when test="${empty recentItems}">
            <div class="empty"><h3>No reports yet</h3><p>The board is empty.</p></div>
        </c:when>
        <c:otherwise>
            <div class="item-grid">
                <c:forEach items="${recentItems}" var="it">
                    <%@ include file="../_itemcard.jspf" %>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="../_foot.jspf" %>
