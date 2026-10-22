package com.web;

import com.dao.TaskQuery;
import com.entity.DeadlineTask;
import com.entity.Priority;
import com.entity.Subject;
import com.entity.TaskStatus;
import com.entity.TaskType;
import com.service.SubjectService;
import com.service.TaskService;
import com.util.ConflictException;
import com.util.Dates;
import com.util.ValidationException;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Everything to do with tasks: the filtered list, the create and edit forms,
 * and the create / update / delete / status operations.
 *
 * Reads are GETs and writes are POSTs, and every write ends in a redirect with
 * a flash message. Refreshing after saving re-runs the redirect's GET, so no
 * operation can be repeated by pressing F5 or the back button.
 *
 * Ownership is never taken on trust: the id from the request is always passed
 * to a service method together with the session's user id, and the query that
 * loads the row has {@code user_id = ?} in it.
 */
@WebServlet(name = "TaskServlet", urlPatterns = {"/tasks", "/tasks/*"})
public class TaskServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    private final TaskService tasks = new TaskService();
    private final SubjectService subjects = new SubjectService();

    // -- GET ----------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        long userId = Web.requireUserId(req);

        switch (action(req)) {
            case "":
                list(req, res, userId);
                break;
            case "new":
                newForm(req, res, userId);
                break;
            case "edit":
                editForm(req, res, userId);
                break;
            case "delete":
                confirmDelete(req, res, userId);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * The delete confirmation page.
     *
     * Deleting is two requests: this GET shows what is about to go, and only
     * the POST removes it. That keeps the destructive step out of reach of a
     * link, and means the confirmation does not depend on JavaScript.
     */
    private void confirmDelete(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        DeadlineTask task = tasks.requireOwned(Web.requireId(req, "id"), userId);
        req.setAttribute("task", task);
        req.setAttribute("returnTo", Web.safePath(req.getParameter("returnTo"), "/tasks"));
        req.setAttribute("page", "tasks");
        render(req, res, "task-delete");
    }

    private void list(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        TaskQuery query = TaskQuery.fromRequest(req);
        List<DeadlineTask> results = tasks.search(userId, query);

        req.setAttribute("tasks", results);
        req.setAttribute("query", query);
        req.setAttribute("resultCount", results.size());
        req.setAttribute("stats", tasks.stats(userId));
        req.setAttribute("subjects", subjects.listForUser(userId));
        putFormChoices(req);
        req.setAttribute("page", "tasks");
        render(req, res, "tasks");
    }

    private void newForm(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        req.setAttribute("formTitle", "New task");
        req.setAttribute("formAction", "/tasks/create");
        req.setAttribute("dueDate", Dates.today().format(Dates.HTML_DATE));
        req.setAttribute("status", TaskStatus.PENDING.name());
        req.setAttribute("priority", Priority.MEDIUM.name());
        req.setAttribute("taskType", TaskType.ASSIGNMENT.name());
        // Lets "add a task" from a subject card pre-select that subject.
        req.setAttribute("subjectId", Web.optionalId(req, "subject"));
        showForm(req, res, userId);
    }

    private void editForm(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        DeadlineTask task = tasks.requireOwned(Web.requireId(req, "id"), userId);

        req.setAttribute("formTitle", "Edit task");
        req.setAttribute("formAction", "/tasks/update");
        req.setAttribute("taskId", task.getId());
        req.setAttribute("task", task);
        req.setAttribute("title", task.getTitle());
        req.setAttribute("description", task.getDescription());
        req.setAttribute("subjectId", task.getSubject().getId());
        req.setAttribute("taskType", task.getTaskType().name());
        req.setAttribute("priority", task.getPriority().name());
        req.setAttribute("status", task.getStatus().name());
        req.setAttribute("dueDate", task.getDueDateIso());
        req.setAttribute("dueTime", task.getDueTimeIso());
        showForm(req, res, userId);
    }

    /**
     * Renders the task form. Also used to redisplay it after a validation
     * failure, in which case the attributes already hold what the user typed.
     */
    private void showForm(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        List<Subject> owned = subjects.listForUser(userId);
        req.setAttribute("subjects", owned);
        req.setAttribute("needsSubject", owned.isEmpty());
        putFormChoices(req);
        req.setAttribute("page", "tasks");
        render(req, res, "task-form");
    }

    private void putFormChoices(HttpServletRequest req) {
        req.setAttribute("taskTypes", TaskType.values());
        req.setAttribute("priorities", Priority.values());
        req.setAttribute("statuses", TaskStatus.values());
        req.setAttribute("views", TaskQuery.View.values());
        req.setAttribute("sorts", TaskQuery.Sort.values());
    }

    // -- POST ---------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        long userId = Web.requireUserId(req);

        switch (action(req)) {
            case "create":
                create(req, res, userId);
                break;
            case "update":
                update(req, res, userId);
                break;
            case "delete":
                delete(req, res, userId);
                break;
            case "status":
                status(req, res, userId);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void create(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        try {
            DeadlineTask task = tasks.create(userId,
                    req.getParameter("title"),
                    req.getParameter("description"),
                    req.getParameter("subjectId"),
                    req.getParameter("taskType"),
                    req.getParameter("priority"),
                    req.getParameter("status"),
                    req.getParameter("dueDate"),
                    req.getParameter("dueTime"));

            Web.flashSuccess(req, "Added \"" + task.getTitle() + "\".");
            redirect(req, res, Web.safePath(req.getParameter("returnTo"), "/tasks"));
        } catch (ValidationException e) {
            keepSubmitted(req);
            req.setAttribute("formTitle", "New task");
            req.setAttribute("formAction", "/tasks/create");
            showErrors(req, e.getFieldErrors(), e.getMessage());
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            showForm(req, res, userId);
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse res, long userId)
            throws ServletException, IOException {

        long id = Web.requireId(req, "id");
        try {
            DeadlineTask task = tasks.update(id, userId,
                    req.getParameter("title"),
                    req.getParameter("description"),
                    req.getParameter("subjectId"),
                    req.getParameter("taskType"),
                    req.getParameter("priority"),
                    req.getParameter("status"),
                    req.getParameter("dueDate"),
                    req.getParameter("dueTime"));

            Web.flashSuccess(req, "Saved \"" + task.getTitle() + "\".");
            redirect(req, res, Web.safePath(req.getParameter("returnTo"), "/tasks"));
        } catch (ValidationException e) {
            keepSubmitted(req);
            req.setAttribute("formTitle", "Edit task");
            req.setAttribute("formAction", "/tasks/update");
            req.setAttribute("taskId", id);
            showErrors(req, e.getFieldErrors(), e.getMessage());
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            showForm(req, res, userId);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse res, long userId)
            throws IOException {

        long id = Web.requireId(req, "id");
        DeadlineTask task = tasks.requireOwned(id, userId);
        String title = task.getTitle();
        tasks.delete(id, userId);
        Web.flashSuccess(req, "Deleted \"" + title + "\".");
        redirect(req, res, Web.safePath(req.getParameter("returnTo"), "/tasks"));
    }

    /**
     * Completes, reopens or starts a task.
     *
     * A conflict - the task was already in that state, or somebody changed it
     * in another tab - is reported as a message on the page the student came
     * from rather than an error page, because the page they are looking at is
     * simply out of date and reloading it fixes everything.
     */
    private void status(HttpServletRequest req, HttpServletResponse res, long userId)
            throws IOException {

        long id = Web.requireId(req, "id");
        String back = Web.safePath(req.getParameter("returnTo"), "/tasks");
        TaskStatus target = TaskStatus.parse(req.getParameter("target"));
        if (target == null) {
            Web.flashError(req, "That is not a status a task can be in.");
            redirect(req, res, back);
            return;
        }

        try {
            DeadlineTask task = tasks.changeStatus(id, userId, target);
            switch (target) {
                case COMPLETED:
                    Web.flashSuccess(req, "Completed \"" + task.getTitle() + "\".");
                    break;
                case PENDING:
                    Web.flashSuccess(req, "Reopened \"" + task.getTitle() + "\".");
                    break;
                default:
                    Web.flashSuccess(req, "Started \"" + task.getTitle() + "\".");
            }
        } catch (ConflictException e) {
            // Not an error page: the student's view is just stale.
            Web.flashError(req, e.getMessage());
        }
        redirect(req, res, back);
    }

    /** Keeps what the user typed so a rejected form comes back filled in. */
    private void keepSubmitted(HttpServletRequest req) {
        for (String field : new String[]{"title", "description",
            "taskType", "priority", "status", "dueDate", "dueTime"}) {
            req.setAttribute(field, Web.strOrEmpty(req, field));
        }
        // Not strOrEmpty: the view compares this with a Long, so anything that
        // is not a positive number must arrive as null rather than as text.
        req.setAttribute("subjectId", Web.optionalId(req, "subjectId"));
    }
}
