<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Maintenance" /><c:set var="nav" value="maintenance" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><h1>Maintenance</h1><p>Fault reports, repairs and calibration reminders.</p></div>
</div>

<div style="display:flex;gap:6px;margin-bottom:14px;flex-wrap:wrap">
    <a class="btn ${fStatus == 'UNRESOLVED' ? '' : 'sec'} sm" href="${ctx}/maintenance">Open &amp; in progress</a>
    <a class="btn ${fStatus == 'RESOLVED' ? '' : 'sec'} sm" href="${ctx}/maintenance?status=RESOLVED">Resolved</a>
    <a class="btn ${fStatus == 'ALL' ? '' : 'sec'} sm" href="${ctx}/maintenance?status=ALL">All</a>
</div>

<div class="grid g-side">
    <div class="card">
        <div class="card-h"><h2>Tickets</h2><span class="sub">${fn:length(tickets)}</span></div>
        <c:choose>
            <c:when test="${empty tickets}"><div class="empty">No tickets here.</div></c:when>
            <c:otherwise>
                <div class="table-wrap"><table class="t">
                    <tr><th>Equipment</th><th>Issue</th><th>Status</th><c:if test="${me.staff}"><th></th></c:if></tr>
                    <c:forEach var="t" items="${tickets}">
                        <tr>
                            <td><a class="cell-main" href="${ctx}/equipment/view?id=${t.equipment.id}"><c:out value="${t.equipment.name}" /></a>
                                <div class="cell-sub"><span class="tag">${t.equipment.assetTag}</span> ${t.equipment.lab.code}</div></td>
                            <td><span class="badge b-${t.severity}">${t.severity}</span> <c:out value="${t.issue}" />
                                <div class="cell-sub">#${t.id} · <c:out value="${t.reportedBy.fullName}" /> · ${t.openedDisplay}</div>
                                <c:if test="${not empty t.resolutionNotes}"><div class="cell-sub">Fix: <c:out value="${t.resolutionNotes}" />
                                    <c:if test="${t.repairCost > 0}"> · ₹<fmt:formatNumber value="${t.repairCost}" maxFractionDigits="0" /></c:if></div></c:if></td>
                            <td><span class="badge b-${t.status}">${t.status.label}</span></td>
                            <c:if test="${me.staff}">
                                <td class="actions">
                                    <c:if test="${t.status == 'OPEN'}">
                                        <form class="inline" method="post" action="${ctx}/maintenance/start">
                                            <input type="hidden" name="id" value="${t.id}"><button class="btn sec sm">Start repair</button></form>
                                    </c:if>
                                    <c:if test="${t.status != 'RESOLVED'}">
                                        <form method="post" action="${ctx}/maintenance/resolve" style="display:flex;gap:4px;margin-top:6px;justify-content:flex-end">
                                            <input type="hidden" name="id" value="${t.id}">
                                            <input type="text" name="notes" placeholder="What was fixed" required style="width:140px;padding:5px 8px">
                                            <input type="number" name="cost" placeholder="₹ cost" min="0" step="1" style="width:80px;padding:5px 8px">
                                            <button class="btn sm">Resolve</button></form>
                                    </c:if>
                                </td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </table></div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="card">
        <div class="card-h"><h2>Service / calibration due</h2><span class="sub">${fn:length(serviceDue)}</span></div>
        <c:choose>
            <c:when test="${empty serviceDue}"><div class="empty">Nothing overdue for service.</div></c:when>
            <c:otherwise>
                <table class="t">
                    <c:forEach var="e" items="${serviceDue}">
                        <tr><td><a class="cell-main" href="${ctx}/equipment/view?id=${e.id}"><c:out value="${e.name}" /></a>
                            <div class="cell-sub"><span class="tag">${e.assetTag}</span> due ${e.nextServiceDueDisplay}</div></td>
                            <td class="actions"><c:if test="${me.staff}">
                                <form class="inline" method="post" action="${ctx}/equipment/serviced">
                                    <input type="hidden" name="id" value="${e.id}"><button class="btn sec sm">Mark done</button></form></c:if></td></tr>
                    </c:forEach>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<%@ include file="_footer.jspf" %>
