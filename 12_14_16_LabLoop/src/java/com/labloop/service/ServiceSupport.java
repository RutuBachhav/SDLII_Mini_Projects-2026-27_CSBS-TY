package com.labloop.service;

import com.labloop.cdi.LabActivity;
import com.labloop.entity.LabUser;
import com.labloop.util.LabLoopException;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared plumbing for every service EJB.
 *
 * Difference from the class assignments: there each servlet created its own
 * EntityManagerFactory and called begin()/commit() by hand (RESOURCE_LOCAL).
 * Here GlassFish INJECTS a container-managed EntityManager with @PersistenceContext and every
 * EJB business method runs inside a JTA transaction automatically - commit on success,
 * rollback on exception. This is how real Jakarta EE systems are written.
 */
public abstract class ServiceSupport {

    @PersistenceContext(unitName = "LabLoopPU")
    protected EntityManager em;

    /** Bean Validation engine, injected by CDI. */
    @Inject
    protected Validator validator;

    /** CDI event source - fire() notifies every @Observes LabActivity method. */
    @Inject
    protected Event<LabActivity> activity;

    /** Runs the @NotBlank/@Email/@Pattern... annotations and turns violations into readable messages. */
    protected <T> void validate(T bean) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        if (!violations.isEmpty()) {
            List<String> msgs = new ArrayList<>();
            for (ConstraintViolation<T> v : violations) {
                msgs.add(v.getMessage());
            }
            msgs.sort(String::compareTo);
            throw LabLoopException.validation(msgs);
        }
    }

    protected <T> T require(Class<T> type, Long id, String what) {
        T found = id == null ? null : em.find(type, id);
        if (found == null) {
            throw LabLoopException.notFound(what + " #" + id + " was not found.");
        }
        return found;
    }

    protected LabUser requireUser(Long userId) {
        return require(LabUser.class, userId, "User");
    }

    protected LabUser requireStaff(Long userId) {
        LabUser u = requireUser(userId);
        if (!u.isStaff()) {
            throw LabLoopException.forbidden("Only lab assistants and admins can do this.");
        }
        return u;
    }

    protected void record(String actor, String action, String details) {
        activity.fire(new LabActivity(actor, action, details));
    }
}
