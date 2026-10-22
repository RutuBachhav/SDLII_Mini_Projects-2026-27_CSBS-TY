package com.dao;

import com.entity.DeadlineTask;
import com.entity.Priority;
import com.entity.TaskStatus;
import com.util.Dates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Queries against the tasks table.
 *
 * Three things to notice:
 *
 * - Every statement carries {@code t.user.id = :userId}. Ownership is part of
 *   the query, not a check the caller is trusted to have done.
 * - Searching, filtering and sorting are done in JPQL, so the database does
 *   the work and the page does not load a student's whole task list to filter
 *   it in Java.
 * - A status change is a conditional UPDATE that names the status it expects to
 *   replace. If it matches no row - the task was already completed, the form was
 *   submitted twice, two tabs raced - it returns 0 and the service reports a
 *   conflict instead of half-applying the change.
 */
public class TaskDao {

    /** Escape character for user text inside a LIKE pattern. */
    private static final char LIKE_ESCAPE = '!';

    public Optional<DeadlineTask> findOwned(EntityManager em, long id, long userId) {
        try {
            return Optional.of(em.createNamedQuery("DeadlineTask.byIdAndUser", DeadlineTask.class)
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public DeadlineTask insert(EntityManager em, DeadlineTask task) {
        em.persist(task);
        em.flush();
        return task;
    }

    public void remove(EntityManager em, DeadlineTask task) {
        em.remove(task);
        em.flush();
    }

    /**
     * The next few deadlines that are still open, nearest first. Used by the
     * dashboard's "upcoming deadlines" panel.
     *
     * {@code from} is today, so a task that is overdue is not shown here - the
     * overdue panel has it.
     */
    public List<DeadlineTask> upcoming(EntityManager em, long userId, int limit) {
        return em.createNamedQuery("DeadlineTask.upcomingByUser", DeadlineTask.class)
                .setParameter("userId", userId)
                .setParameter("from", Dates.today())
                .setMaxResults(limit)
                .getResultList();
    }

    /** Open tasks whose due date has passed, most overdue first. */
    public List<DeadlineTask> overdue(EntityManager em, long userId, int limit) {
        return em.createQuery(
                "SELECT t FROM DeadlineTask t JOIN FETCH t.subject "
                + "WHERE t.user.id = :userId AND t.status <> com.entity.TaskStatus.COMPLETED "
                + "AND t.dueDate < :today ORDER BY t.dueDate, t.id", DeadlineTask.class)
                .setParameter("userId", userId)
                .setParameter("today", Dates.today())
                .setMaxResults(limit)
                .getResultList();
    }

    // -- search / filter / sort ---------------------------------------------

    /**
     * Runs the task list query. The WHERE clause is assembled from whichever
     * filters are set; values always go in as bound parameters, never as
     * concatenated text.
     */
    public List<DeadlineTask> search(EntityManager em, long userId, TaskQuery q) {
        StringBuilder jpql = new StringBuilder(
                "SELECT t FROM DeadlineTask t JOIN FETCH t.subject WHERE t.user.id = :userId");
        List<Object[]> params = new ArrayList<>();
        appendFilters(jpql, params, q);
        jpql.append(orderBy(q.getSort()));

        TypedQuery<DeadlineTask> query = em.createQuery(jpql.toString(), DeadlineTask.class)
                .setParameter("userId", userId);
        bind(query, params);
        if (q.getSort() == TaskQuery.Sort.PRIORITY) {
            query.setParameter("pHigh", Priority.HIGH)
                 .setParameter("pMedium", Priority.MEDIUM);
        }
        return query.getResultList();
    }

    /** How many tasks the current filters match, for the result count line. */
    public long countMatching(EntityManager em, long userId, TaskQuery q) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(t) FROM DeadlineTask t WHERE t.user.id = :userId");
        List<Object[]> params = new ArrayList<>();
        appendFilters(jpql, params, q);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("userId", userId);
        bind(query, params);
        Long count = query.getSingleResult();
        return count == null ? 0 : count;
    }

    private void appendFilters(StringBuilder jpql, List<Object[]> params, TaskQuery q) {
        LocalDate today = Dates.today();

        if (q.getKeyword() != null) {
            jpql.append(" AND (LOWER(t.title) LIKE :kw ESCAPE '").append(LIKE_ESCAPE)
                .append("' OR LOWER(t.description) LIKE :kw ESCAPE '").append(LIKE_ESCAPE)
                .append("')");
            params.add(new Object[]{"kw", "%" + escapeLike(q.getKeyword().toLowerCase()) + "%"});
        }
        if (q.getSubjectId() != null) {
            jpql.append(" AND t.subject.id = :subjectId");
            params.add(new Object[]{"subjectId", q.getSubjectId()});
        }
        if (q.getType() != null) {
            jpql.append(" AND t.taskType = :type");
            params.add(new Object[]{"type", q.getType()});
        }
        if (q.getPriority() != null) {
            jpql.append(" AND t.priority = :priority");
            params.add(new Object[]{"priority", q.getPriority()});
        }
        if (q.getStatus() != null) {
            jpql.append(" AND t.status = :status");
            params.add(new Object[]{"status", q.getStatus()});
        }

        switch (q.getView()) {
            case OVERDUE:
                jpql.append(" AND t.status <> :doneStatus AND t.dueDate < :today");
                params.add(new Object[]{"doneStatus", TaskStatus.COMPLETED});
                params.add(new Object[]{"today", today});
                break;
            case TODAY:
                jpql.append(" AND t.status <> :doneStatus AND t.dueDate = :today");
                params.add(new Object[]{"doneStatus", TaskStatus.COMPLETED});
                params.add(new Object[]{"today", today});
                break;
            case UPCOMING:
                jpql.append(" AND t.status <> :doneStatus AND t.dueDate > :today");
                params.add(new Object[]{"doneStatus", TaskStatus.COMPLETED});
                params.add(new Object[]{"today", today});
                break;
            case COMPLETED:
                jpql.append(" AND t.status = :doneStatus");
                params.add(new Object[]{"doneStatus", TaskStatus.COMPLETED});
                break;
            case ALL:
            default:
                break;
        }
    }

    private String orderBy(TaskQuery.Sort sort) {
        switch (sort) {
            case PRIORITY:
                // JPQL has no notion of the enum's own ordering, so the ranking
                // is spelled out. Bound parameters, not literals, so it cannot
                // drift from the Priority enum's names.
                return " ORDER BY CASE WHEN t.priority = :pHigh THEN 0"
                        + " WHEN t.priority = :pMedium THEN 1 ELSE 2 END,"
                        + " t.dueDate, t.id";
            case RECENT:
                return " ORDER BY t.createdAt DESC, t.id DESC";
            case DUE_DATE:
            default:
                return " ORDER BY t.dueDate, t.dueTime, t.id";
        }
    }

    private void bind(TypedQuery<?> query, List<Object[]> params) {
        for (Object[] p : params) {
            query.setParameter((String) p[0], p[1]);
        }
    }

    /** Stops a % or _ typed into the search box behaving as a wildcard. */
    private static String escapeLike(String raw) {
        StringBuilder sb = new StringBuilder(raw.length() + 8);
        for (char c : raw.toCharArray()) {
            if (c == '%' || c == '_' || c == LIKE_ESCAPE) {
                sb.append(LIKE_ESCAPE);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // -- status changes -----------------------------------------------------

    /**
     * Moves a task from one status to another, but only if it is still in
     * {@code expected}. Returns the number of rows changed: 1 on success, 0 if
     * somebody got there first.
     */
    public int changeStatus(EntityManager em, long taskId, long userId,
            TaskStatus expected, TaskStatus target) {
        LocalDateTime now = LocalDateTime.now();
        // completed_at is set when a task is completed and cleared when it is
        // reopened. The NULL case is written as a literal rather than a bound
        // parameter: an untyped null parameter in a bulk update is ambiguous.
        String setCompletedAt = target == TaskStatus.COMPLETED
                ? "t.completedAt = :now"
                : "t.completedAt = NULL";
        int rows = em.createQuery(
                "UPDATE DeadlineTask t SET t.status = :target, t.updatedAt = :now, "
                + setCompletedAt
                + " WHERE t.id = :id AND t.user.id = :userId AND t.status = :expected")
                .setParameter("target", target)
                .setParameter("now", now)
                .setParameter("id", taskId)
                .setParameter("userId", userId)
                .setParameter("expected", expected)
                .executeUpdate();
        if (rows > 0) {
            // A bulk update bypasses the persistence context; drop any copy of
            // this task so the next read does not serve the stale status.
            em.clear();
        }
        return rows;
    }

    // -- dashboard counts ---------------------------------------------------

    /**
     * Every dashboard number in one pass over the user's tasks.
     *
     * Order of the returned array: total, completed, in progress, pending,
     * overdue, due today.
     */
    public long[] counts(EntityManager em, long userId) {
        Object[] row = em.createQuery(
                "SELECT COUNT(t), "
                + " SUM(CASE WHEN t.status = :done THEN 1 ELSE 0 END), "
                + " SUM(CASE WHEN t.status = :progress THEN 1 ELSE 0 END), "
                + " SUM(CASE WHEN t.status = :pending THEN 1 ELSE 0 END), "
                + " SUM(CASE WHEN t.status <> :done AND t.dueDate < :today THEN 1 ELSE 0 END), "
                + " SUM(CASE WHEN t.status <> :done AND t.dueDate = :today THEN 1 ELSE 0 END) "
                + "FROM DeadlineTask t WHERE t.user.id = :userId", Object[].class)
                .setParameter("userId", userId)
                .setParameter("done", TaskStatus.COMPLETED)
                .setParameter("progress", TaskStatus.IN_PROGRESS)
                .setParameter("pending", TaskStatus.PENDING)
                .setParameter("today", Dates.today())
                .getSingleResult();

        long[] counts = new long[6];
        for (int i = 0; i < counts.length; i++) {
            counts[i] = row[i] == null ? 0L : ((Number) row[i]).longValue();
        }
        return counts;
    }
}
