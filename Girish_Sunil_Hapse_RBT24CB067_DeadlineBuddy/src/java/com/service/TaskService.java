package com.service;

import com.dao.SubjectDao;
import com.dao.TaskDao;
import com.dao.TaskQuery;
import com.dao.UserDao;
import com.entity.DeadlineTask;
import com.entity.Priority;
import com.entity.Subject;
import com.entity.TaskStatus;
import com.entity.TaskType;
import com.entity.User;
import com.util.AppException;
import com.util.ConflictException;
import com.util.Dates;
import com.util.Jpa;
import com.util.NotFoundException;
import com.util.ValidationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Task CRUD, the deadline queries, and the status machine.
 *
 * The create and update methods take the raw form values as strings and do all
 * the parsing and validation here, so there is exactly one place where a task's
 * rules are defined and the servlets stay thin.
 *
 * Two rules are worth singling out:
 *
 * - A task's subject is loaded with {@code findOwned}, so a student cannot file
 *   a task under another student's subject by editing the hidden field. The
 *   composite foreign key in the schema enforces the same thing underneath.
 * - A status change goes through {@link #changeStatus}, which checks the
 *   transition is legal and then writes it as a conditional UPDATE. A refreshed
 *   POST or a double click reports "already done" instead of applying twice.
 */
public class TaskService {

    private static final int TITLE_MAX = 150;
    private static final int DESCRIPTION_MAX = 4000;

    /** Sanity bounds on the due date - a typo like year 0202 should not land. */
    private static final LocalDate EARLIEST_DUE = LocalDate.of(2000, 1, 1);
    private static final int LATEST_DUE_YEARS_AHEAD = 10;

    private final TaskDao tasks = new TaskDao();
    private final SubjectDao subjects = new SubjectDao();
    private final UserDao users = new UserDao();

    // -- reads --------------------------------------------------------------

    public List<DeadlineTask> search(long userId, TaskQuery query) {
        return Jpa.read(em -> tasks.search(em, userId, query));
    }

    public DeadlineTask requireOwned(long id, long userId) {
        return Jpa.read(em -> tasks.findOwned(em, id, userId))
                .orElseThrow(() -> new NotFoundException("That task does not exist."));
    }

    public List<DeadlineTask> upcoming(long userId, int limit) {
        return Jpa.read(em -> tasks.upcoming(em, userId, limit));
    }

    public List<DeadlineTask> overdue(long userId, int limit) {
        return Jpa.read(em -> tasks.overdue(em, userId, limit));
    }

    /** Every dashboard figure, read in one query. */
    public DashboardStats stats(long userId) {
        long[] c = Jpa.read(em -> tasks.counts(em, userId));
        return new DashboardStats(c[0], c[1], c[2], c[3], c[4], c[5]);
    }

    // -- writes -------------------------------------------------------------

    public DeadlineTask create(long userId, String title, String description, String subjectId,
            String type, String priority, String status, String dueDate, String dueTime) {

        Parsed in = parse(title, description, subjectId, type, priority, status, dueDate, dueTime);

        return Jpa.write(em -> {
            User owner = users.findById(em, userId)
                    .orElseThrow(() -> new AppException("Your account could not be loaded."));
            Subject subject = subjects.findOwned(em, in.subjectId, userId)
                    .orElseThrow(() -> new ValidationException("subjectId",
                            "Choose one of your subjects."));

            DeadlineTask task = new DeadlineTask();
            task.setUser(owner);
            task.setSubject(subject);
            apply(task, in);
            return tasks.insert(em, task);
        });
    }

    public DeadlineTask update(long id, long userId, String title, String description,
            String subjectId, String type, String priority, String status,
            String dueDate, String dueTime) {

        Parsed in = parse(title, description, subjectId, type, priority, status, dueDate, dueTime);

        return Jpa.write(em -> {
            DeadlineTask task = tasks.findOwned(em, id, userId)
                    .orElseThrow(() -> new NotFoundException("That task does not exist."));
            Subject subject = subjects.findOwned(em, in.subjectId, userId)
                    .orElseThrow(() -> new ValidationException("subjectId",
                            "Choose one of your subjects."));

            // The edit form offers the three statuses directly, so an edit may
            // also be a status change. It must obey the same machine.
            if (in.status != task.getStatus() && !task.getStatus().canMoveTo(in.status)) {
                throw new ValidationException("status", "A completed task has to be reopened first.");
            }
            if (in.status == TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED) {
                task.setCompletedAt(Dates.now());
            } else if (in.status != TaskStatus.COMPLETED) {
                task.setCompletedAt(null);
            }

            task.setSubject(subject);
            apply(task, in);
            em.flush();
            return task;
        });
    }

    public void delete(long id, long userId) {
        Jpa.write(em -> {
            DeadlineTask task = tasks.findOwned(em, id, userId)
                    .orElseThrow(() -> new NotFoundException("That task does not exist."));
            tasks.remove(em, task);
            return null;
        });
    }

    /**
     * Applies a status change.
     *
     * Reads the task first so the message can name a legal next step, then
     * writes a conditional UPDATE that expects the status just read. Zero rows
     * changed means something moved in between, which is a conflict and not a
     * silent no-op.
     */
    public DeadlineTask changeStatus(long id, long userId, TaskStatus target) {
        if (target == null) {
            throw new ValidationException("status", "That is not a status a task can be in.");
        }
        return Jpa.write(em -> {
            DeadlineTask task = tasks.findOwned(em, id, userId)
                    .orElseThrow(() -> new NotFoundException("That task does not exist."));
            TaskStatus current = task.getStatus();

            if (current == target) {
                throw new ConflictException("That task is already " + target.getLabel().toLowerCase() + ".");
            }
            if (!current.canMoveTo(target)) {
                throw new ConflictException("A " + current.getLabel().toLowerCase()
                        + " task cannot be moved straight to " + target.getLabel().toLowerCase() + ".");
            }

            int rows = tasks.changeStatus(em, id, userId, current, target);
            if (rows == 0) {
                throw new ConflictException("That task was changed in another tab. "
                        + "The list below is up to date.");
            }
            return tasks.findOwned(em, id, userId)
                    .orElseThrow(() -> new NotFoundException("That task does not exist."));
        });
    }

    public DeadlineTask complete(long id, long userId) {
        return changeStatus(id, userId, TaskStatus.COMPLETED);
    }

    public DeadlineTask reopen(long id, long userId) {
        return changeStatus(id, userId, TaskStatus.PENDING);
    }

    // -- validation ---------------------------------------------------------

    /** The validated, parsed form values. */
    private static final class Parsed {

        String title;
        String description;
        long subjectId;
        TaskType type;
        Priority priority;
        TaskStatus status;
        LocalDate dueDate;
        LocalTime dueTime;
    }

    /**
     * Validates everything in one pass and collects the problems, so a form
     * with three mistakes reports all three rather than one per submission.
     */
    private Parsed parse(String title, String description, String subjectId, String type,
            String priority, String status, String dueDate, String dueTime) {

        Map<String, String> errors = new LinkedHashMap<>();
        Parsed out = new Parsed();

        out.title = title == null ? "" : title.trim();
        if (out.title.isEmpty()) {
            errors.put("title", "Title is required.");
        } else if (out.title.length() > TITLE_MAX) {
            errors.put("title", "Title cannot be longer than " + TITLE_MAX + " characters.");
        }

        out.description = description == null || description.trim().isEmpty()
                ? null : description.trim();
        if (out.description != null && out.description.length() > DESCRIPTION_MAX) {
            errors.put("description",
                    "Description cannot be longer than " + DESCRIPTION_MAX + " characters.");
        }

        Long subject = parseId(subjectId);
        if (subject == null) {
            errors.put("subjectId", "Choose a subject.");
        } else {
            out.subjectId = subject;
        }

        out.type = TaskType.parse(type);
        if (out.type == null) {
            errors.put("type", "Choose a task type.");
        }

        out.priority = Priority.parse(priority);
        if (out.priority == null) {
            errors.put("priority", "Choose a priority.");
        }

        out.status = status == null || status.trim().isEmpty()
                ? TaskStatus.PENDING : TaskStatus.parse(status);
        if (out.status == null) {
            errors.put("status", "Choose a status.");
        }

        // A past due date is allowed - students do add work they are already
        // late on, and it should show up as overdue. Only nonsense is refused.
        try {
            out.dueDate = Dates.requireDate(dueDate, "dueDate", "Due date");
            if (out.dueDate.isBefore(EARLIEST_DUE)) {
                errors.put("dueDate", "Due date looks wrong - check the year.");
            } else if (out.dueDate.isAfter(Dates.today().plusYears(LATEST_DUE_YEARS_AHEAD))) {
                errors.put("dueDate", "Due date cannot be more than "
                        + LATEST_DUE_YEARS_AHEAD + " years away.");
            }
        } catch (ValidationException e) {
            errors.putAll(e.getFieldErrors());
        }

        try {
            out.dueTime = Dates.optionalTime(dueTime, "dueTime", "Due time");
        } catch (ValidationException e) {
            errors.putAll(e.getFieldErrors());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return out;
    }

    private void apply(DeadlineTask task, Parsed in) {
        task.setTitle(in.title);
        task.setDescription(in.description);
        task.setTaskType(in.type);
        task.setPriority(in.priority);
        task.setStatus(in.status);
        task.setDueDate(in.dueDate);
        task.setDueTime(in.dueTime);
        if (in.status == TaskStatus.COMPLETED && task.getCompletedAt() == null) {
            task.setCompletedAt(Dates.now());
        }
    }

    private static Long parseId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            long id = Long.parseLong(raw.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
