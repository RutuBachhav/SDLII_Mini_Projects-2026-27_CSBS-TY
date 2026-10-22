<%--
    Add or edit a subject.

    The colour is picked from a fixed palette of radio buttons rather than a
    free text field, which is why the server can insist on #rrggbb and the
    CHECK constraint in the schema can too. app.js only updates the preview
    dot; the radios work without it.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${formTitle}"/>
<%@ include file="fragments/head.jspf" %>

<div class="page-head">
    <div>
        <div class="eyebrow">Subjects</div>
        <h1>${formTitle}</h1>
    </div>
    <a class="btn btn-quiet" href="<c:url value='/subjects'/>">Back to subjects</a>
</div>

<div class="panel" style="max-width: 560px">
    <div class="panel-body">
        <form method="post" action="<c:url value='${formAction}'/>" novalidate>
            <input type="hidden" name="csrf" value="${csrfToken}">
            <c:if test="${not empty subjectId}">
                <input type="hidden" name="id" value="${subjectId}">
            </c:if>

            <div class="mb-3">
                <label class="form-label" for="name">Subject name</label>
                <div class="d-flex align-items-center gap-2">
                    <span class="bar" data-swatch-preview
                          style="width: 10px; height: 34px; border-radius: 99px; flex: none;
                                 background: ${color}"></span>
                    <input class="form-control ${not empty errors.name ? 'is-invalid' : ''}"
                           type="text" id="name" name="name" maxlength="80"
                           value="<c:out value='${name}'/>"
                           placeholder="Operating Systems" autofocus required>
                </div>
                <c:choose>
                    <c:when test="${not empty errors.name}">
                        <div class="field-error"><c:out value="${errors.name}"/></div>
                    </c:when>
                    <c:otherwise>
                        <div class="hint">Each of your subjects needs a different name.</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <fieldset class="mb-4">
                <legend class="form-label">Colour</legend>
                <div class="d-flex flex-wrap gap-3">
                    <c:forEach var="swatch" items="${palette}" varStatus="loop">
                        <label class="d-inline-flex align-items-center gap-2"
                               style="cursor: pointer">
                            <input type="radio" class="form-check-input mt-0" name="color"
                                   value="${swatch}" data-swatch
                                   id="color-${loop.index}"
                                   ${color eq swatch ? 'checked' : ''}>
                            <span style="display: inline-block; width: 18px; height: 18px;
                                         border-radius: 4px; background: ${swatch};
                                         border: 1px solid rgba(0,0,0,.12)"></span>
                        </label>
                    </c:forEach>
                </div>
                <c:if test="${not empty errors.color}">
                    <div class="field-error"><c:out value="${errors.color}"/></div>
                </c:if>
                <div class="hint">Used for the dot next to this subject's tasks.</div>
            </fieldset>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-accent">
                    ${empty subjectId ? 'Add subject' : 'Save changes'}
                </button>
                <a class="btn btn-quiet" href="<c:url value='/subjects'/>">Cancel</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="fragments/foot.jspf" %>
