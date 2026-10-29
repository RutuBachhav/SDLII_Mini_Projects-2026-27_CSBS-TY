<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="${lab.name}" /><c:set var="nav" value="labs" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><div class="crumb"><a href="${ctx}/labs">Labs</a></div>
        <h1><c:out value="${lab.name}" /></h1>
        <p><span class="tag">${lab.code}</span> <c:out value="${lab.location}" /> · In-charge <c:out value="${lab.inCharge}" /> · ${lab.capacity} seats</p></div>
    <c:if test="${me.staff}">
        <div style="display:flex;gap:8px">
            <a class="btn" href="${ctx}/equipment/new?labId=${lab.id}">+ Add equipment here</a>
            <a class="btn sec" href="${ctx}/labs/edit?id=${lab.id}">Edit lab</a>
            <form class="inline" method="post" action="${ctx}/labs/delete" onsubmit="return confirm('Delete lab ${lab.code}?');">
                <input type="hidden" name="id" value="${lab.id}"><button class="btn danger">Delete</button></form>
        </div>
    </c:if>
</div>

<div class="card">
    <div class="card-h"><h2>Equipment in this lab</h2><span class="sub">${fn:length(items)} active item(s)</span></div>
    <c:choose>
        <c:when test="${empty items}"><div class="empty">No equipment yet.</div></c:when>
        <c:otherwise>
            <div class="table-wrap"><table class="t">
                <tr><th>Equipment</th><th>Category</th><th>Status</th><th>Next service</th><th></th></tr>
                <c:forEach var="e" items="${items}">
                    <tr><td><a class="cell-main" href="${ctx}/equipment/view?id=${e.id}"><c:out value="${e.name}" /></a>
                        <div class="cell-sub"><span class="tag">${e.assetTag}</span> <c:out value="${e.model}" /></div></td>
                        <td class="cell-sub">${e.category.label}</td>
                        <td><span class="badge b-${e.status}">${e.status.label}</span></td>
                        <td class="cell-sub">${e.nextServiceDueDisplay}</td>
                        <td class="actions"><c:if test="${e.bookable}"><a class="btn sm" href="${ctx}/bookings/new?equipmentId=${e.id}">Book</a></c:if></td></tr>
                </c:forEach>
            </table></div>
        </c:otherwise>
    </c:choose>
</div>
<p class="hint" style="margin-top:12px">JSON: <a href="${ctx}/api/labs/${lab.id}/equipment" target="_blank"><code>/api/labs/${lab.id}/equipment</code></a></p>
<%@ include file="_footer.jspf" %>
