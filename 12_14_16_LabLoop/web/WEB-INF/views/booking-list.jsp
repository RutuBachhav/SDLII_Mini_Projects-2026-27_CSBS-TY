<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="${me.staff ? 'Bookings desk' : 'My bookings'}" /><c:set var="nav" value="bookings" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><h1>${me.staff ? 'Bookings desk' : 'My bookings'}</h1>
        <p>${me.staff ? 'Approve requests, issue items and record returns.' : 'Everything you have requested, newest first.'}</p></div>
    <a class="btn" href="${ctx}/equipment?status=AVAILABLE">New booking</a>
</div>

<c:if test="${me.staff}">
    <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:14px">
        <c:forEach var="s" items="${statuses}">
            <a class="btn ${fStatus == s.name() ? '' : 'sec'} sm" href="${ctx}/bookings?status=${s}">${s.label}</a>
        </c:forEach>
        <a class="btn ${fStatus == 'ALL' ? '' : 'sec'} sm" href="${ctx}/bookings?status=ALL">All</a>
    </div>
    <c:if test="${not empty overdue and fStatus != 'CHECKED_OUT'}">
        <div class="flash err">${fn:length(overdue)} item(s) are overdue. <a href="${ctx}/bookings?status=CHECKED_OUT">Show checked-out items</a></div>
    </c:if>
</c:if>

<div class="card">
    <c:choose>
        <c:when test="${empty bookings}"><div class="empty">No bookings here.</div></c:when>
        <c:otherwise>
            <div class="table-wrap"><table class="t">
                <tr><th>#</th><th>Equipment</th><c:if test="${me.staff}"><th>Member</th></c:if><th>Slot</th><th>Status</th><th></th></tr>
                <c:forEach var="b" items="${bookings}">
                    <tr id="b${b.id}">
                        <td class="cell-sub">${b.id}<c:if test="${not empty b.requestRef}"><br><span class="tag">${b.requestRef}</span></c:if></td>
                        <td><a class="cell-main" href="${ctx}/equipment/view?id=${b.equipment.id}"><c:out value="${b.equipment.name}" /></a>
                            <div class="cell-sub"><span class="tag">${b.equipment.assetTag}</span> ${b.equipment.lab.code} · <c:out value="${b.purpose}" /></div></td>
                        <c:if test="${me.staff}">
                            <td><c:out value="${b.requestedBy.fullName}" /><div class="cell-sub">${b.requestedBy.role.label} · ${b.requestedBy.memberId}</div></td>
                        </c:if>
                        <td class="cell-sub">${b.startDisplay}<br>→ ${b.endDisplay}</td>
                        <td><span class="badge b-${b.overdue ? 'overdue' : b.status}">${b.overdue ? 'Overdue' : b.status.label}</span>
                            <c:if test="${not empty b.staffRemark}"><div class="cell-sub" style="max-width:220px"><c:out value="${b.staffRemark}" /></div></c:if></td>
                        <td class="actions">
                            <c:set var="back" value="/bookings?status=${fStatus}" />
                            <c:if test="${me.staff and b.status == 'PENDING'}">
                                <form class="inline" method="post" action="${ctx}/bookings/approve">
                                    <input type="hidden" name="id" value="${b.id}"><input type="hidden" name="back" value="${back}">
                                    <button class="btn sm">Approve</button></form>
                                <form class="inline" method="post" action="${ctx}/bookings/reject" style="display:inline-flex;gap:4px">
                                    <input type="hidden" name="id" value="${b.id}"><input type="hidden" name="back" value="${back}">
                                    <input type="text" name="remark" placeholder="Reason" required style="width:120px;padding:5px 8px">
                                    <button class="btn danger sm">Reject</button></form>
                            </c:if>
                            <c:if test="${me.staff and b.status == 'APPROVED'}">
                                <form class="inline" method="post" action="${ctx}/bookings/checkout">
                                    <input type="hidden" name="id" value="${b.id}"><input type="hidden" name="back" value="${back}">
                                    <button class="btn sm">Issue item</button></form>
                            </c:if>
                            <c:if test="${me.staff and b.status == 'CHECKED_OUT'}">
                                <form class="inline" method="post" action="${ctx}/bookings/return" style="display:inline-flex;gap:6px;align-items:center">
                                    <input type="hidden" name="id" value="${b.id}"><input type="hidden" name="back" value="${back}">
                                    <input type="text" name="remark" placeholder="Condition note" style="width:130px;padding:5px 8px">
                                    <label class="check" style="margin:0;font-size:12.5px"><input type="checkbox" name="damaged"> damaged</label>
                                    <button class="btn sm">Record return</button></form>
                            </c:if>
                            <c:if test="${(b.status == 'PENDING' or b.status == 'APPROVED') and (b.requestedBy.id == me.userId or me.staff)}">
                                <form class="inline" method="post" action="${ctx}/bookings/cancel" onsubmit="return confirm('Cancel booking #${b.id}?');">
                                    <input type="hidden" name="id" value="${b.id}"><input type="hidden" name="back" value="${me.staff ? back : '/bookings'}">
                                    <button class="btn ghost sm">Cancel</button></form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table></div>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="_footer.jspf" %>
