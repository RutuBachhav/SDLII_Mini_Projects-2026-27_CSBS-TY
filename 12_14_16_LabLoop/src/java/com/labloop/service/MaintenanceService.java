package com.labloop.service;

import com.labloop.cdi.Audited;
import com.labloop.entity.*;
import com.labloop.util.LabLoopException;
import jakarta.ejb.Stateless;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** STATELESS SESSION BEAN - fault reports, repairs and calibration. */
@Stateless
@Audited
public class MaintenanceService extends ServiceSupport {

    /** Anyone can report a fault. HIGH/CRITICAL faults take the item out of service at once. */
    public MaintenanceTicket open(Long equipmentId, Long reporterId, String issue, TicketSeverity severity) {
        Equipment eq = require(Equipment.class, equipmentId, "Equipment");
        LabUser reporter = requireUser(reporterId);
        if (eq.getStatus() == EquipmentStatus.RETIRED) {
            throw LabLoopException.conflict(eq.getAssetTag() + " is retired.");
        }
        MaintenanceTicket t = new MaintenanceTicket();
        t.setEquipment(eq);
        t.setReportedBy(reporter);
        t.setIssue(issue == null ? null : issue.trim());
        t.setSeverity(severity == null ? TicketSeverity.MEDIUM : severity);
        validate(t);
        em.persist(t);

        String extra = "";
        if (t.getSeverity().takesOutOfService() && eq.getStatus() == EquipmentStatus.AVAILABLE) {
            eq.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
            long affected = em.createQuery("SELECT COUNT(b) FROM Booking b WHERE b.equipment = :e AND b.status IN :st "
                    + "AND b.endTime > :now", Long.class)
                    .setParameter("e", eq)
                    .setParameter("st", List.of(BookingStatus.PENDING, BookingStatus.APPROVED))
                    .setParameter("now", LocalDateTime.now())
                    .getSingleResult();
            extra = " - taken out of service" + (affected > 0 ? ", " + affected + " upcoming booking(s) affected" : "");
        }
        record(reporter.getFullName(), "FAULT_REPORTED",
                eq.getAssetTag() + " [" + t.getSeverity() + "] " + t.getIssue() + extra);
        return t;
    }

    public MaintenanceTicket start(Long ticketId, Long staffId) {
        LabUser staff = requireStaff(staffId);
        MaintenanceTicket t = require(MaintenanceTicket.class, ticketId, "Ticket");
        if (t.getStatus() != TicketStatus.OPEN) {
            throw LabLoopException.conflict("Ticket #" + ticketId + " is already " + t.getStatus().getLabel().toLowerCase() + ".");
        }
        t.setStatus(TicketStatus.IN_PROGRESS);
        Equipment eq = t.getEquipment();
        if (eq.getStatus() == EquipmentStatus.AVAILABLE) {
            eq.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        }
        record(staff.getFullName(), "REPAIR_STARTED", "Ticket #" + t.getId() + " " + eq.getAssetTag());
        return t;
    }

    /** Closing a ticket counts as a service: lastServicedOn = today. Item returns to AVAILABLE if nothing else is open. */
    public MaintenanceTicket resolve(Long ticketId, Long staffId, String notes, double cost) {
        LabUser staff = requireStaff(staffId);
        MaintenanceTicket t = require(MaintenanceTicket.class, ticketId, "Ticket");
        if (t.getStatus() == TicketStatus.RESOLVED) {
            throw LabLoopException.conflict("Ticket #" + ticketId + " is already resolved.");
        }
        if (notes == null || notes.isBlank()) {
            throw LabLoopException.validation("Describe what was done to fix it.");
        }
        t.setStatus(TicketStatus.RESOLVED);
        t.setResolutionNotes(notes.trim());
        t.setRepairCost(cost);
        t.setResolvedAt(LocalDateTime.now());
        validate(t);

        Equipment eq = t.getEquipment();
        eq.setLastServicedOn(LocalDate.now());
        long stillOpen = em.createQuery("SELECT COUNT(m) FROM MaintenanceTicket m WHERE m.equipment = :e "
                + "AND m.status <> :r AND m.id <> :self", Long.class)
                .setParameter("e", eq).setParameter("r", TicketStatus.RESOLVED).setParameter("self", t.getId())
                .getSingleResult();
        if (stillOpen == 0 && eq.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
            eq.setStatus(EquipmentStatus.AVAILABLE);
        }
        record(staff.getFullName(), "REPAIR_RESOLVED", "Ticket #" + t.getId() + " " + eq.getAssetTag()
                + (cost > 0 ? " (Rs. " + Math.round(cost) + ")" : ""));
        return t;
    }

    /** Calibration/service done without a fault (resets the service-due clock). */
    public void markServiced(Long equipmentId, Long staffId) {
        LabUser staff = requireStaff(staffId);
        Equipment eq = require(Equipment.class, equipmentId, "Equipment");
        eq.setLastServicedOn(LocalDate.now());
        record(staff.getFullName(), "SERVICED", eq.getAssetTag() + " routine service done");
    }

    public List<MaintenanceTicket> list(TicketStatus status) {
        if (status == null) {
            return em.createQuery("SELECT t FROM MaintenanceTicket t ORDER BY t.openedAt DESC", MaintenanceTicket.class)
                    .setMaxResults(200).getResultList();
        }
        return em.createQuery("SELECT t FROM MaintenanceTicket t WHERE t.status = :s ORDER BY t.openedAt DESC",
                MaintenanceTicket.class).setParameter("s", status).getResultList();
    }

    /** Open + in-progress tickets, most severe first (sorted in Java because the enum is stored as text). */
    public List<MaintenanceTicket> unresolved() {
        List<MaintenanceTicket> list = em.createQuery(
                "SELECT t FROM MaintenanceTicket t WHERE t.status <> :r ORDER BY t.openedAt",
                MaintenanceTicket.class).setParameter("r", TicketStatus.RESOLVED).getResultList();
        list.sort(Comparator.comparing((MaintenanceTicket t) -> t.getSeverity().ordinal()).reversed());
        return list;
    }

    public List<MaintenanceTicket> forEquipment(Long equipmentId) {
        return em.createQuery("SELECT t FROM MaintenanceTicket t WHERE t.equipment.id = :e ORDER BY t.openedAt DESC",
                MaintenanceTicket.class).setParameter("e", equipmentId).getResultList();
    }
}
