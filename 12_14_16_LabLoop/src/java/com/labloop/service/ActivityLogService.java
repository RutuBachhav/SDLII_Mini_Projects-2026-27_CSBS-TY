package com.labloop.service;

import com.labloop.cdi.LabActivity;
import com.labloop.entity.ActivityLog;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * CDI EVENT OBSERVER. Every time any service fires a LabActivity event, onActivity() runs
 * and stores an audit row - in the SAME transaction, so if the booking rolls back, so does its log.
 */
@Stateless
public class ActivityLogService {

    @PersistenceContext(unitName = "LabLoopPU")
    private EntityManager em;

    public void onActivity(@Observes LabActivity event) {
        String details = event.getDetails();
        if (details != null && details.length() > 300) {
            details = details.substring(0, 297) + "...";
        }
        em.persist(new ActivityLog(event.getActor(), event.getAction(), details));
    }

    public List<ActivityLog> recent(int limit) {
        return em.createQuery("SELECT a FROM ActivityLog a ORDER BY a.occurredAt DESC, a.id DESC", ActivityLog.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
