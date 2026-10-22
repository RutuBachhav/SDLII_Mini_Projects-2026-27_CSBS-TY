package com.web;

import com.entity.Subject;
import com.entity.DeadlineTask;
import com.service.DashboardStats;
import com.service.SubjectService;
import com.service.TaskService;
import com.util.Dates;
import com.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * The dashboard.
 *
 * Read-only: it runs four queries - the counts, the overdue list, the upcoming
 * list and the subject cards - and hands them to the view. Because every figure
 * is read on each request, the dashboard is correct straight after a task is
 * added, completed, reopened or deleted, with nothing to invalidate.
 *
 * The quick-add form on this page posts to /tasks/create like the full form, so
 * there is one code path for creating a task.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    /** How many rows the two deadline panels show before linking to the list. */
    private static final int PANEL_SIZE = 6;

    private final TaskService tasks = new TaskService();
    private final SubjectService subjects = new SubjectService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        long userId = Web.requireUserId(req);

        DashboardStats stats = tasks.stats(userId);
        List<DeadlineTask> overdue = tasks.overdue(userId, PANEL_SIZE);
        List<DeadlineTask> upcoming = tasks.upcoming(userId, PANEL_SIZE);
        List<Subject> subjectCards = subjects.listWithProgress(userId);

        req.setAttribute("stats", stats);
        req.setAttribute("overdueTasks", overdue);
        req.setAttribute("upcomingTasks", upcoming);
        req.setAttribute("subjects", subjectCards);
        req.setAttribute("today", Dates.today());
        req.setAttribute("todayIso", Dates.today().format(Dates.HTML_DATE));
        req.setAttribute("page", "dashboard");

        render(req, res, "dashboard");
    }
}
