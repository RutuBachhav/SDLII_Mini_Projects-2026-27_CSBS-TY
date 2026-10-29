<%@ include file="_taglibs.jspf" %>
<c:set var="isNew" value="${empty form.id}" />
<c:set var="pageTitle" value="${isNew ? 'Add equipment' : 'Edit equipment'}" /><c:set var="nav" value="equipment" />
<%@ include file="_header.jspf" %>

<div class="page-head">
    <div><div class="crumb"><a href="${ctx}/equipment">Equipment</a></div>
        <h1>${isNew ? 'Add equipment' : ('Edit ' += form.assetTag)}</h1>
        <p>Fields are checked by Bean Validation annotations on the Equipment entity.</p></div>
</div>

<div class="card" style="max-width:820px">
    <div class="card-b">
        <form class="form" method="post" action="${ctx}/equipment/save">
            <c:if test="${not isNew}"><input type="hidden" name="id" value="${form.id}"></c:if>
            <div class="row">
                <div><label for="assetTag">Asset tag</label>
                    <input type="text" id="assetTag" name="assetTag" value="<c:out value='${form.assetTag}'/>" placeholder="LL-ELC-0042"
                           ${isNew ? 'required' : 'readonly'} pattern="LL-[A-Za-z]{3}-[0-9]{4}">
                    <div class="hint">Printed on the sticker. Format LL-XXX-0000. Cannot change later.</div></div>
                <div><label for="labId">Lab</label>
                    <select id="labId" name="labId" required>
                        <option value="">Choose a lab…</option>
                        <c:forEach var="l" items="${labs}"><option value="${l.id}" ${l.id == labId ? 'selected' : ''}>${l.code} — <c:out value="${l.name}" /></option></c:forEach>
                    </select></div>
            </div>
            <div class="row">
                <div><label for="name">Name</label>
                    <input type="text" id="name" name="name" value="<c:out value='${form.name}'/>" required maxlength="80" placeholder="Digital Storage Oscilloscope"></div>
                <div><label for="model">Make / model</label>
                    <input type="text" id="model" name="model" value="<c:out value='${form.model}'/>" maxlength="60" placeholder="Rigol DS1054Z"></div>
            </div>
            <div class="row3">
                <div><label for="category">Category</label>
                    <select id="category" name="category" required>
                        <c:forEach var="c" items="${categories}"><option value="${c}" ${c == form.category ? 'selected' : ''}>${c.label}</option></c:forEach>
                    </select></div>
                <div><label for="purchaseDate">Purchase date</label>
                    <input type="date" id="purchaseDate" name="purchaseDate" value="${form.purchaseDate}"></div>
                <div><label for="purchaseCost">Purchase cost (₹)</label>
                    <input type="number" id="purchaseCost" name="purchaseCost" min="1" step="0.01" value="${form.purchaseCost > 0 ? form.purchaseCost : ''}" required></div>
            </div>
            <div class="row">
                <div><label for="serviceIntervalDays">Service / calibration every (days)</label>
                    <input type="number" id="serviceIntervalDays" name="serviceIntervalDays" min="7" max="730" value="${form.serviceIntervalDays}"></div>
                <div><label for="lastServicedOn">Last serviced on</label>
                    <input type="date" id="lastServicedOn" name="lastServicedOn" value="${form.lastServicedOn}">
                    <div class="hint">Leave empty for new items — the purchase date is used.</div></div>
            </div>
            <div><label for="notes">Notes</label>
                <textarea id="notes" name="notes" maxlength="250" placeholder="Accessories, handling instructions…"><c:out value="${form.notes}" /></textarea></div>
            <div class="form-actions">
                <button class="btn" type="submit">${isNew ? 'Add equipment' : 'Save changes'}</button>
                <a class="btn ghost" href="${ctx}${isNew ? '/equipment' : ('/equipment/view?id=' += form.id)}">Cancel</a>
            </div>
        </form>
    </div>
</div>
<%@ include file="_footer.jspf" %>
