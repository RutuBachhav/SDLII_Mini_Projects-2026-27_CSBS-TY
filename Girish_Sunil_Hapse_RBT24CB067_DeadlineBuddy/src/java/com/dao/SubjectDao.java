package com.dao;

import com.entity.Subject;
import com.entity.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Queries against the subjects table.
 *
 * Every lookup is scoped by {@code user_id}. There is deliberately no
 * {@code findById(id)} that ignores the owner, so no servlet can accidentally
 * load somebody else's subject by trusting a request parameter.
 */
public class SubjectDao {

    public List<Subject> listByUser(EntityManager em, long userId) {
        return em.createNamedQuery("Subject.byUser", Subject.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * The same list, with each subject's task count and completed count filled
     * in. One grouped query for all subjects rather than two per card.
     *
     * A LEFT JOIN keeps subjects that have no tasks yet; COUNT over the join
     * would report 1 for them, so the totals are built from a SUM over CASE.
     */
    public List<Subject> listWithCounts(EntityManager em, long userId) {
        List<Object[]> rows = em.createQuery(
                "SELECT s, "
                + "  SUM(CASE WHEN t.id IS NULL THEN 0 ELSE 1 END), "
                + "  SUM(CASE WHEN t.status = :done THEN 1 ELSE 0 END) "
                + "FROM Subject s LEFT JOIN DeadlineTask t ON t.subject = s "
                + "WHERE s.user.id = :userId "
                + "GROUP BY s ORDER BY s.name", Object[].class)
                .setParameter("userId", userId)
                .setParameter("done", TaskStatus.COMPLETED)
                .getResultList();

        List<Subject> subjects = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            Subject subject = (Subject) row[0];
            subject.setTaskCount(asLong(row[1]));
            subject.setCompletedCount(asLong(row[2]));
            subjects.add(subject);
        }
        return subjects;
    }

    /** Loads a subject only if it belongs to this user. */
    public Optional<Subject> findOwned(EntityManager em, long id, long userId) {
        try {
            return Optional.of(em.createNamedQuery("Subject.byIdAndUser", Subject.class)
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * True when this user already has a subject with that name.
     * {@code excludeId} is the subject being edited, so renaming a subject to
     * its own current name is not reported as a duplicate.
     */
    public boolean nameTaken(EntityManager em, long userId, String name, Long excludeId) {
        if (name == null) {
            return false;
        }
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(s) FROM Subject s WHERE s.user.id = :userId AND LOWER(s.name) = :name");
        if (excludeId != null) {
            jpql.append(" AND s.id <> :excludeId");
        }
        var query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("userId", userId)
                .setParameter("name", name.trim().toLowerCase());
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    public long countTasks(EntityManager em, long subjectId) {
        Long count = em.createNamedQuery("DeadlineTask.countBySubject", Long.class)
                .setParameter("subjectId", subjectId)
                .getSingleResult();
        return count == null ? 0 : count;
    }

    public Subject insert(EntityManager em, Subject subject) {
        em.persist(subject);
        em.flush();
        return subject;
    }

    public void remove(EntityManager em, Subject subject) {
        em.remove(subject);
        em.flush();
    }

    /**
     * Moves every task of one subject to another, in one statement.
     *
     * Both subject ids are checked against the owner first by the service, and
     * the user_id predicate here is a second lock: a bulk update cannot escape
     * the current user's rows even if it were called with a foreign id.
     * Returns the number of tasks moved.
     */
    public int reassignTasks(EntityManager em, long fromSubjectId, Subject target, long userId) {
        return em.createQuery(
                "UPDATE DeadlineTask t SET t.subject = :target, t.updatedAt = :now "
                + "WHERE t.subject.id = :from AND t.user.id = :userId")
                .setParameter("target", target)
                .setParameter("now", java.time.LocalDateTime.now())
                .setParameter("from", fromSubjectId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    /** Deletes every task of one subject. Returns how many were deleted. */
    public int deleteTasks(EntityManager em, long subjectId, long userId) {
        return em.createQuery(
                "DELETE FROM DeadlineTask t WHERE t.subject.id = :subjectId AND t.user.id = :userId")
                .setParameter("subjectId", subjectId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    private static long asLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}
