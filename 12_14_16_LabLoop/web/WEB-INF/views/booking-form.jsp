<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Book equipment" /><c:set var="nav" value="bookings" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><div class="crumb"><a href="${ctx}/equipment">Equipment</a> / <a href="${ctx}/equipment/view?id=${e.id}"><c:out value="${e.name}" /></a></div>
        <h1>Book <c:out value="${e.name}" /></h1>
        <p><span class="tag">${e.assetTag}</span> ${e.lab.code} · <c:out value="${e.lab.location}" /></p></div>
</div>

<div class="grid g-side">
    <div class="card">
        <div class="card-b">
            <form class="form" method="post" action="${ctx}/bookings/create">
                <input type="hidden" name="equipmentId" value="${e.id}">
                <div class="row">
                    <div><label for="start">From</label>
                        <input type="datetime-local" id="start" name="start" value="${startValue}" min="${minValue}" required></div>
                    <div><label for="end">To</label>
                        <input type="datetime-local" id="end" name="end" value="${endValue}" min="${minValue}" required></div>
                </div>
                <div><label for="purpose">Purpose</label>
                    <textarea id="purpose" name="purpose" required minlength="5" maxlength="200"
                              placeholder="e.g. DSP practical 4 - observing FIR filter output"><c:out value="${purpose}" /></textarea></div>
                <div class="note">
                    You can book up to <b>${maxHours} hours</b> at a time.
                    <c:if test="${isStudent}">Student bookings must be on one day between 08:00 and 20:00, Monday–Saturday.</c:if>
                    ${autoApprove ? 'Your booking is confirmed instantly.' : 'A lab assistant approves student requests before the slot.'}
                </div>
                <div class="form-actions">
                    <button class="btn" type="submit">${autoApprove ? 'Confirm booking' : 'Send request'}</button>
                    <a class="btn ghost" href="${ctx}/equipment/view?id=${e.id}">Cancel</a>
                </div>
            </form>
        </div>
    </div>
    <div class="card">
        <div class="card-h"><h2>Already taken</h2><span class="sub">${dayLabel}</span></div>
        <div class="card-b">
            <c:choose>
                <c:when test="${empty taken}"><div class="cell-sub">No bookings on this day — any slot is free.</div></c:when>
                <c:otherwise>
                    <c:forEach var="b" items="${taken}">
                        <div class="slot"><span>${b.startDisplay} → ${b.endDisplay}</span><span class="badge b-${b.status}">${b.status.label}</span></div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            <p class="hint" style="margin-top:10px">Other days: <a href="${ctx}/equipment/view?id=${e.id}">availability calendar</a>.</p>
        </div>
    </div>
</div>
<%@ include file="_footer.jspf" %>
