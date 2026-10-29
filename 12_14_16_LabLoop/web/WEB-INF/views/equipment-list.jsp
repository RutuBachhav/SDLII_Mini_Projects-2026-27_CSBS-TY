<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Equipment" /><c:set var="nav" value="equipment" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><h1>Equipment</h1><p>${fn:length(items)} item(s). Book one item, or add several to a Practical Kit.</p></div>
    <c:if test="${me.staff}"><a class="btn" href="${ctx}/equipment/new">+ Add equipment</a></c:if>
</div>

<div class="card">
    <form class="filters" method="get" action="${ctx}/equipment">
        <div class="grow"><label for="q">Search</label>
            <input type="search" id="q" name="q" placeholder="Name, model or asset tag (e.g. LL-ELC-0004)" value="<c:out value='${fQ}'/>"></div>
        <div><label for="labId">Lab</label>
            <select id="labId" name="labId"><option value="">All labs</option>
                <c:forEach var="l" items="${labs}"><option value="${l.id}" ${l.id == fLab ? 'selected' : ''}>${l.code}</option></c:forEach>
            </select></div>
        <div><label for="category">Category</label>
            <select id="category" name="category"><option value="">All</option>
                <c:forEach var="c" items="${categories}"><option value="${c}" ${c == fCategory ? 'selected' : ''}>${c.label}</option></c:forEach>
            </select></div>
        <div><label for="status">Status</label>
            <select id="status" name="status"><option value="">Active (not retired)</option>
                <c:forEach var="s" items="${statuses}"><option value="${s}" ${s == fStatus ? 'selected' : ''}>${s.label}</option></c:forEach>
            </select></div>
        <div style="min-width:0"><button class="btn sec" type="submit">Filter</button>
            <a class="btn ghost" href="${ctx}/equipment">Reset</a></div>
    </form>
    <c:choose>
        <c:when test="${empty items}"><div class="empty">No equipment matches these filters.</div></c:when>
        <c:otherwise>
            <div class="table-wrap"><table class="t">
                <tr><th>Equipment</th><th>Lab</th><th>Category</th><th>Status</th><th>Next service</th><th></th></tr>
                <c:forEach var="e" items="${items}">
                    <tr>
                        <td><a class="cell-main" href="${ctx}/equipment/view?id=${e.id}"><c:out value="${e.name}" /></a>
                            <div class="cell-sub"><span class="tag">${e.assetTag}</span> <c:out value="${e.model}" /></div></td>
                        <td><a href="${ctx}/labs/view?id=${e.lab.id}">${e.lab.code}</a></td>
                        <td class="cell-sub">${e.category.label}</td>
                        <td><span class="badge b-${e.status}">${e.status.label}</span></td>
                        <td class="cell-sub">${e.nextServiceDueDisplay}
                            <c:if test="${e.serviceOverdue and e.status != 'RETIRED'}"><br><span class="badge b-PENDING">due</span></c:if></td>
                        <td class="actions">
                            <c:if test="${e.bookable}">
                                <a class="btn sm" href="${ctx}/bookings/new?equipmentId=${e.id}">Book</a>
                                <c:choose>
                                    <c:when test="${kit.contains(e.id)}"><span class="badge b-APPROVED">In kit</span></c:when>
                                    <c:otherwise>
                                        <form class="inline" method="post" action="${ctx}/kit/add">
                                            <input type="hidden" name="equipmentId" value="${e.id}">
                                            <input type="hidden" name="back" value="/equipment?${fn:escapeXml(pageContext.request.queryString)}">
                                            <button class="btn sec sm" type="submit">+ Kit</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <a class="btn ghost sm" href="${ctx}/equipment/view?id=${e.id}">Details</a>
                        </td>
                    </tr>
                </c:forEach>
            </table></div>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="_footer.jspf" %>
