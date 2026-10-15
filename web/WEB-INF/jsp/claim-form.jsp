<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="mine" scope="request" />
<c:set var="pageTitle" value="Submit a claim" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <h1>Claim &ldquo;<c:out value="${item.title}" />&rdquo;</h1>
    <p>
        The person holding the item reads this and decides. Write the detail that proves it is
        yours, not the detail that is already on the public page.
    </p>
</div>

<c:if test="${not empty errors}">
    <div class="alert alert-error">
        <strong>That claim was not submitted</strong>
        <ul>
            <c:forEach items="${errors}" var="e"><li><c:out value="${e}" /></li></c:forEach>
        </ul>
    </div>
</c:if>

<div class="row g-3">
    <div class="col-12 col-lg-7">
        <div class="card">
            <form method="post" action="${ctx}/claims/new">
                <input type="hidden" name="csrf" value="${csrf}">
                <input type="hidden" name="itemId" value="${item.id}">

                <div class="field">
                    <label for="evidence">How do you know it is yours?</label>
                    <textarea id="evidence" name="evidence" rows="6" required minlength="25"
                              maxlength="2000"
                              placeholder="What is inside it, a mark or a scratch, a name written somewhere, where exactly you lost it."><c:out value="${evidence}" /></textarea>
                    <span class="hint">
                        Private. Only you, the finder and the campus administrator can read this.
                    </span>
                </div>

                <div class="field">
                    <label for="contact">How can the finder reach you?</label>
                    <input type="text" id="contact" name="contact" maxlength="120" required
                           value="${fn:escapeXml(contact)}" placeholder="Phone number or email">
                    <span class="hint">Shown only to the finder, after you submit.</span>
                </div>

                <div class="actions">
                    <button class="btn" type="submit">Submit claim</button>
                    <a class="btn btn-ghost" href="${ctx}/items/view?id=${item.id}">Back to the report</a>
                </div>
            </form>
        </div>
    </div>

    <div class="col-12 col-lg-5">
        <div class="card">
            <h2>What happens next</h2>
            <ul class="reasons mt-2">
                <li>The finder is notified and reads your description.</li>
                <li>They approve or reject it, and you get an alert either way.</li>
                <li>If approved, you arrange the handover between yourselves.</li>
                <li>The finder records the return, which closes the case.</li>
            </ul>
            <p class="hint mt-3 mb-0">
                A match score never approves a claim on its own, and submitting a claim does not
                mark the item as returned.
            </p>
        </div>

        <div class="card">
            <h2>The item</h2>
            <dl class="facts mt-2">
                <div><dt>Found at</dt><dd><c:out value="${item.location}" /></dd></div>
                <div><dt>Date found</dt><dd class="mono">${item.itemDateLabel}</dd></div>
                <div><dt>Category</dt><dd><c:out value="${item.categoryName}" /></dd></div>
                <div><dt>Held by</dt><dd><c:out value="${item.reporterName}" /></dd></div>
            </dl>
        </div>
    </div>
</div>

<%@ include file="_foot.jspf" %>
