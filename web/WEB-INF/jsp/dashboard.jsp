<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="dashboard" scope="request" />
<c:set var="pageTitle" value="Dashboard" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <div class="row-between">
        <div>
            <h1>Your board</h1>
            <p>Everything here belongs to your account: your reports, your claims, and the claims waiting on you.</p>
        </div>
        <div class="actions" style="margin:0">
            <a class="btn btn-ghost btn-sm" href="${ctx}/report?type=LOST">Report lost</a>
            <a class="btn btn-sm" href="${ctx}/report?type=FOUND">Report found</a>
        </div>
    </div>
</div>

<div class="row g-3">
    <div class="col-6 col-md-3">
        <div class="stat"><div class="label">Lost reports</div><div class="value">${fn:length(myLost)}</div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="stat"><div class="label">Found reports</div><div class="value">${fn:length(myFound)}</div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="stat"><div class="label">Your claims</div><div class="value">${fn:length(myClaims)}</div></div>
    </div>
    <div class="col-6 col-md-3">
        <div class="stat">
            <div class="label">Waiting on you</div>
            <div class="value">${pendingForMe}</div>
            <div class="foot"><a href="${ctx}/claims/review">Review inbox</a></div>
        </div>
    </div>
</div>

<section class="section">
    <div class="card-head">
        <h2>Possible matches for your reports</h2>
        <span class="hint">Recomputed whenever a report is filed or edited</span>
    </div>
    <c:choose>
        <c:when test="${empty matches}">
            <div class="empty">
                <h3>No suggestions yet</h3>
                <p>
                    When somebody files a report on the other side of the board that lines up with
                    yours on category, wording, colour, place and date, it appears here and you get
                    an alert.
                </p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-3">
                <c:forEach items="${matches}" var="m">
                    <div class="col-12 col-lg-6">
                        <div class="match h-100">
                            <div class="match-head">
                                <div>
                                    <span class="chip ${m.counterpart.lost ? 'chip-lost' : 'chip-found'}">
                                        ${m.counterpart.lost ? 'Someone lost this' : 'Someone found this'}
                                    </span>
                                    <h3 class="mt-2">
                                        <a href="${ctx}/items/view?id=${m.counterpart.id}">
                                            <c:out value="${m.counterpart.title}" />
                                        </a>
                                    </h3>
                                    <p class="hint mb-0">
                                        against your <c:out value="${m.mine.title}" />
                                    </p>
                                </div>
                                <div class="match-score">
                                    <div class="pct">${m.score}%</div>
                                    <div class="word">${m.strength}</div>
                                </div>
                            </div>
                            <div class="meter"><span style="width:${m.score}%"></span></div>
                            <ul class="reasons">
                                <c:forEach items="${m.reasonList}" var="r"><li><c:out value="${r}" /></li></c:forEach>
                            </ul>
                            <div class="d-flex gap-2">
                                <a class="btn btn-ghost btn-sm" href="${ctx}/items/view?id=${m.counterpart.id}">Open report</a>
                                <c:if test="${m.counterpart.found and m.counterpart.open}">
                                    <a class="btn btn-sm" href="${ctx}/claims/new?item=${m.counterpart.id}">Claim it</a>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<div class="row g-3 section">
    <div class="col-12 col-lg-6">
        <div class="card card-flush">
            <div class="p-3 card-head mb-0">
                <h2>Your lost reports</h2>
                <a class="hint" href="${ctx}/report?type=LOST">Add one &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty myLost}">
                    <p class="hint px-3 pb-3 mb-0">You have not reported anything lost. Good sign.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table class="data">
                            <thead><tr><th>Item</th><th>Date</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach items="${myLost}" var="it">
                                <tr>
                                    <td><a href="${ctx}/items/view?id=${it.id}"><c:out value="${it.title}" /></a></td>
                                    <td class="mono">${it.itemDateLabel}</td>
                                    <td><span class="chip chip-${fn:toLowerCase(it.status)}">${fn:toLowerCase(it.status)}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="col-12 col-lg-6">
        <div class="card card-flush">
            <div class="p-3 card-head mb-0">
                <h2>Items you found</h2>
                <a class="hint" href="${ctx}/report?type=FOUND">Add one &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty myFound}">
                    <p class="hint px-3 pb-3 mb-0">Nothing yet. If you pick something up, report it here.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table class="data">
                            <thead><tr><th>Item</th><th>Date</th><th class="num">Claims</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach items="${myFound}" var="it">
                                <tr>
                                    <td><a href="${ctx}/items/view?id=${it.id}"><c:out value="${it.title}" /></a></td>
                                    <td class="mono">${it.itemDateLabel}</td>
                                    <td class="num">${it.openClaimCount}</td>
                                    <td><span class="chip chip-${fn:toLowerCase(it.status)}">${fn:toLowerCase(it.status)}</span></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div class="row g-3 section">
    <div class="col-12 col-lg-7">
        <div class="card card-flush">
            <div class="p-3 card-head mb-0">
                <h2>Your claims</h2>
                <a class="hint" href="${ctx}/claims/mine">All claims &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty myClaims}">
                    <p class="hint px-3 pb-3 mb-0">
                        You have not claimed anything. Found items you can claim are on the
                        <a href="${ctx}/browse?type=FOUND">found board</a>.
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table class="data">
                            <thead><tr><th>Item</th><th>Submitted</th><th>Status</th></tr></thead>
                            <tbody>
                            <c:forEach items="${myClaims}" var="cl" end="4">
                                <tr>
                                    <td><a href="${ctx}/items/view?id=${cl.itemId}"><c:out value="${cl.itemTitle}" /></a></td>
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
            <div class="card-head">
                <h2>Recent alerts</h2>
                <a class="hint" href="${ctx}/notifications">All alerts &rarr;</a>
            </div>
            <c:choose>
                <c:when test="${empty notifications}">
                    <p class="hint mb-0">Nothing yet. Matches, claims and returns all show up here.</p>
                </c:when>
                <c:otherwise>
                    <div class="notif">
                        <c:forEach items="${notifications}" var="n">
                            <div class="notif-row ${n.read ? '' : 'unread'}">
                                <span>
                                    <c:choose>
                                        <c:when test="${not empty n.link}">
                                            <a href="${ctx}${n.link}"><c:out value="${n.message}" /></a>
                                        </c:when>
                                        <c:otherwise><c:out value="${n.message}" /></c:otherwise>
                                    </c:choose>
                                </span>
                                <span class="when">${n.whenAgo}</span>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="_foot.jspf" %>
