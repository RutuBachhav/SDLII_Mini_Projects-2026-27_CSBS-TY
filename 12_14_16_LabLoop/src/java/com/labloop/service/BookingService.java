package com.labloop.service;

import com.labloop.cdi.Audited;
import com.labloop.cdi.BookingPolicy;
import com.labloop.entity.*;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * STATELESS SESSION BEAN - the heart of LabLoop: reservations and the issue/return desk.
 *
 * Booking life-cycle:
 *   PENDING --approve--> APPROVED --check out--> CHECKED_OUT --return--> RETURNED
 *      |                    |
 *      +--reject/expire--> REJECTED      +--cancel--> CANCELLED
 */
@Stateless
@Audited
public class BookingService extends ServiceSupport {

    /** CDI bean injected into an EJB. */
    @Inject
    private BookingPolicy policy;

    /** EJB injected into another EJB - a damaged return raises a maintenance ticket. */
    @EJB
    private MaintenanceService maintenance;

    // ===================== create =====================

    public Booking create(Long equipmentId, Long userId, LocalDateTime start, LocalDateTime end, String purpose) {
        LabUser user = requireUser(userId);
        Booking b = createOne(require(Equipment.class, equipmentId, "Equipment"), user, start, end, purpose, null);
        record(user.getFullName(), "BOOKING_REQUESTED", describe(b));
        return b;
    }

    /**
     * Books several items for the SAME slot in ONE transaction (used by the Practical Kit).
     * All-or-nothing: if item 3 clashes, items 1 and 2 are rolled back too.
     */
    public String submitKit(Long userId, List<Long> equipmentIds, LocalDateTime start, LocalDateTime end, String purpose) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            throw LabLoopException.validation("Your kit is empty.");
        }
        LabUser user = requireUser(userId);
        String ref = "KIT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        List<String> tags = new ArrayList<>();
        for (Long id : equipmentIds) {
            Booking b = createOne(require(Equipment.class, id, "Equipment"), user, start, end, purpose, ref);
            tags.add(b.getEquipment().getAssetTag());
        }
        record(user.getFullName(), "KIT_SUBMITTED", ref + ": " + String.join(", ", tags));
        return ref;
    }

    private Booking createOne(Equipment eq, LabUser user, LocalDateTime start, LocalDateTime end,
                              String purpose, String ref) {
        List<String> problems = new ArrayList<>(policy.check(user, start, end));
        if (!eq.isBookable()) {
            problems.add(eq.getAssetTag() + " is " + eq.getStatus().getLabel().toLowerCase() + " and cannot be booked right now.");
        }
        if (!problems.isEmpty()) {
            throw LabLoopException.validation(problems);
        }
        Booking clash = findClash(eq, start, end, null);
        if (clash != null) {
            throw LabLoopException.conflict(eq.getAssetTag() + " (" + eq.getName() + ") is already booked "
                    + clash.getStartDisplay() + " to " + clash.getEndDisplay() + ". Pick another slot.");
        }
        Booking b = new Booking();
        b.setEquipment(eq);
        b.setRequestedBy(user);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setPurpose(purpose == null ? null : purpose.trim());
        b.setRequestRef(ref);
        if (policy.autoApprove(user)) {
            b.setStatus(BookingStatus.APPROVED);
            b.setDecidedBy(user);
            b.setStaffRemark("Auto-approved (" + user.getRole().getLabel() + ")");
        } else {
            b.setStatus(BookingStatus.PENDING);
        }
        validate(b);
        em.persist(b);
        return b;
    }

    /** Overlap rule: two slots clash when  existing.start < new.end  AND  existing.end > new.start. */
    private Booking findClash(Equipment eq, LocalDateTime start, LocalDateTime end, Long ignoreId) {
        List<Booking> r = em.createQuery(
                "SELECT b FROM Booking b WHERE b.equipment = :eq AND b.status IN :active "
                + "AND b.startTime < :end AND b.endTime > :start ORDER BY b.startTime", Booking.class)
                .setParameter("eq", eq)
                .setParameter("active", new ArrayList<>(BookingStatus.blockingSlot()))
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        r.removeIf(b -> b.getId().equals(ignoreId));
        return r.isEmpty() ? null : r.get(0);
    }

    // ===================== staff workflow =====================

    public Booking approve(Long bookingId, Long staffId, String remark) {
        LabUser staff = requireStaff(staffId);
        Booking b = requireStatus(bookingId, BookingStatus.PENDING);
        if (!b.getEquipment().isBookable()) {
            throw LabLoopException.conflict(b.getEquipment().getAssetTag() + " is "
                    + b.getEquipment().getStatus().getLabel().toLowerCase() + " - reject this request instead.");
        }
        b.setStatus(BookingStatus.APPROVED);
        b.setDecidedBy(staff);
        b.setStaffRemark(blankToNull(remark));
        record(staff.getFullName(), "BOOKING_APPROVED", describe(b));
        return b;
    }

    public Booking reject(Long bookingId, Long staffId, String remark) {
        LabUser staff = requireStaff(staffId);
        Booking b = requireStatus(bookingId, BookingStatus.PENDING);
        if (remark == null || remark.isBlank()) {
            throw LabLoopException.validation("Give a reason when rejecting a request.");
        }
        b.setStatus(BookingStatus.REJECTED);
        b.setDecidedBy(staff);
        b.setStaffRemark(remark.trim());
        record(staff.getFullName(), "BOOKING_REJECTED", describe(b) + " - " + remark.trim());
        return b;
    }

    /** Issue desk: the student collects the item. */
    public Booking checkOut(Long bookingId, Long staffId) {
        LabUser staff = requireStaff(staffId);
        Booking b = requireStatus(bookingId, BookingStatus.APPROVED);
        Equipment eq = b.getEquipment();
        if (eq.getStatus() != EquipmentStatus.AVAILABLE) {
            throw LabLoopException.conflict(eq.getAssetTag() + " is " + eq.getStatus().getLabel().toLowerCase()
                    + " and cannot be issued.");
        }
        if (LocalDateTime.now().isBefore(b.getStartTime().minusMinutes(30))) {
            throw LabLoopException.validation("Too early - items can be issued from 30 minutes before the slot ("
                    + b.getStartDisplay() + ").");
        }
        b.setStatus(BookingStatus.CHECKED_OUT);
        b.setCheckedOutAt(LocalDateTime.now());
        eq.setStatus(EquipmentStatus.IN_USE);
        record(staff.getFullName(), "CHECKED_OUT", describe(b));
        return b;
    }

    /** Return desk. If the item comes back damaged, a HIGH maintenance ticket is raised automatically. */
    public Booking returnItem(Long bookingId, Long staffId, boolean damaged, String conditionNote) {
        LabUser staff = requireStaff(staffId);
        Booking b = requireStatus(bookingId, BookingStatus.CHECKED_OUT);
        Equipment eq = b.getEquipment();
        boolean late = b.isOverdue();
        b.setStatus(BookingStatus.RETURNED);
        b.setReturnedAt(LocalDateTime.now());
        String note = blankToNull(conditionNote);
        b.setStaffRemark((late ? "Returned late. " : "") + (damaged ? "Damaged: " : "") + (note == null ? "" : note));
        eq.setStatus(EquipmentStatus.AVAILABLE);
        record(staff.getFullName(), late ? "RETURNED_LATE" : "RETURNED", describe(b));
        if (damaged) {
            maintenance.open(eq.getId(), staff.getId(),
                    "Returned damaged by " + b.getRequestedBy().getFullName() + (note == null ? "" : ": " + note),
                    TicketSeverity.HIGH);
        }
        return b;
    }

    /** The requester (or staff) cancels a booking that has not been collected yet. */
    public Booking cancel(Long bookingId, Long userId) {
        LabUser user = requireUser(userId);
        Booking b = require(Booking.class, bookingId, "Booking");
        if (!user.isStaff() && !b.getRequestedBy().getId().equals(user.getId())) {
            throw LabLoopException.forbidden("You can only cancel your own bookings.");
        }
        if (b.getStatus() != BookingStatus.PENDING && b.getStatus() != BookingStatus.APPROVED) {
            throw LabLoopException.conflict("Only pending or approved bookings can be cancelled (this one is "
                    + b.getStatus().getLabel().toLowerCase() + ").");
        }
        b.setStatus(BookingStatus.CANCELLED);
        record(user.getFullName(), "BOOKING_CANCELLED", describe(b));
        return b;
    }

    /** Housekeeping (called by the Singleton timer): requests nobody approved before the slot started. */
    public int expireStalePending() {
        List<Booking> stale = em.createQuery(
                "SELECT b FROM Booking b WHERE b.status = :p AND b.startTime < :now", Booking.class)
                .setParameter("p", BookingStatus.PENDING)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        for (Booking b : stale) {
            b.setStatus(BookingStatus.REJECTED);
            b.setStaffRemark("Expired - not approved before the slot started");
        }
        if (!stale.isEmpty()) {
            record("SYSTEM", "AUTO_EXPIRED", stale.size() + " unapproved request(s) expired");
        }
        return stale.size();
    }

    // ===================== queries =====================

    public Booking find(Long id) { return require(Booking.class, id, "Booking"); }

    public List<Booking> forUser(Long userId) {
        return em.createQuery("SELECT b FROM Booking b WHERE b.requestedBy.id = :u ORDER BY b.startTime DESC", Booking.class)
                .setParameter("u", userId).getResultList();
    }

    public List<Booking> byStatus(BookingStatus status) {
        if (status == null) {
            return em.createQuery("SELECT b FROM Booking b ORDER BY b.startTime DESC", Booking.class)
                    .setMaxResults(300).getResultList();
        }
        return em.createQuery("SELECT b FROM Booking b WHERE b.status = :s ORDER BY b.startTime", Booking.class)
                .setParameter("s", status).getResultList();
    }

    public List<Booking> forEquipment(Long equipmentId) {
        return em.createQuery("SELECT b FROM Booking b WHERE b.equipment.id = :e ORDER BY b.startTime DESC", Booking.class)
                .setParameter("e", equipmentId).setMaxResults(50).getResultList();
    }

    /** Slots already taken for one item on one day - drives the availability strip and GET /api/equipment/{id}/availability. */
    public List<Booking> takenOn(Long equipmentId, LocalDate day) {
        return em.createQuery("SELECT b FROM Booking b WHERE b.equipment.id = :e AND b.status IN :active "
                + "AND b.startTime < :to AND b.endTime > :from ORDER BY b.startTime", Booking.class)
                .setParameter("e", equipmentId)
                .setParameter("active", new ArrayList<>(BookingStatus.blockingSlot()))
                .setParameter("from", day.atStartOfDay())
                .setParameter("to", day.plusDays(1).atStartOfDay())
                .getResultList();
    }

    public List<Booking> overdue() {
        return em.createQuery("SELECT b FROM Booking b WHERE b.status = :s AND b.endTime < :now ORDER BY b.endTime", Booking.class)
                .setParameter("s", BookingStatus.CHECKED_OUT)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    /** Approved bookings starting within the next 24 hours (the issue desk's to-do list). */
    public List<Booking> dueForPickup() {
        LocalDateTime now = LocalDateTime.now();
        return em.createQuery("SELECT b FROM Booking b WHERE b.status = :s AND b.startTime < :soon AND b.endTime > :now "
                + "ORDER BY b.startTime", Booking.class)
                .setParameter("s", BookingStatus.APPROVED)
                .setParameter("soon", now.plusHours(24))
                .setParameter("now", now)
                .getResultList();
    }

    public List<Booking> upcomingForUser(Long userId) {
        return em.createQuery("SELECT b FROM Booking b WHERE b.requestedBy.id = :u AND b.status IN :active "
                + "AND b.endTime > :now ORDER BY b.startTime", Booking.class)
                .setParameter("u", userId)
                .setParameter("active", new ArrayList<>(BookingStatus.blockingSlot()))
                .setParameter("now", LocalDateTime.now())
                .setMaxResults(8)
                .getResultList();
    }

    // ===================== helpers =====================

    private Booking requireStatus(Long id, BookingStatus expected) {
        Booking b = require(Booking.class, id, "Booking");
        if (b.getStatus() != expected) {
            throw LabLoopException.conflict("Booking #" + id + " is " + b.getStatus().getLabel().toLowerCase()
                    + ", expected " + expected.getLabel().toLowerCase() + ".");
        }
        return b;
    }

    private static String describe(Booking b) {
        return "#" + b.getId() + " " + b.getEquipment().getAssetTag() + " for " + b.getRequestedBy().getFullName()
                + " (" + b.getStartDisplay() + ")";
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
