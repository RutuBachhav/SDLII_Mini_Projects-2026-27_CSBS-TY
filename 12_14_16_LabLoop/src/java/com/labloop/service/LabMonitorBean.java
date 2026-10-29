package com.labloop.service;

import com.labloop.cdi.LabActivity;
import com.labloop.entity.*;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * SINGLETON #2 - the lab's "control room".
 *  - Caches dashboard numbers so 200 students opening the dashboard don't fire 200 x 10 queries.
 *  - @Schedule timer runs housekeeping every 10 minutes (expire stale requests, log overdue returns).
 *  - Observes the LabActivity CDI event: any change marks the cache stale.
 * persistent = false: the timer is not stored in a database (no timer-pool setup needed in GlassFish).
 */
@Singleton
@Startup
@DependsOn("LabLoopSettingsBean")
public class LabMonitorBean {

    private static final Logger LOG = Logger.getLogger(LabMonitorBean.class.getName());

    @PersistenceContext(unitName = "LabLoopPU")
    private EntityManager em;

    @EJB
    private BookingService bookings;

    private volatile DashboardStats cached;
    private volatile boolean stale = true;
    private volatile LocalDateTime lastHousekeeping;

    /** Many readers at once; recomputes only when something changed. */
    @Lock(LockType.READ)
    public DashboardStats getStats() {
        DashboardStats s = cached;
        if (stale || s == null) {
            stale = false;
            s = compute();
            cached = s;
        }
        return s;
    }

    /** CDI observer: called for every booking/repair/inventory change. */
    @Lock(LockType.READ)
    public void onActivity(@Observes LabActivity event) {
        stale = true;
    }

    /**
     * READ lock on purpose: housekeeping fires CDI events, which call onActivity() on this same bean.
     * Keeping both methods READ avoids any lock upgrade. (WRITE locking is shown in LabLoopSettingsBean.update.)
     * EJB timers never overlap for the same @Schedule, so two housekeeping runs cannot collide.
     */
    @Schedule(minute = "*/10", hour = "*", persistent = false)
    @Lock(LockType.READ)
    public void housekeeping() {
        int expired = bookings.expireStalePending();
        int overdue = bookings.overdue().size();
        lastHousekeeping = LocalDateTime.now();
        stale = true;
        LOG.info("[LabLoop] Housekeeping: " + expired + " request(s) expired, " + overdue + " overdue return(s)");
    }

    private DashboardStats compute() {
        DashboardStats s = new DashboardStats();
        List<Equipment> all = em.createQuery("SELECT e FROM Equipment e ORDER BY e.lab.code", Equipment.class).getResultList();

        List<DashboardStats.LabLoad> loads = new ArrayList<>();
        List<Lab> labs = em.createQuery("SELECT l FROM Lab l ORDER BY l.code", Lab.class).getResultList();
        for (Lab lab : labs) {
            long total = 0, inUse = 0, down = 0;
            for (Equipment e : all) {
                if (!e.getLab().getId().equals(lab.getId()) || e.getStatus() == EquipmentStatus.RETIRED) continue;
                total++;
                if (e.getStatus() == EquipmentStatus.IN_USE) inUse++;
                if (e.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) down++;
            }
            loads.add(new DashboardStats.LabLoad(lab.getCode(), lab.getName(), total, inUse, down));
        }
        s.setLabs(loads);

        for (Equipment e : all) {
            switch (e.getStatus()) {
                case AVAILABLE: s.setAvailable(s.getAvailable() + 1); break;
                case IN_USE: s.setInUse(s.getInUse() + 1); break;
                case UNDER_MAINTENANCE: s.setUnderMaintenance(s.getUnderMaintenance() + 1); break;
                default: s.setRetired(s.getRetired() + 1);
            }
            if (e.getStatus() != EquipmentStatus.RETIRED && e.isServiceOverdue()) {
                s.setServiceDue(s.getServiceDue() + 1);
            }
        }
        long active = all.size() - s.getRetired();
        s.setTotalEquipment(active);
        s.setUtilizationPercent(active == 0 ? 0 : (int) Math.round(s.getInUse() * 100.0 / active));

        s.setPendingApprovals(count("SELECT COUNT(b) FROM Booking b WHERE b.status = :p", BookingStatus.PENDING));
        s.setOpenTickets(em.createQuery("SELECT COUNT(t) FROM MaintenanceTicket t WHERE t.status <> :r", Long.class)
                .setParameter("r", TicketStatus.RESOLVED).getSingleResult());
        s.setOverdueReturns(em.createQuery("SELECT COUNT(b) FROM Booking b WHERE b.status = :s AND b.endTime < :now", Long.class)
                .setParameter("s", BookingStatus.CHECKED_OUT).setParameter("now", LocalDateTime.now()).getSingleResult());
        LocalDate today = LocalDate.now();
        s.setBookingsToday(em.createQuery("SELECT COUNT(b) FROM Booking b WHERE b.status IN :st "
                + "AND b.startTime < :to AND b.endTime > :from", Long.class)
                .setParameter("st", new ArrayList<>(BookingStatus.blockingSlot()))
                .setParameter("from", today.atStartOfDay())
                .setParameter("to", today.plusDays(1).atStartOfDay())
                .getSingleResult());
        s.setGeneratedAt(LocalDateTime.now());
        s.setLastHousekeeping(lastHousekeeping);
        return s;
    }

    private long count(String jpql, Object p) {
        return em.createQuery(jpql, Long.class).setParameter("p", p).getSingleResult();
    }
}
