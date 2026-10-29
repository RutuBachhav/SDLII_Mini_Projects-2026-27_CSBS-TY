<%--
    Dashboard.

    Directives first, then the shared header - see head.jspf for why the
    fragment carries none of its own.

    Every number on this page comes from ${stats}, which DashboardServlet read
    from the database on this request. The six tiles link into the task list
    filtered to the same set, so a count and the list behind it can never
    disagree.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Dashboard"/>
<%@ include file="fragments/head.jspf" %>

<div class="page-head">
    <div>
        <div class="eyebrow">Overview</div>
        <h1>Hello, <c:out value="${sessionScope.userName}"/></h1>
        <p class="lede">
            <c:choose>
                <c:when test="${stats.overdue gt 0}">
                    ${stats.overdue} ${stats.overdue eq 1 ? 'task is' : 'tasks are'} overdue.
                    Clear those first.
                </c:when>
                <c:when test="${stats.dueToday gt 0}">
                    ${stats.dueToday} ${stats.dueToday eq 1 ? 'task is' : 'tasks are'} due today.
                </c:when>
                <c:when test="${stats.total eq 0}">
                    Nothing tracked yet. Add a subject, then add your first deadline.
                </c:when>
                <c:otherwise>Nothing overdue and nothing due today.</c:otherwise>
            </c:choose>
        </p>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-quiet" href="<c:url value='/subjects'/>">Manage subjects</a>
        <a class="btn btn-accent" href="<c:url value='/tasks/new'/>">New task</a>
    </div>
</div>

<%-- ---- the six counts ------------------------------------------------- --%>
<section class="stats" aria-label="Task counts">
    <a class="stat" href="<c:url value='/tasks'/>">
        <div class="label">Total tasks</div>
        <div class="value">${stats.total}</div>
    </a>
    <a class="stat is-good" href="<c:url value='/tasks?view=COMPLETED'/>">
        <div class="label">Completed</div>
        <div class="value">${stats.completed}</div>
    </a>
    <a class="stat" href="<c:url value='/tasks?status=PENDING'/>">
        <div class="label">Pending</div>
        <div class="value">${stats.pending}</div>
    </a>
    <a class="stat" href="<c:url value='/tasks?status=IN_PROGRESS'/>">
        <div class="label">In progress</div>
        <div class="value">${stats.inProgress}</div>
    </a>
    <a class="stat is-danger" href="<c:url value='/tasks?view=OVERDUE'/>">
        <div class="label">Overdue</div>
        <div class="value">${stats.overdue}</div>
    </a>
    <a class="stat is-warn" href="<c:url value='/tasks?view=TODAY'/>">
        <div class="label">Due today</div>
        <div class="value">${stats.dueToday}</div>
    </a>
</section>

<div class="row g-3 mt-3">

    <%-- ---- progress + quick add ---------------------------------------- --%>
    <div class="col-lg-4">
        <div class="panel">
            <div class="panel-head">
                <h2>Completion</h2>
                <span class="count">${stats.completed} of ${stats.total}</span>
            </div>
            <div class="panel-body">
                <div class="progress-line">
                    <span class="pct">${stats.completionPercent}%</span>
                    <span class="text-muted-2">${stats.open} still open</span>
                </div>
                <div class="meter ${stats.completionPercent eq 100 and stats.total gt 0 ? 'is-good' : ''}"
                     role="progressbar" aria-valuenow="${stats.completionPercent}"
                     aria-valuemin="0" aria-valuemax="100"
                     aria-label="Overall completion">
                    <span style="width: ${stats.completionPercent}%"></span>
                </div>
                <c:if test="${stats.total eq 0}">
                    <p class="hint mb-0 mt-2">
                        The percentage stays at 0% until there is something to count.
                    </p>
                </c:if>
            </div>
        </div>

        <div class="panel">
            <div class="panel-head">
                <h2>Quick add</h2>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty subjects}">
                        <p class="text-muted-2 mb-2" style="font-size: .875rem">
                            A task needs a subject. Add one first.
                        </p>
                        <a class="btn btn-accent btn-sm" href="<c:url value='/subjects/new'/>">
                            Add a subject
                        </a>
                    </c:when>
                    <c:otherwise>
                        <%-- Posts to the same endpoint as the full form, with
                             the defaults the full form would have used. --%>
                        <form method="post" action="<c:url value='/tasks/create'/>">
                            <input type="hidden" name="csrf" value="${csrfToken}">
                            <input type="hidden" name="returnTo" value="/dashboard">
                            <input type="hidden" name="status" value="PENDING">
                            <input type="hidden" name="priority" value="MEDIUM">
                            <input type="hidden" name="taskType" value="ASSIGNMENT">

                            <div class="mb-2">
                                <label class="form-label" for="qa-title">Title</label>
                                <input class="form-control form-control-sm" type="text"
                                       id="qa-title" name="title" maxlength="150"
                                       placeholder="DBMS assignment 3" required>
                            </div>
                            <div class="mb-2">
                                <label class="form-label" for="qa-subject">Subject</label>
                                <select class="form-select form-select-sm" id="qa-subject"
                                        name="subjectId" required>
                                    <c:forEach var="s" items="${subjects}">
                                        <option value="${s.id}"><c:out value="${s.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label" for="qa-due">Due date</label>
                                <input class="form-control form-control-sm" type="date"
                                       id="qa-due" name="dueDate" value="${todayIso}" required>
                            </div>
                            <button type="submit" class="btn btn-accent btn-sm w-100">Add task</button>
                            <div class="hint mt-2">
                                Assignment, medium priority, pending. Use
                                <a href="<c:url value='/tasks/new'/>">the full form</a> for the rest.
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <%-- ---- overdue + upcoming ----------------------------------------- --%>
    <div class="col-lg-8">

        <c:if test="${not empty overdueTasks}">
            <div class="panel">
                <div class="panel-head">
                    <h2 class="text-danger">Overdue</h2>
                    <a class="count" href="<c:url value='/tasks?view=OVERDUE'/>">See all ${stats.overdue}</a>
                </div>
                <div class="panel-body tight">
                    <ul class="task-list">
                        <c:set var="returnTo" value="/dashboard"/>
                        <c:forEach var="t" items="${overdueTasks}">
                            <%@ include file="fragments/task-row.jspf" %>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>

        <div class="panel">
            <div class="panel-head">
                <h2>Upcoming deadlines</h2>
                <a class="count" href="<c:url value='/tasks?view=UPCOMING'/>">All upcoming</a>
            </div>
            <div class="panel-body tight">
                <c:choose>
                    <c:when test="${empty upcomingTasks}">
                        <div class="empty">
                            <div class="mark">0</div>
                            <h3>No deadlines ahead</h3>
                            <c:choose>
                                <c:when test="${stats.total eq 0}">
                                    <p>Once you add tasks, the next ones due appear here,
                                       nearest first.</p>
                                </c:when>
                                <c:otherwise>
                                    <p>Nothing is due from today onwards. Everything else is
                                       either done or already overdue.</p>
                                </c:otherwise>
                            </c:choose>
                            <a class="btn btn-accent btn-sm" href="<c:url value='/tasks/new'/>">
                                New task
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <ul class="task-list">
                            <c:set var="returnTo" value="/dashboard"/>
                            <c:forEach var="t" items="${upcomingTasks}">
                                <%@ include file="fragments/task-row.jspf" %>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <%-- ---- subject progress --------------------------------------- --%>
        <div class="panel">
            <div class="panel-head">
                <h2>Subjects</h2>
                <a class="count" href="<c:url value='/subjects'/>">Manage</a>
            </div>
            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty subjects}">
                        <div class="empty" style="padding: 20px 10px">
                            <h3>No subjects yet</h3>
                            <p>Subjects group your tasks and give you per-course progress.</p>
                            <a class="btn btn-accent btn-sm" href="<c:url value='/subjects/new'/>">
                                Add a subject
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="subject-grid">
                            <c:forEach var="s" items="${subjects}">
                                <div class="subject-card">
                                    <div class="head">
                                        <span class="bar" style="background: ${s.displayColor}"></span>
                                        <div class="flex-grow-1">
                                            <div class="name"><c:out value="${s.name}"/></div>
                                            <div class="numbers">
                                                ${s.completedCount} of ${s.taskCount} done
                                            </div>
                                        </div>
                                        <span class="chip">${s.progressPercent}%</span>
                                    </div>
                                    <div class="meter thin ${s.progressPercent eq 100 and s.taskCount gt 0 ? 'is-good' : ''}">
                                        <span style="width: ${s.progressPercent}%"></span>
                                    </div>
                                    <div class="foot">
                                        <a href="<c:url value='/tasks'><c:param name='subject' value='${s.id}'/></c:url>">
                                            View tasks
                                        </a>
                                        <a href="<c:url value='/tasks/new'><c:param name='subject' value='${s.id}'/></c:url>">
                                            Add task
                                        </a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragments/foot.jspf" %>
