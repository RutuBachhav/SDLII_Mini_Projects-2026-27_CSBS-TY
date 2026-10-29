<%@ include file="_taglibs.jspf" %>
<c:set var="isNew" value="${empty form.id}" />
<c:set var="pageTitle" value="${isNew ? 'Add lab' : 'Edit lab'}" /><c:set var="nav" value="labs" />
<%@ include file="_header.jspf" %>

<div class="page-head"><div><div class="crumb"><a href="${ctx}/labs">Labs</a></div><h1>${isNew ? 'Add lab' : 'Edit lab'}</h1></div></div>

<div class="card" style="max-width:720px">
    <div class="card-b">
        <form class="form" method="post" action="${ctx}/labs/save">
            <c:if test="${not isNew}"><input type="hidden" name="id" value="${form.id}"></c:if>
            <div class="row">
                <div><label for="code">Lab code</label>
                    <input type="text" id="code" name="code" value="<c:out value='${form.code}'/>" required placeholder="CSL-101" maxlength="12">
                    <div class="hint">2–5 capital letters, dash, 3 digits.</div></div>
                <div><label for="capacity">Seating capacity</label>
                    <input type="number" id="capacity" name="capacity" min="1" max="300" value="${form.capacity > 0 ? form.capacity : ''}" required></div>
            </div>
            <div><label for="name">Lab name</label>
                <input type="text" id="name" name="name" value="<c:out value='${form.name}'/>" required maxlength="80" placeholder="AI & Data Science Lab"></div>
            <div class="row">
                <div><label for="location">Building / room</label>
                    <input type="text" id="location" name="location" value="<c:out value='${form.location}'/>" required maxlength="60" placeholder="Block A, Room 101"></div>
                <div><label for="inCharge">Lab in-charge</label>
                    <input type="text" id="inCharge" name="inCharge" value="<c:out value='${form.inCharge}'/>" required maxlength="60"></div>
            </div>
            <div class="form-actions"><button class="btn" type="submit">Save lab</button>
                <a class="btn ghost" href="${ctx}/labs">Cancel</a></div>
        </form>
    </div>
</div>
<%@ include file="_footer.jspf" %>
