<%--
    The task list: deadline tabs, search, four filters, sort, reset.

    The whole bar is one GET form. That makes every combination a plain URL -
    bookmarkable, shareable, and working with JavaScript switched off. app.js
    only adds submit-on-change so the Apply button is optional.

    ${query} is the TaskQuery the servlet built from the request, so each
    control re-selects what is actually in force rather than guessing.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Tasks"/>
<%@ include file="fragments/head.jspf" %>

<div class="page-head">
    <div>
        <div class="eyebrow">Tasks</div>
        <h1>${query.view.label}</h1>
        <p class="lede">
            ${resultCount} ${resultCount eq 1 ? 'task' : 'tasks'}
            <c:if test="${query.filtered}">matching the current filters</c:if>
            <c:if test="${not query.filtered}">in total</c:if>
        </p>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-quiet" href="<c:url value='/dashboard'/>">Dashboard</a>
        <a class="btn btn-accent" href="<c:url value='/tasks/new'/>">New task</a>
    </div>
</div>

<div class="panel">

    <%-- ---- deadline buckets -------------------------------------------- --%>
    <nav class="view-tabs" aria-label="Deadline view">
        <c:forEach var="v" items="${views}">
            <a class="${query.view eq v ? 'active' : ''}"
               href="<c:url value='/tasks'>
                        <c:param name='view' value='${v}'/>
                        <c:param name='q' value='${query.keywordOrEmpty}'/>
                        <c:param name='subject' value='${query.subjectId}'/>
                        <c:param name='type' value='${query.type}'/>
                        <c:param name='priority' value='${query.priority}'/>
                        <c:param name='status' value='${query.status}'/>
                        <c:param name='sort' value='${query.sort}'/>
                     </c:url>">${v.label}</a>
        </c:forEach>
    </nav>

    <%-- ---- search, filters, sort --------------------------------------- --%>
    <div class="panel-body">
        <form method="get" action="<c:url value='/tasks'/>" data-autosubmit>
            <input type="hidden" name="view" value="${query.view}">

            <div class="filters">
                <div>
                    <label class="form-label" for="f-q">Search</label>
                    <input class="form-control form-control-sm" type="search" id="f-q" name="q"
                           value="<c:out value='${query.keywordOrEmpty}'/>"
                           placeholder="Title or description">
                </div>

                <div>
                    <label class="form-label" for="f-subject">Subject</label>
                    <select class="form-select form-select-sm" id="f-subject" name="subject">
                        <option value="">All subjects</option>
                        <c:forEach var="s" items="${subjects}">
                            <option value="${s.id}" ${query.subjectId eq s.id ? 'selected' : ''}>
                                <c:out value="${s.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="f-type">Type</label>
                    <select class="form-select form-select-sm" id="f-type" name="type">
                        <option value="">All types</option>
                        <c:forEach var="t" items="${taskTypes}">
                            <option value="${t}" ${query.type eq t ? 'selected' : ''}>${t.label}</option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="f-priority">Priority</label>
                    <select class="form-select form-select-sm" id="f-priority" name="priority">
                        <option value="">Any priority</option>
                        <c:forEach var="p" items="${priorities}">
                            <option value="${p}" ${query.priority eq p ? 'selected' : ''}>${p.label}</option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label class="form-label" for="f-status">Status</label>
                    <select class="form-select form-select-sm" id="f-status" name="status">
                        <option value="">Any status</option>
                        <c:forEach var="st" items="${statuses}">
                            <option value="${st}" ${query.status eq st ? 'selected' : ''}>${st.label}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="d-flex gap-2">
                    <button type="submit" class="btn btn-accent btn-sm">Apply</button>
                </div>
            </div>

            <div class="d-flex flex-wrap align-items-end gap-3 mt-3">
                <div>
                    <label class="form-label" for="f-sort">Sort by</label>
                    <select class="form-select form-select-sm" id="f-sort" name="sort"
                            style="min-width: 170px">
                        <c:forEach var="so" items="${sorts}">
                            <option value="${so}" ${query.sort eq so ? 'selected' : ''}>${so.label}</option>
                        </c:forEach>
                    </select>
                </div>
                <c:if test="${query.filtered}">
                    <a class="btn btn-quiet btn-sm" href="<c:url value='/tasks'/>">Reset filters</a>
                </c:if>
            </div>
        </form>
    </div>

    <%-- ---- results ----------------------------------------------------- --%>
    <c:choose>
        <c:when test="${empty tasks}">
            <div class="empty">
                <div class="mark">0</div>
                <c:choose>
                    <c:when test="${query.filtered}">
                        <h3>Nothing matches those filters</h3>
                        <p>
                            No task
                            <c:if test="${not empty query.keyword}">
                                matches &ldquo;<c:out value="${query.keyword}"/>&rdquo;
                            </c:if>
                            with the filters you have set. Try widening them.
                        </p>
                        <a class="btn btn-quiet btn-sm" href="<c:url value='/tasks'/>">Reset filters</a>
                    </c:when>
                    <c:when test="${stats.total eq 0}">
                        <h3>No tasks yet</h3>
                        <p>Add your assignments, practicals, submissions and exams and they
                           will appear here with their deadlines.</p>
                        <a class="btn btn-accent btn-sm" href="<c:url value='/tasks/new'/>">
                            Add your first task
                        </a>
                    </c:when>
                    <c:otherwise>
                        <h3>Nothing in this view</h3>
                        <p>You have ${stats.total} tasks, but none of them are
                           ${fn:toLowerCase(query.view.label)}.</p>
                        <a class="btn btn-quiet btn-sm" href="<c:url value='/tasks'/>">See all tasks</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:when>
        <c:otherwise>
            <div class="panel-body tight">
                <ul class="task-list">
                    <%-- Keep the whole query string so the row's buttons come
                         back to the same filtered page. --%>
                    <c:set var="returnTo">/tasks?${pageContext.request.queryString}</c:set>
                    <c:forEach var="t" items="${tasks}">
                        <%@ include file="fragments/task-row.jspf" %>
                    </c:forEach>
                </ul>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="fragments/foot.jspf" %>
