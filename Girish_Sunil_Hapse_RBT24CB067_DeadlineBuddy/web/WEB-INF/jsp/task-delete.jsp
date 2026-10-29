<%--
    Delete confirmation for a task.

    A separate page rather than only a JavaScript confirm(), so the destructive
    step is a POST that cannot be reached by following a link, and the
    confirmation still exists with scripting switched off.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Delete task"/>
<%@ include file="fragments/head.jspf" %>

<div class="page-head">
    <div>
        <div class="eyebrow">Tasks</div>
        <h1>Delete this task?</h1>
        <p class="lede">This cannot be undone.</p>
    </div>
</div>

<div class="panel" style="max-width: 620px">
    <div class="panel-body">

        <div class="task-row" style="border: 0; padding: 0 0 14px">
            <div class="task-main">
                <div class="task-title"><c:out value="${task.title}"/></div>
                <c:if test="${not empty task.description}">
                    <div class="task-desc"><c:out value="${task.description}"/></div>
                </c:if>
                <div class="task-meta">
                    <span class="chip">
                        <span class="swatch" style="background: ${task.subject.displayColor}"></span>
                        <c:out value="${task.subject.name}"/>
                    </span>
                    <span class="chip">${task.taskType.label}</span>
                    <span class="chip ${task.priority.cssClass}">${task.priority.label}</span>
                    <span class="chip ${task.status.cssClass}">${task.status.label}</span>
                    <span class="due ${task.overdue ? 'is-overdue' : ''}">
                        ${task.dueDateDisplay} &middot; ${task.dueLabel}
                    </span>
                </div>
            </div>
        </div>

        <div class="d-flex gap-2">
            <form class="inline-form" method="post" action="<c:url value='/tasks/delete'/>"
                  data-confirm="Delete this task permanently?">
                <input type="hidden" name="csrf" value="${csrfToken}">
                <input type="hidden" name="id" value="${task.id}">
                <input type="hidden" name="returnTo" value="<c:out value='${returnTo}'/>">
                <button type="submit" class="btn btn-accent" style="background: var(--danger); border-color: var(--danger)">
                    Delete task
                </button>
            </form>
            <a class="btn btn-quiet" href="<c:url value='${returnTo}'/>">Keep it</a>
        </div>
    </div>
</div>

<%@ include file="fragments/foot.jspf" %>
