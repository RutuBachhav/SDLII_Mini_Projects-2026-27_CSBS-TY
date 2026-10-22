<%--
    Delete a subject, and decide what happens to its tasks.

    The foreign key from tasks is ON DELETE RESTRICT, so this choice is not
    cosmetic: without it the delete would simply be refused by the database.
    The two options are "move the tasks to another subject" and "delete them
    with it", and both are carried out in one transaction with the delete.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Delete subject"/>
<%@ include file="fragments/head.jspf" %>

<div class="page-head">
    <div>
        <div class="eyebrow">Subjects</div>
        <h1>Delete <c:out value="${subject.name}"/>?</h1>
        <p class="lede">
            <c:choose>
                <c:when test="${subject.taskCount eq 0}">
                    This subject has no tasks, so nothing else is affected.
                </c:when>
                <c:otherwise>
                    This subject has ${subject.taskCount}
                    ${subject.taskCount eq 1 ? 'task' : 'tasks'}. Choose what happens to
                    ${subject.taskCount eq 1 ? 'it' : 'them'}.
                </c:otherwise>
            </c:choose>
        </p>
    </div>
</div>

<div class="panel" style="max-width: 620px">
    <div class="panel-body">
        <form method="post" action="<c:url value='/subjects/delete'/>"
              data-confirm="Delete this subject? This cannot be undone.">
            <input type="hidden" name="csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${subject.id}">

            <c:if test="${subject.taskCount gt 0}">
                <fieldset class="mb-4">
                    <legend class="form-label mb-2">The ${subject.taskCount}
                        ${subject.taskCount eq 1 ? 'task' : 'tasks'} should be</legend>

                    <c:choose>
                        <c:when test="${not empty otherSubjects}">
                            <div class="form-check mb-2">
                                <input class="form-check-input" type="radio" name="policy"
                                       id="policy-move" value="move" checked>
                                <label class="form-check-label" for="policy-move">
                                    moved to another subject
                                </label>
                            </div>
                            <div class="mb-3 ms-4" style="max-width: 320px">
                                <label class="form-label" for="moveTo">Move to</label>
                                <select class="form-select form-select-sm ${not empty errors.moveTo ? 'is-invalid' : ''}"
                                        id="moveTo" name="moveTo">
                                    <c:forEach var="s" items="${otherSubjects}">
                                        <option value="${s.id}"><c:out value="${s.name}"/></option>
                                    </c:forEach>
                                </select>
                                <c:if test="${not empty errors.moveTo}">
                                    <div class="field-error"><c:out value="${errors.moveTo}"/></div>
                                </c:if>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-info py-2">
                                This is your only subject, so there is nowhere to move the
                                tasks to. Either delete them along with it, or
                                <a href="<c:url value='/subjects/new'/>">add another subject</a>
                                first.
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="policy"
                               id="policy-delete" value="delete"
                               ${empty otherSubjects ? 'checked' : ''}>
                        <label class="form-check-label" for="policy-delete">
                            deleted along with the subject
                        </label>
                    </div>
                </fieldset>
            </c:if>

            <c:if test="${subject.taskCount eq 0}">
                <input type="hidden" name="policy" value="delete">
            </c:if>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-accent"
                        style="background: var(--danger); border-color: var(--danger)">
                    Delete subject
                </button>
                <a class="btn btn-quiet" href="<c:url value='/subjects'/>">Keep it</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="fragments/foot.jspf" %>
