<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="${e.name}" /><c:set var="nav" value="equipment" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div>
        <div class="crumb"><a href="${ctx}/equipment">Equipment</a> / <a href="${ctx}/labs/view?id=${e.lab.id}">${e.lab.code}</a></div>
        <h1><c:out value="${e.name}" /> <span class="badge b-${e.status}" style="vertical-align:middle">${e.status.label}</span></h1>
        <p><span class="tag">${e.assetTag}</span> <c:out value="${e.model}" /> · ${e.category.label}</p>
    </div>
    <div style="display:flex;gap:8px;flex-wrap:wrap">
        <c:if test="${e.bookable}">
            <a class="btn" href="${ctx}/bookings/new?equipmentId=${e.id}">Book this</a>
            <c:if test="${not inKit}">
                <form class="inline" method="post" action="${ctx}/kit/add">
                    <input type="hidden" name="equipmentId" value="${e.id}">
                    <input type="hidden" name="back" value="/equipment/view?id=${e.id}">
                    <button class="btn sec">+ Add to kit</button></form>
            </c:if>
        </c:if>
        <c:if test="${e.status != 'RETIRED'}"><a class="btn sec" href="${ctx}/maintenance/new?equipmentId=${e.id}">Report a fault</a></c:if>
    </div>
</div>

<div class="grid g-side">
    <div class="stack">
        <div class="card">
            <div class="card-h"><h2>Availability</h2>
                <div class="daynav">
                    <a class="btn ghost sm" href="${ctx}/equipment/view?id=${e.id}&date=${prevDay}">‹</a>
                    <b>${dayLabel}</b>
                    <a class="btn ghost sm" href="${ctx}/equipment/view?id=${e.id}&date=${nextDay}">›</a>
                </div></div>
            <div class="card-b">
                <c:choose>
                    <c:when test="${empty taken}"><div class="cell-sub">Free all day.</div></c:when>
                    <c:otherwise>
                        <c:forEach var="b" items="${taken}">
                            <div class="slot"><span>${b.startDisplay} → ${b.endDisplay}</span>
                                <span><c:out value="${b.requestedBy.fullName}" /> <span class="badge b-${b.status}">${b.status.label}</span></span></div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                <p class="hint" style="margin-top:10px">JSON: <a href="${ctx}/api/equipment/${e.id}/availability?date=${day}" target="_blank"><code>/api/equipment/${e.id}/availability?date=${day}</code></a></p>
            </div>
        </div>

        <div class="card">
            <div class="card-h"><h2>Booking history</h2><span class="sub">last 50</span></div>
            <c:choose>
                <c:when test="${empty bookings}"><div class="empty">Never booked yet.</div></c:when>
                <c:otherwise>
                    <div class="table-wrap"><table class="t">
                        <tr><th>#</th><th>Member</th><th>Slot</th><th>Status</th></tr>
                        <c:forEach var="b" items="${bookings}">
                            <tr><td class="cell-sub">${b.id}</td>
                                <td><c:out value="${b.requestedBy.fullName}" /><div class="cell-sub"><c:out value="${b.purpose}" /></div></td>
                                <td class="cell-sub">${b.startDisplay}<br>→ ${b.endDisplay}</td>
                                <td><span class="badge b-${b.overdue ? 'overdue' : b.status}">${b.overdue ? 'Overdue' : b.status.label}</span></td></tr>
                        </c:forEach>
                    </table></div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="stack">
        <div class="card">
            <div class="card-h"><h2>Asset details</h2>
                <c:if test="${me.staff}"><a class="btn sec sm" href="${ctx}/equipment/edit?id=${e.id}">Edit</a></c:if></div>
            <div class="card-b">
                <dl class="kv">
                    <dt>Lab</dt><dd><a href="${ctx}/labs/view?id=${e.lab.id}">${e.lab.code}</a> · <c:out value="${e.lab.name}" /></dd>
                    <dt>Location</dt><dd><c:out value="${e.lab.location}" /></dd>
                    <dt>Purchased</dt><dd>${e.purchaseDateDisplay}</dd>
                    <dt>Cost</dt><dd>₹ <fmt:formatNumber value="${e.purchaseCost}" maxFractionDigits="0" /></dd>
                    <dt>Service every</dt><dd>${e.serviceIntervalDays} days</dd>
                    <dt>Last serviced</dt><dd>${e.lastServicedDisplay}</dd>
                    <dt>Next service</dt><dd>${e.nextServiceDueDisplay}
                        <c:if test="${e.serviceOverdue and e.status != 'RETIRED'}"> <span class="badge b-PENDING">overdue</span></c:if></dd>
                    <c:if test="${not empty e.notes}"><dt>Notes</dt><dd><c:out value="${e.notes}" /></dd></c:if>
                </dl>
            </div>
        </div>

        <c:if test="${me.staff and e.status != 'RETIRED'}">
            <div class="card">
                <div class="card-h"><h2>Staff actions</h2></div>
                <div class="card-b" style="display:flex;gap:8px;flex-wrap:wrap">
                    <form class="inline" method="post" action="${ctx}/equipment/serviced">
                        <input type="hidden" name="id" value="${e.id}"><button class="btn sec sm">Mark serviced today</button></form>
                    <form class="inline" method="post" action="${ctx}/equipment/retire" onsubmit="return confirm('Retire ${e.assetTag}? It will no longer be bookable.');">
                        <input type="hidden" name="id" value="${e.id}"><button class="btn danger sm">Retire</button></form>
                    <form class="inline" method="post" action="${ctx}/equipment/delete" onsubmit="return confirm('Permanently delete ${e.assetTag}?');">
                        <input type="hidden" name="id" value="${e.id}"><button class="btn danger sm">Delete</button></form>
                </div>
                <p class="hint" style="padding:0 18px 14px">Delete only works for items with no history. Otherwise retire them, so past bookings stay on record.</p>
            </div>
        </c:if>

        <div class="card">
            <div class="card-h"><h2>Repairs</h2></div>
            <c:choose>
                <c:when test="${empty tickets}"><div class="empty">No faults reported.</div></c:when>
                <c:otherwise>
                    <ul class="timeline" style="padding:6px 18px">
                        <c:forEach var="t" items="${tickets}">
                            <li><span class="when">${t.openedDisplay}</span>
                                <span class="what"><span class="badge b-${t.severity}">${t.severity}</span> <span class="badge b-${t.status}">${t.status.label}</span><br>
                                    <c:out value="${t.issue}" />
                                    <c:if test="${not empty t.resolutionNotes}"><br><span class="cell-sub">Fix: <c:out value="${t.resolutionNotes}" /></span></c:if></span></li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
<%@ include file="_footer.jspf" %>
