<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Activity log" /><c:set var="nav" value="activity" />
<%@ include file="_header.jspf" %>

<div class="page-head"><div><h1>Activity log</h1>
    <p>Written automatically by a CDI event observer — services just fire <code>LabActivity</code> events.</p></div></div>

<div class="card">
    <div class="table-wrap"><table class="t">
        <tr><th>When</th><th>Who</th><th>Action</th><th>Details</th></tr>
        <c:forEach var="a" items="${rows}">
            <tr><td class="cell-sub" style="white-space:nowrap">${a.occurredDisplay}</td>
                <td><c:out value="${a.actor}" /></td>
                <td><span class="tag">${a.action}</span></td>
                <td class="cell-sub"><c:out value="${a.details}" /></td></tr>
        </c:forEach>
    </table></div>
    <c:if test="${empty rows}"><div class="empty">Nothing logged yet.</div></c:if>
</div>
<%@ include file="_footer.jspf" %>
