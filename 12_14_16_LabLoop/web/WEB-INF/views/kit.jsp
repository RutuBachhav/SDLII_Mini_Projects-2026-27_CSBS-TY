<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Practical Kit" /><c:set var="nav" value="kit" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><h1>Practical Kit</h1>
        <p>Collect everything a practical needs, then reserve it all for one slot. If any item clashes, nothing is booked.</p></div>
    <a class="btn sec" href="${ctx}/equipment?status=AVAILABLE">+ Add more items</a>
</div>

<div class="grid g-side">
    <div class="card">
        <div class="card-h"><h2>Items in your kit</h2><span class="sub">${fn:length(items)} / ${maxItems}</span></div>
        <c:choose>
            <c:when test="${empty items}">
                <div class="empty">Your kit is empty.<br>Open <a href="${ctx}/equipment?status=AVAILABLE">Equipment</a> and press <b>+ Kit</b> on each item you need.</div>
            </c:when>
            <c:otherwise>
                <table class="t">
                    <c:forEach var="i" items="${items}">
                        <tr><td><div class="cell-main"><c:out value="${i.name}" /></div>
                            <div class="cell-sub"><span class="tag">${i.assetTag}</span> ${i.labCode}</div></td>
                            <td class="actions">
                                <form class="inline" method="post" action="${ctx}/kit/remove">
                                    <input type="hidden" name="equipmentId" value="${i.equipmentId}">
                                    <button class="btn ghost sm">Remove</button></form></td></tr>
                    </c:forEach>
                </table>
                <div class="card-b" style="border-top:1px solid var(--line)">
                    <form class="inline" method="post" action="${ctx}/kit/clear"><button class="btn danger sm">Empty kit</button></form>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="card">
        <div class="card-h"><h2>Reserve the kit</h2></div>
        <div class="card-b">
            <form class="form" method="post" action="${ctx}/kit/submit">
                <div><label for="start">From</label>
                    <input type="datetime-local" id="start" name="start" value="${startValue}" min="${minValue}" required></div>
                <div><label for="end">To</label>
                    <input type="datetime-local" id="end" name="end" value="${endValue}" min="${minValue}" required></div>
                <div><label for="purpose">Purpose</label>
                    <textarea id="purpose" name="purpose" required minlength="5" maxlength="200"
                              placeholder="e.g. Experiment 6: UART decoding with logic analyzer"><c:out value="${purpose}" /></textarea></div>
                <button class="btn" type="submit" ${empty items ? 'disabled' : ''}>Reserve ${fn:length(items)} item(s)</button>
            </form>
            <p class="hint" style="margin-top:12px">This page is powered by a <b>Stateful</b> session bean — your kit is remembered while you browse, and is private to your login session.</p>
        </div>
    </div>
</div>
<%@ include file="_footer.jspf" %>
