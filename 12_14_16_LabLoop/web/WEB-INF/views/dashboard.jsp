<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Dashboard" /><c:set var="nav" value="dashboard" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div>
        <h1>Good to see you, <c:out value="${me.firstName}" /></h1>
        <p>${me.staff ? 'Here is what needs the lab team today.' : 'Your lab bookings at a glance.'}</p>
    </div>
    <div style="display:flex;gap:8px">
        <a class="btn" href="${ctx}/equipment?status=AVAILABLE">Book equipment</a>
        <a class="btn sec" href="${ctx}/kit">Practical Kit</a>
    </div>
</div>

<%-- Numbers come from the Singleton LabMonitorBean; the script below refreshes them from GET /api/stats --%>
<div class="stats">
    <div class="stat"><div class="k">Equipment</div><div class="v" data-stat="totalEquipment">${stats.totalEquipment}</div><div class="h"><span data-stat="available">${stats.available}</span> available now</div></div>
    <div class="stat"><div class="k">In use</div><div class="v" data-stat="inUse">${stats.inUse}</div><div class="h"><span data-stat="utilizationPercent">${stats.utilizationPercent}</span>% utilisation</div></div>
    <div class="stat"><div class="k">Booked today</div><div class="v" data-stat="bookingsToday">${stats.bookingsToday}</div><div class="h">active slots</div></div>
    <div class="stat ${stats.pendingApprovals > 0 ? 'warn' : ''}"><div class="k">Pending</div><div class="v" data-stat="pendingApprovals">${stats.pendingApprovals}</div><div class="h">awaiting approval</div></div>
    <div class="stat ${stats.overdueReturns > 0 ? 'alert' : ''}"><div class="k">Overdue</div><div class="v" data-stat="overdueReturns">${stats.overdueReturns}</div><div class="h">not returned on time</div></div>
    <div class="stat ${stats.openTickets > 0 ? 'warn' : ''}"><div class="k">Faults</div><div class="v" data-stat="openTickets">${stats.openTickets}</div><div class="h"><span data-stat="serviceDue">${stats.serviceDue}</span> due for service</div></div>
</div>

<div class="grid g-side">
    <div class="stack">
        <c:if test="${me.staff}">
            <div class="card">
                <div class="card-h"><h2>Approval queue</h2><a class="sub" href="${ctx}/bookings">Open bookings desk →</a></div>
                <c:choose>
                    <c:when test="${empty pending}"><div class="empty">No requests waiting. 🎉</div></c:when>
                    <c:otherwise>
                        <div class="table-wrap"><table class="t">
                            <tr><th>Requested</th><th>Slot</th><th></th></tr>
                            <c:forEach var="b" items="${pending}" end="5">
                                <tr>
                                    <td><div class="cell-main"><c:out value="${b.equipment.name}" /> <span class="tag">${b.equipment.assetTag}</span></div>
                                        <div class="cell-sub"><c:out value="${b.requestedBy.fullName}" /> · <c:out value="${b.purpose}" /></div></td>
                                    <td class="cell-sub">${b.startDisplay}<br>→ ${b.endDisplay}</td>
                                    <td class="actions">
                                        <form class="inline" method="post" action="${ctx}/bookings/approve">
                                            <input type="hidden" name="id" value="${b.id}"><input type="hidden" name="back" value="/dashboard">
                                            <button class="btn sm">Approve</button></form>
                                        <a class="btn sec sm" href="${ctx}/bookings?status=PENDING#b${b.id}">Review</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </table></div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="card">
                <div class="card-h"><h2>Issue / return desk</h2><span class="sub">next 24 hours</span></div>
                <c:if test="${empty pickups and empty overdue}"><div class="empty">Nothing to issue or collect right now.</div></c:if>
                <c:if test="${not empty overdue or not empty pickups}">
                    <div class="table-wrap"><table class="t">
                        <tr><th>Item</th><th>Member</th><th>Slot</th><th></th></tr>
                        <c:forEach var="b" items="${overdue}">
                            <tr>
                                <td><div class="cell-main"><c:out value="${b.equipment.name}" /></div><span class="tag">${b.equipment.assetTag}</span></td>
                                <td><c:out value="${b.requestedBy.fullName}" /></td>
                                <td><span class="badge b-overdue">Overdue</span><div class="cell-sub">was due ${b.endDisplay}</div></td>
                                <td class="actions"><a class="btn sm" href="${ctx}/bookings?status=CHECKED_OUT#b${b.id}">Record return</a></td>
                            </tr>
                        </c:forEach>
                        <c:forEach var="b" items="${pickups}">
                            <tr>
                                <td><div class="cell-main"><c:out value="${b.equipment.name}" /></div><span class="tag">${b.equipment.assetTag}</span></td>
                                <td><c:out value="${b.requestedBy.fullName}" /></td>
                                <td class="cell-sub">${b.startDisplay}</td>
                                <td class="actions">
                                    <form class="inline" method="post" action="${ctx}/bookings/checkout">
                                        <input type="hidden" name="id" value="${b.id}"><input type="hidden" name="back" value="/dashboard">
                                        <button class="btn sec sm">Issue</button></form>
                                </td>
                            </tr>
                        </c:forEach>
                    </table></div>
                </c:if>
            </div>
        </c:if>

        <div class="card">
            <div class="card-h"><h2>My upcoming bookings</h2><a class="sub" href="${ctx}/bookings">All →</a></div>
            <c:choose>
                <c:when test="${empty myUpcoming}"><div class="empty">No upcoming bookings. <a href="${ctx}/equipment?status=AVAILABLE">Find equipment</a></div></c:when>
                <c:otherwise>
                    <table class="t">
                        <c:forEach var="b" items="${myUpcoming}">
                            <tr>
                                <td><div class="cell-main"><c:out value="${b.equipment.name}" /></div>
                                    <div class="cell-sub"><span class="tag">${b.equipment.assetTag}</span> ${b.equipment.lab.code}
                                        <c:if test="${not empty b.requestRef}"> · ${b.requestRef}</c:if></div></td>
                                <td class="cell-sub">${b.startDisplay}<br>→ ${b.endDisplay}</td>
                                <td class="num"><span class="badge b-${b.overdue ? 'overdue' : b.status}">${b.overdue ? 'Overdue' : b.status.label}</span></td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="stack">
        <div class="card">
            <div class="card-h"><h2>Lab load</h2>
                <div class="legend"><span class="l-use">In use</span><span class="l-down">Down</span></div></div>
            <div class="card-b">
                <c:forEach var="l" items="${stats.labs}">
                    <div class="load-row">
                        <div><div class="lc">${l.code}</div><div class="ln"><c:out value="${l.name}" /></div></div>
                        <div class="bar"><i class="use" style="width:${l.inUsePercent}%"></i><i class="down" style="width:${l.downPercent}%"></i></div>
                        <div class="lv">${l.inUse + l.down}/${l.total} busy</div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <c:if test="${me.staff}">
            <div class="card">
                <div class="card-h"><h2>Maintenance</h2><a class="sub" href="${ctx}/maintenance">Open →</a></div>
                <c:if test="${empty tickets and empty serviceDue}"><div class="empty">All equipment healthy.</div></c:if>
                <c:if test="${not empty tickets or not empty serviceDue}">
                    <table class="t">
                        <c:forEach var="t" items="${tickets}" end="3">
                            <tr><td><div class="cell-main"><c:out value="${t.equipment.name}" /></div>
                                <div class="cell-sub"><c:out value="${t.issue}" /></div></td>
                                <td class="num"><span class="badge b-${t.severity}">${t.severity}</span></td></tr>
                        </c:forEach>
                        <c:forEach var="e" items="${serviceDue}" end="3">
                            <tr><td><div class="cell-main"><c:out value="${e.name}" /> <span class="tag">${e.assetTag}</span></div>
                                <div class="cell-sub">Service was due ${e.nextServiceDueDisplay}</div></td>
                                <td class="num"><span class="badge b-PENDING">Service due</span></td></tr>
                        </c:forEach>
                    </table>
                </c:if>
            </div>

            <div class="card">
                <div class="card-h"><h2>Recent activity</h2><a class="sub" href="${ctx}/activity">Log →</a></div>
                <div class="card-b">
                    <ul class="timeline">
                        <c:forEach var="a" items="${recent}">
                            <li><span class="when">${a.occurredDisplay}</span>
                                <span class="what"><b><c:out value="${a.actor}" /></b> · ${a.action}<br><span class="cell-sub"><c:out value="${a.details}" /></span></span></li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>
    </div>
</div>

<p class="cell-sub" style="margin-top:16px">Live figures · refreshed <span id="refreshed">${stats.generatedTime}</span> from <code>/api/stats</code></p>

<script>
    // Poll the REST API every 30 s and update the tiles without reloading the page.
    setInterval(function () {
        fetch('${ctx}/api/stats', {headers: {'Accept': 'application/json'}})
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (s) {
                if (!s) return;
                document.querySelectorAll('[data-stat]').forEach(function (el) {
                    var k = el.getAttribute('data-stat');
                    if (s[k] !== undefined) el.textContent = s[k];
                });
                document.getElementById('refreshed').textContent = new Date().toLocaleTimeString();
            })
            .catch(function () { /* server restarting - try again next tick */ });
    }, 30000);
</script>
<%@ include file="_footer.jspf" %>
