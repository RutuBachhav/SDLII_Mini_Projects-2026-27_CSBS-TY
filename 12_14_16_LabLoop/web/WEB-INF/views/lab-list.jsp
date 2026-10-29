<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Labs" /><c:set var="nav" value="labs" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><h1>Labs</h1><p>Each lab owns many equipment items (One-to-Many).</p></div>
    <c:if test="${me.staff}"><a class="btn" href="${ctx}/labs/new">+ Add lab</a></c:if>
</div>

<div class="grid g2">
    <c:forEach var="l" items="${labs}">
        <div class="card">
            <div class="card-h">
                <div><h2><a href="${ctx}/labs/view?id=${l.id}"><c:out value="${l.name}" /></a></h2>
                    <div class="sub"><span class="tag">${l.code}</span> <c:out value="${l.location}" /></div></div>
                <span class="badge">${counts[l.id] == null ? 0 : counts[l.id]} items</span>
            </div>
            <div class="card-b" style="display:flex;justify-content:space-between;align-items:center">
                <div class="cell-sub">In-charge: <b><c:out value="${l.inCharge}" /></b> · Seats: ${l.capacity}</div>
                <a class="btn sec sm" href="${ctx}/labs/view?id=${l.id}">Open</a>
            </div>
        </div>
    </c:forEach>
</div>
<c:if test="${empty labs}"><div class="card"><div class="empty">No labs yet.</div></div></c:if>
<%@ include file="_footer.jspf" %>
