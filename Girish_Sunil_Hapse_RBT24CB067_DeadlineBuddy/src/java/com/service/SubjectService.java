package com.service;

import com.dao.SubjectDao;
import com.dao.UserDao;
import com.entity.Subject;
import com.entity.User;
import com.util.AppException;
import com.util.Jpa;
import com.util.NotFoundException;
import com.util.ValidationException;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Subject CRUD.
 *
 * Deleting a subject is the only operation here with a policy worth stating.
 * The foreign key from tasks is ON DELETE RESTRICT, so the database will not
 * let a subject with tasks disappear. Rather than cascade silently - which
 * would make one confirmation click destroy a term's worth of tasks - the user
 * is shown how many tasks are attached and must choose:
 *
 *   MOVE   reassign them to another of their subjects, then delete this one
 *   DELETE delete the tasks with the subject
 *
 * Both run inside one transaction, so a subject is never left half-emptied.
 */
public class SubjectService {

    private static final int NAME_MAX = 80;
    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9a-fA-F]{6}$");

    /** What to do with the tasks of a subject being deleted. */
    public enum DeletePolicy {
        MOVE, DELETE
    }

    private final SubjectDao subjects = new SubjectDao();
    private final UserDao users = new UserDao();

    public List<Subject> listForUser(long userId) {
        return Jpa.read(em -> subjects.listByUser(em, userId));
    }

    /** The subject cards: each subject with its task count and progress. */
    public List<Subject> listWithProgress(long userId) {
        return Jpa.read(em -> subjects.listWithCounts(em, userId));
    }

    public Subject requireOwned(long id, long userId) {
        return Jpa.read(em -> subjects.findOwned(em, id, userId))
                .orElseThrow(() -> new NotFoundException("That subject does not exist."));
    }

    /** Subject plus its task count, for the delete confirmation page. */
    public Subject requireOwnedWithCounts(long id, long userId) {
        return Jpa.read(em -> {
            Subject subject = subjects.findOwned(em, id, userId)
                    .orElseThrow(() -> new NotFoundException("That subject does not exist."));
            subject.setTaskCount(subjects.countTasks(em, subject.getId()));
            return subject;
        });
    }

    public Subject create(long userId, String rawName, String rawColor) {
        String name = validateName(rawName);
        String color = validateColor(rawColor);

        return Jpa.write(em -> {
            if (subjects.nameTaken(em, userId, name, null)) {
                throw new ValidationException("name", "You already have a subject called " + name + ".");
            }
            User owner = users.findById(em, userId)
                    .orElseThrow(() -> new AppException("Your account could not be loaded."));
            try {
                return subjects.insert(em, new Subject(owner, name, color));
            } catch (PersistenceException e) {
                throw new ValidationException("name", "You already have a subject called " + name + ".");
            }
        });
    }

    public Subject update(long id, long userId, String rawName, String rawColor) {
        String name = validateName(rawName);
        String color = validateColor(rawColor);

        return Jpa.write(em -> {
            Subject subject = subjects.findOwned(em, id, userId)
                    .orElseThrow(() -> new NotFoundException("That subject does not exist."));
            if (subjects.nameTaken(em, userId, name, subject.getId())) {
                throw new ValidationException("name", "You already have a subject called " + name + ".");
            }
            subject.setName(name);
            subject.setColor(color);
            try {
                em.flush();
            } catch (PersistenceException e) {
                throw new ValidationException("name", "You already have a subject called " + name + ".");
            }
            return subject;
        });
    }

    /**
     * Deletes a subject and deals with its tasks according to {@code policy}.
     * Returns how many tasks were moved or deleted, for the confirmation
     * message.
     */
    public int delete(long id, long userId, DeletePolicy policy, Long moveToSubjectId) {
        if (policy == null) {
            throw new ValidationException("policy", "Choose what should happen to the tasks.");
        }
        return Jpa.write(em -> {
            Subject subject = subjects.findOwned(em, id, userId)
                    .orElseThrow(() -> new NotFoundException("That subject does not exist."));
            long taskCount = subjects.countTasks(em, subject.getId());
            int affected = 0;

            if (taskCount > 0) {
                if (policy == DeletePolicy.MOVE) {
                    if (moveToSubjectId == null) {
                        throw new ValidationException("moveTo", "Choose the subject to move the tasks to.");
                    }
                    if (moveToSubjectId.equals(subject.getId())) {
                        throw new ValidationException("moveTo",
                                "Tasks cannot be moved to the subject being deleted.");
                    }
                    Subject target = subjects.findOwned(em, moveToSubjectId, userId)
                            .orElseThrow(() -> new ValidationException("moveTo",
                                    "That subject does not exist."));
                    affected = subjects.reassignTasks(em, subject.getId(), target, userId);
                } else {
                    affected = subjects.deleteTasks(em, subject.getId(), userId);
                }
                // Both branches were bulk statements; the subject about to be
                // removed must be re-read or EclipseLink deletes a stale copy.
                em.clear();
                subject = subjects.findOwned(em, id, userId)
                        .orElseThrow(() -> new NotFoundException("That subject does not exist."));
            }

            subjects.remove(em, subject);
            return affected;
        });
    }

    private String validateName(String raw) {
        String name = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            throw new ValidationException("name", "Subject name is required.");
        }
        if (name.length() > NAME_MAX) {
            throw new ValidationException("name",
                    "Subject name cannot be longer than " + NAME_MAX + " characters.");
        }
        return name;
    }

    /** Colour is optional; anything that is not #rrggbb is rejected, not silently dropped. */
    private String validateColor(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String color = raw.trim().toLowerCase();
        if (!HEX_COLOR.matcher(color).matches()) {
            throw new ValidationException("color", "Pick a colour from the list.");
        }
        return color;
    }
}
