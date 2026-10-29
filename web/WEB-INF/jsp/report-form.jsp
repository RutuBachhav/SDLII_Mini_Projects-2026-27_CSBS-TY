<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="nav" value="report" scope="request" />
<c:set var="pageTitle" value="${editing ? 'Edit report' : 'New report'}" scope="request" />
<%@ include file="_head.jspf" %>

<div class="page-head">
    <h1>${editing ? 'Edit report' : (item.lost ? 'Report a lost item' : 'Report a found item')}</h1>
    <p>
        <c:choose>
            <c:when test="${item.lost}">
                Describe the item as somebody else would see it. Keep the detail that proves it is
                yours for the private field at the bottom.
            </c:when>
            <c:otherwise>
                Thank you for picking it up. Write enough for the owner to recognise it, and put the
                giveaway detail in the private field so a claim can be verified.
            </c:otherwise>
        </c:choose>
    </p>
</div>

<c:if test="${not empty errors}">
    <div class="alert alert-error">
        <strong>That form could not be saved</strong>
        <ul>
            <c:forEach items="${errors}" var="e"><li><c:out value="${e}" /></li></c:forEach>
        </ul>
    </div>
</c:if>

<div class="row g-3">
    <div class="col-12 col-lg-8">
        <div class="card">
            <c:if test="${not editing}">
                <div class="type-toggle">
                    <a href="${ctx}/report?type=LOST" class="${item.lost ? 'on' : ''}">I lost this</a>
                    <a href="${ctx}/report?type=FOUND" class="${item.found ? 'on' : ''}">I found this</a>
                </div>
            </c:if>

            <%-- The CSRF token travels in the query string on this form, because the
                 body is multipart and must be left for the file upload to read. --%>
            <form method="post" enctype="multipart/form-data"
                  action="${editing ? ctx.concat('/items/edit') : ctx.concat('/report')}?csrf=${csrf}">
                <input type="hidden" name="type" value="${item.type}">
                <c:if test="${editing}"><input type="hidden" name="id" value="${item.id}"></c:if>

                <div class="field">
                    <label for="title">Title</label>
                    <input type="text" id="title" name="title" maxlength="120" required
                           value="${fn:escapeXml(item.title)}"
                           placeholder="${item.lost ? 'Brown leather wallet' : 'Leather wallet found near canteen'}">
                    <span class="hint">A short line somebody could scan in a list.</span>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label for="categoryId">Category</label>
                        <select id="categoryId" name="categoryId" required>
                            <option value="">Choose one</option>
                            <c:forEach items="${categories}" var="cat">
                                <option value="${cat.id}" ${item.categoryId eq cat.id ? 'selected' : ''}>
                                    <c:out value="${cat.name}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="color">Colour</label>
                        <input type="text" id="color" name="color" maxlength="40"
                               value="${fn:escapeXml(item.color)}" placeholder="Brown">
                        <span class="hint">One word works best; it is used for matching.</span>
                    </div>
                </div>

                <div class="field">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" required rows="5"
                              placeholder="What it looks like, what was inside, anything that helps somebody recognise it."><c:out value="${item.description}" /></textarea>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label for="location">${item.lost ? 'Last seen at' : 'Found at'}</label>
                        <input type="text" id="location" name="location" maxlength="120" required
                               value="${fn:escapeXml(item.location)}" placeholder="Central Library">
                    </div>
                    <div class="field">
                        <label for="itemDate">${item.lost ? 'Date lost' : 'Date found'}</label>
                        <input type="date" id="itemDate" name="itemDate" required value="${item.itemDate}">
                    </div>
                </div>

                <div class="field">
                    <label for="privateMarks">Identifying detail (private)</label>
                    <input type="text" id="privateMarks" name="privateMarks" maxlength="255"
                           value="${fn:escapeXml(item.privateMarks)}"
                           placeholder="A scratch, a sticker, a name written inside">
                    <span class="hint">
                        Never shown on a public page. It is how a claim gets verified, so keep it
                        out of the description above.
                    </span>
                </div>

                <div class="field">
                    <label for="photo">Photograph (optional)</label>
                    <input type="file" id="photo" name="photo" accept="image/jpeg,image/png,image/gif">
                    <span class="hint">
                        JPG, PNG or GIF, up to 5&nbsp;MB. Stored in the project's own uploads folder and
                        served through FoundIt, never as a public file.
                        <c:if test="${editing and item.hasPhoto}"> Uploading a new one replaces the current photograph.</c:if>
                    </span>
                </div>

                <div class="actions">
                    <button class="btn" type="submit">${editing ? 'Save changes' : 'Publish report'}</button>
                    <a class="btn btn-ghost" href="${editing ? ctx.concat('/items/view?id=').concat(item.id) : ctx.concat('/browse')}">Cancel</a>
                </div>
            </form>
        </div>
    </div>

    <div class="col-12 col-lg-4">
        <div class="card">
            <h2>How matching uses this</h2>
            <ul class="reasons mt-2">
                <li>Same category is worth the most, so choose it carefully.</li>
                <li>Words shared between descriptions are compared, so write plainly.</li>
                <li>Colour and place are each worth a slice of the score.</li>
                <li>Dates that are close together score higher than distant ones.</li>
            </ul>
            <p class="hint mt-3 mb-0">
                A high score only puts two reports next to each other. Somebody still has to verify
                the claim before anything is handed over.
            </p>
        </div>
    </div>
</div>

<%@ include file="_foot.jspf" %>
