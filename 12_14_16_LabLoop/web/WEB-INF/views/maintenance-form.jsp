<%@ include file="_taglibs.jspf" %>
<c:set var="pageTitle" value="Report a fault" /><c:set var="nav" value="maintenance" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><div class="crumb"><a href="${ctx}/equipment/view?id=${e.id}"><c:out value="${e.name}" /></a></div>
        <h1>Report a fault</h1><p><span class="tag">${e.assetTag}</span> <c:out value="${e.name}" /> · ${e.lab.code}</p></div>
</div>

<div class="card" style="max-width:640px">
    <div class="card-b">
        <form class="form" method="post" action="${ctx}/maintenance/report">
            <input type="hidden" name="equipmentId" value="${e.id}">
            <div><label for="issue">What is wrong?</label>
                <textarea id="issue" name="issue" required minlength="5" maxlength="250"
                          placeholder="e.g. Channel 2 shows no signal even with a known-good probe"><c:out value="${issue}" /></textarea></div>
            <div><label for="severity">Severity</label>
                <select id="severity" name="severity">
                    <c:forEach var="s" items="${severities}"><option value="${s}" ${s == 'MEDIUM' ? 'selected' : ''}>${s}</option></c:forEach>
                </select>
                <div class="hint">HIGH or CRITICAL takes the item out of service immediately so nobody else gets a faulty unit.</div></div>
            <div class="form-actions"><button class="btn" type="submit">Submit report</button>
                <a class="btn ghost" href="${ctx}/equipment/view?id=${e.id}">Cancel</a></div>
        </form>
    </div>
</div>
<%@ include file="_footer.jspf" %>
