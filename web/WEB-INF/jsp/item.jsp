<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="browse" scope="request" />
<c:set var="pageTitle" value="${item.title}" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <div class="item-meta mb-2">
        <span class="chip ${item.lost ? 'chip-lost' : 'chip-found'}">${item.lost ? 'Lost item' : 'Found item'}</span>
        <span class="chip chip-${fn:toLowerCase(item.status)}">${fn:toLowerCase(item.status)}</span>
        <span class="chip chip-open"><c:out value="${item.categoryName}" /></span>
    </div>
    <div class="row-between">
        <div>
            <h1><c:out value="${item.title}" /></h1>
            <p>
                Reported by <c:out value="${item.reporterName}" /> &middot; ${item.createdAgo}
            </p>
        </div>
        <div class="actions" style="margin:0">
            <c:if test="${canClaim}">
                <a class="btn" href="${ctx}/claims/new?item=${item.id}">This is mine &mdash; submit a claim</a>
            </c:if>
            <c:if test="${owner or (not empty user and user.admin)}">
                <c:if test="${item.status ne 'RESOLVED'}">
                    <a class="btn btn-ghost btn-sm" href="${ctx}/items/edit?id=${item.id}">Edit</a>
                </c:if>
                <c:choose>
                    <c:when test="${item.status eq 'OPEN'}">
                        <form method="post" action="${ctx}/items/close" style="margin:0">
                            <input type="hidden" name="csrf" value="${csrf}">
                            <input type="hidden" name="id" value="${item.id}">
                            <button class="btn btn-danger btn-sm" type="submit">Close report</button>
                        </form>
                    </c:when>
                    <c:when test="${item.status eq 'CLOSED'}">
                        <form method="post" action="${ctx}/items/reopen" style="margin:0">
                            <input type="hidden" name="csrf" value="${csrf}">
                            <input type="hidden" name="id" value="${item.id}">
                            <button class="btn btn-ghost btn-sm" type="submit">Reopen</button>
                        </form>
                    </c:when>
                </c:choose>
            </c:if>
        </div>
    </div>
</div>

<div class="row g-3">
    <div class="col-12 col-lg-5">
        <div class="detail-photo">
            <c:choose>
                <c:when test="${item.hasPhoto}">
                    <img src="${ctx}/photo?id=${item.photoId}" alt="Photograph of ${fn:escapeXml(item.title)}">
                </c:when>
                <c:otherwise><div class="no-photo">No photograph was attached</div></c:otherwise>
            </c:choose>
        </div>
        <div class="card mt-3">
            <dl class="facts">
                <div><dt>${item.lost ? 'Last seen at' : 'Found at'}</dt><dd><c:out value="${item.location}" /></dd></div>
                <div><dt>${item.lost ? 'Date lost' : 'Date found'}</dt><dd class="mono">${item.itemDateLabel}</dd></div>
                <div><dt>Category</dt><dd><c:out value="${item.categoryName}" /></dd></div>
                <c:if test="${not empty item.color}">
                    <div><dt>Colour</dt><dd><c:out value="${item.color}" /></dd></div>
                </c:if>
                <div><dt>Reported</dt><dd class="mono">${item.createdLabel}</dd></div>
                <c:if test="${item.status eq 'RESOLVED'}">
                    <div><dt>Returned</dt><dd class="mono">${item.resolvedLabel}</dd></div>
                </c:if>
            </dl>
        </div>
    </div>

    <div class="col-12 col-lg-7">
        <div class="card">
            <h2>Description</h2>
            <p class="mt-2" style="white-space:pre-line"><c:out value="${item.description}" /></p>

            <c:choose>
                <c:when test="${item.canSeePrivate and not empty item.privateMarks}">
                    <div class="private-box mt-3">
                        <span class="label">Identifying details &mdash; only you and the administrator see this</span>
                        <c:out value="${item.privateMarks}" />
                    </div>
                </c:when>
                <c:when test="${item.found and not item.canSeePrivate}">
                    <div class="private-box mt-3">
                        <span class="label">Held back on purpose</span>
                        The finder recorded identifying details that are not shown here. A claim is
                        verified by describing them, which is what stops somebody claiming an item
                        from the photograph alone.
                    </div>
                </c:when>
            </c:choose>
        </div>

        <c:if test="${privileged}">
            <div class="card">
                <div class="card-head">
                    <h2>Possible matches</h2>
                    <span class="hint">Scored by category, wording, colour, place and date</span>
                </div>
                <c:choose>
                    <c:when test="${empty matches}">
                        <p class="hint mb-0">
                            Nothing on the other side of the board looks like this yet. FoundIt
                            re-checks every time a new report is filed, and tells you if one turns up.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="d-grid gap-2">
                            <c:forEach items="${matches}" var="m">
                                <div class="match">
                                    <div class="match-head">
                                        <div>
                                            <span class="chip ${m.counterpart.lost ? 'chip-lost' : 'chip-found'}">
                                                ${m.counterpart.lost ? 'Lost' : 'Found'}
                                            </span>
                                            <h3 class="mt-2">
                                                <a href="${ctx}/items/view?id=${m.counterpart.id}">
                                                    <c:out value="${m.counterpart.title}" />
                                                </a>
                                            </h3>
                                            <p class="hint mb-0">
                                                <c:out value="${m.counterpart.location}" /> &middot;
                                                ${m.counterpart.itemDateLabel} &middot;
                                                <c:out value="${m.counterpart.reporterName}" />
                                            </p>
                                        </div>
                                        <div class="match-score">
                                            <div class="pct">${m.score}%</div>
                                            <div class="word">${m.strength}</div>
                                        </div>
                                    </div>
                                    <div class="meter"><span style="width:${m.score}%"></span></div>
                                    <ul class="reasons">
                                        <c:forEach items="${m.reasonList}" var="r">
                                            <li><c:out value="${r}" /></li>
                                        </c:forEach>
                                    </ul>
                                    <div class="d-flex gap-2">
                                        <a class="btn btn-ghost btn-sm" href="${ctx}/items/view?id=${m.counterpart.id}">
                                            Open that report
                                        </a>
                                        <c:if test="${item.lost and m.counterpart.found and m.counterpart.open}">
                                            <a class="btn btn-sm" href="${ctx}/claims/new?item=${m.counterpart.id}">
                                                Claim it
                                            </a>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <c:if test="${item.found}">
                <div class="card">
                    <div class="card-head">
                        <h2>Claims on this item</h2>
                        <a class="hint" href="${ctx}/claims/review">Review inbox &rarr;</a>
                    </div>
                    <c:choose>
                        <c:when test="${empty claims}">
                            <p class="hint mb-0">Nobody has claimed this item yet.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-scroll">
                                <table class="data">
                                    <thead>
                                        <tr><th>Claimant</th><th>Submitted</th><th>Status</th><th></th></tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${claims}" var="cl">
                                        <tr>
                                            <td class="strong"><c:out value="${cl.claimantName}" /></td>
                                            <td class="mono">${cl.createdAgo}</td>
                                            <td><span class="chip chip-${fn:toLowerCase(cl.status)}">${fn:toLowerCase(cl.status)}</span></td>
                                            <td class="actions-cell">
                                                <a class="btn btn-ghost btn-sm" href="${ctx}/claims/review">Open</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>
        </c:if>

        <c:if test="${empty user and item.found and item.open}">
            <div class="card">
                <h2>Is this yours?</h2>
                <p class="hint mt-2 mb-3">
                    Sign in to submit a claim. You will be asked to describe a detail that is not
                    shown on this page, and the person holding the item decides.
                </p>
                <a class="btn" href="${ctx}/login?next=/items/view%3Fid=${item.id}">Sign in to claim</a>
            </div>
        </c:if>
    </div>
</div>

<%@ include file="_foot.jspf" %>
