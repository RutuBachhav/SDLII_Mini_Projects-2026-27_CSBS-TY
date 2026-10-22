package com.dao;

import com.entity.Priority;
import com.entity.TaskStatus;
import com.entity.TaskType;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * The state of the task list's search box, filters and sort order.
 *
 * Built straight from the query string, so every filter is bookmarkable and the
 * whole list page works with JavaScript disabled - the form is a GET. Unknown
 * or malformed values fall back to "no filter" rather than failing, so a
 * hand-edited URL cannot produce an error page.
 *
 * The same object is put back on the request so the JSP can re-select each
 * dropdown and build the "clear filters" link.
 */
public class TaskQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The deadline bucket the list is restricted to. */
    public enum View {

        ALL("All tasks"),
        OVERDUE("Overdue"),
        TODAY("Due today"),
        UPCOMING("Upcoming"),
        COMPLETED("Completed");

        private final String label;

        View(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        static View parse(String raw) {
            if (raw != null) {
                for (View v : values()) {
                    if (v.name().equalsIgnoreCase(raw.trim())) {
                        return v;
                    }
                }
            }
            return ALL;
        }
    }

    /** Sort order. */
    public enum Sort {

        DUE_DATE("Due date"),
        PRIORITY("Priority"),
        RECENT("Recently added");

        private final String label;

        Sort(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        static Sort parse(String raw) {
            if (raw != null) {
                for (Sort s : values()) {
                    if (s.name().equalsIgnoreCase(raw.trim())) {
                        return s;
                    }
                }
            }
            return DUE_DATE;
        }
    }

    private String keyword;
    private Long subjectId;
    private TaskType type;
    private Priority priority;
    private TaskStatus status;
    private View view = View.ALL;
    private Sort sort = Sort.DUE_DATE;

    public static TaskQuery fromRequest(HttpServletRequest req) {
        TaskQuery q = new TaskQuery();
        q.keyword = trimToNull(req.getParameter("q"));
        q.subjectId = parseId(req.getParameter("subject"));
        q.type = TaskType.parse(req.getParameter("type"));
        q.priority = Priority.parse(req.getParameter("priority"));
        q.status = TaskStatus.parse(req.getParameter("status"));
        q.view = View.parse(req.getParameter("view"));
        q.sort = Sort.parse(req.getParameter("sort"));
        return q;
    }

    /** True when anything is narrowing the list, so the UI can offer a reset. */
    public boolean isFiltered() {
        return keyword != null || subjectId != null || type != null
                || priority != null || status != null || view != View.ALL;
    }

    public String getKeyword() {
        return keyword;
    }

    /** "" rather than null, for the value attribute of the search box. */
    public String getKeywordOrEmpty() {
        return keyword == null ? "" : keyword;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public TaskType getType() {
        return type;
    }

    public Priority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public View getView() {
        return view;
    }

    public Sort getSort() {
        return sort;
    }

    public void setView(View view) {
        this.view = view == null ? View.ALL : view;
    }

    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim();
        return v.isEmpty() ? null : v;
    }

    private static Long parseId(String raw) {
        String v = trimToNull(raw);
        if (v == null) {
            return null;
        }
        try {
            long id = Long.parseLong(v);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
