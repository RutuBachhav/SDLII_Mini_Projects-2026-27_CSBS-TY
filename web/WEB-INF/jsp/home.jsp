<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="home" scope="request" />
<c:set var="pageTitle" value="Campus lost and found" scope="request" />
<%@ include file="_head.jspf" %>

<section class="hero">
    <h1>Lost something on campus? Start here, not in six WhatsApp groups.</h1>
    <p class="lead">
        FoundIt keeps every lost and found report in one place, lines up the ones that look
        like the same object, and then walks the two people through a claim, a verification
        and a recorded handover.
    </p>
    <div class="actions">
        <a class="btn" href="${ctx}/report?type=LOST">I lost something</a>
        <a class="btn btn-ghost" href="${ctx}/report?type=FOUND">I found something</a>
        <a class="btn btn-ghost" href="${ctx}/browse">Browse all reports</a>
    </div>
    <form class="search-big" method="get" action="${ctx}/browse" role="search">
        <input type="search" name="q" placeholder="Search for a wallet, a calculator, a hostel key&hellip;"
               aria-label="Search reports">
        <button class="btn" type="submit">Search</button>
    </form>
    <div class="flow">
        <span class="flow-step"><span class="n">1</span> Report</span>
        <span class="flow-step"><span class="n">2</span> Match</span>
        <span class="flow-step"><span class="n">3</span> Claim</span>
        <span class="flow-step"><span class="n">4</span> Verify</span>
        <span class="flow-step"><span class="n">5</span> Return</span>
    </div>
</section>

<div class="row g-3 mt-3">
    <div class="col-12 col-md-4">
        <div class="stat">
            <div class="label">Open lost reports</div>
            <div class="value">${openLost}</div>
            <div class="foot">waiting for somebody to find them</div>
        </div>
    </div>
    <div class="col-12 col-md-4">
        <div class="stat">
            <div class="label">Items waiting to be claimed</div>
            <div class="value">${openFound}</div>
            <div class="foot">found and held by a student or an office</div>
        </div>
    </div>
    <div class="col-12 col-md-4">
        <div class="stat">
            <div class="label">Returned to their owner</div>
            <div class="value">${returned}</div>
            <div class="foot">handovers recorded on FoundIt</div>
        </div>
    </div>
</div>

<section class="section">
    <div class="card-head">
        <h2>Latest reports</h2>
        <a class="hint" href="${ctx}/browse">See all &rarr;</a>
    </div>
    <c:choose>
        <c:when test="${empty recent}">
            <div class="empty">
                <h3>Nothing has been reported yet</h3>
                <p>
                    The board is empty. The first report is usually the hardest one to write,
                    so keep it short: what the item is, where you last had it, and one detail
                    only you would know.
                </p>
                <div class="actions">
                    <a class="btn" href="${ctx}/report?type=LOST">Report a lost item</a>
                    <a class="btn btn-ghost" href="${ctx}/report?type=FOUND">Report a found item</a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="item-grid">
                <c:forEach items="${recent}" var="it">
                    <%@ include file="_itemcard.jspf" %>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<section class="section">
    <div class="card">
        <div class="card-head">
            <h2>Browse by category</h2>
            <span class="hint">Categories are managed by the campus administrator</span>
        </div>
        <div class="d-flex flex-wrap gap-2">
            <c:forEach items="${categories}" var="cat">
                <a class="chip chip-open" href="${ctx}/browse?category=${cat.id}"><c:out value="${cat.name}" /></a>
            </c:forEach>
        </div>
    </div>
</section>

<%@ include file="_foot.jspf" %>
