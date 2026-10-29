<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Settings" /><c:set var="nav" value="settings" />
<%@ include file="_header.jspf" %>

<div class="page-head"><div><h1>Settings</h1>
    <p>Held by the <b>Singleton</b> session bean — one shared copy for the whole application.</p></div></div>

<div class="card" style="max-width:680px">
    <div class="card-b">
        <form class="form" method="post" action="${ctx}/settings">
            <div><label for="institutionName">Institution name</label>
                <input type="text" id="institutionName" name="institutionName" value="<c:out value='${settings.institutionName}'/>" ${me.admin ? '' : 'disabled'} maxlength="80"></div>
            <div class="row3">
                <div><label for="studentMaxHours">Student max hours</label>
                    <input type="number" id="studentMaxHours" name="studentMaxHours" min="1" max="12" value="${settings.studentMaxHours}" ${me.admin ? '' : 'disabled'}></div>
                <div><label for="facultyMaxHours">Faculty max hours</label>
                    <input type="number" id="facultyMaxHours" name="facultyMaxHours" min="1" max="168" value="${settings.facultyMaxHours}" ${me.admin ? '' : 'disabled'}></div>
                <div><label for="maxAdvanceDays">Book ahead (days)</label>
                    <input type="number" id="maxAdvanceDays" name="maxAdvanceDays" min="1" max="120" value="${settings.maxAdvanceDays}" ${me.admin ? '' : 'disabled'}></div>
            </div>
            <c:choose>
                <c:when test="${me.admin}"><div class="form-actions"><button class="btn" type="submit">Save settings</button></div></c:when>
                <c:otherwise><div class="note">Only administrators can change these values.</div></c:otherwise>
            </c:choose>
        </form>
    </div>
</div>
<%@ include file="_footer.jspf" %>
