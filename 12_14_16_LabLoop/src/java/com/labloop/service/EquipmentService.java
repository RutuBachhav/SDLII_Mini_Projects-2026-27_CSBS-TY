package com.labloop.service;

import com.labloop.cdi.Audited;
import com.labloop.entity.*;
import com.labloop.util.LabLoopException;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import java.util.List;

/** STATELESS SESSION BEAN - equipment inventory (add / view / update / delete / retire). */
@Stateless
@Audited
public class EquipmentService extends ServiceSupport {

    /** Search with optional filters; any argument may be null. */
    public List<Equipment> search(Long labId, EquipmentStatus status, EquipmentCategory category, String text) {
        StringBuilder jpql = new StringBuilder("SELECT e FROM Equipment e WHERE 1 = 1");
        if (labId != null) jpql.append(" AND e.lab.id = :lab");
        if (status != null) jpql.append(" AND e.status = :status");
        else jpql.append(" AND e.status <> :retired");
        if (category != null) jpql.append(" AND e.category = :cat");
        if (text != null && !text.isBlank()) {
            jpql.append(" AND (LOWER(e.name) LIKE :q OR LOWER(e.assetTag) LIKE :q OR LOWER(e.model) LIKE :q)");
        }
        jpql.append(" ORDER BY e.lab.code, e.name");

        TypedQuery<Equipment> q = em.createQuery(jpql.toString(), Equipment.class);
        if (labId != null) q.setParameter("lab", labId);
        if (status != null) q.setParameter("status", status);
        else q.setParameter("retired", EquipmentStatus.RETIRED);
        if (category != null) q.setParameter("cat", category);
        if (text != null && !text.isBlank()) q.setParameter("q", "%" + text.trim().toLowerCase() + "%");
        return q.getResultList();
    }

    public List<Equipment> findAll() {
        return search(null, null, null, null);
    }

    public Equipment find(Long id) {
        return require(Equipment.class, id, "Equipment");
    }

    public Equipment findByTag(String tag) {
        List<Equipment> r = em.createQuery("SELECT e FROM Equipment e WHERE e.assetTag = :t", Equipment.class)
                .setParameter("t", tag == null ? "" : tag.trim().toUpperCase()).getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    public Equipment create(Equipment e, Long labId, String actor) {
        e.setLab(require(Lab.class, labId, "Lab"));
        if (e.getAssetTag() != null) e.setAssetTag(e.getAssetTag().trim().toUpperCase());
        e.setStatus(EquipmentStatus.AVAILABLE);
        validate(e);
        if (findByTag(e.getAssetTag()) != null) {
            throw LabLoopException.conflict("Asset tag " + e.getAssetTag() + " already exists.");
        }
        em.persist(e);
        record(actor, "EQUIPMENT_ADDED", e.getAssetTag() + " " + e.getName() + " -> " + e.getLab().getCode());
        return e;
    }

    public Equipment update(Long id, Equipment form, Long labId, String actor) {
        Equipment e = find(id);
        e.setName(form.getName());
        e.setModel(form.getModel());
        e.setCategory(form.getCategory());
        e.setPurchaseDate(form.getPurchaseDate());
        e.setPurchaseCost(form.getPurchaseCost());
        e.setServiceIntervalDays(form.getServiceIntervalDays());
        e.setLastServicedOn(form.getLastServicedOn());
        e.setNotes(form.getNotes());
        if (labId != null && !labId.equals(e.getLab().getId())) {
            if (e.getStatus() == EquipmentStatus.IN_USE) {
                throw LabLoopException.conflict("Cannot move " + e.getAssetTag() + " to another lab while it is checked out.");
            }
            e.setLab(require(Lab.class, labId, "Lab"));
        }
        validate(e);
        // em.merge() is not needed: e is a MANAGED entity, changes are saved automatically at commit.
        record(actor, "EQUIPMENT_UPDATED", e.getAssetTag());
        return e;
    }

    /**
     * Hard delete is only allowed for items with no history (e.g. added by mistake).
     * Items with bookings or repairs must be RETIRED instead, so the audit history is kept.
     */
    public void delete(Long id, String actor) {
        Equipment e = find(id);
        long bookings = em.createQuery("SELECT COUNT(b) FROM Booking b WHERE b.equipment = :e", Long.class)
                .setParameter("e", e).getSingleResult();
        long tickets = em.createQuery("SELECT COUNT(t) FROM MaintenanceTicket t WHERE t.equipment = :e", Long.class)
                .setParameter("e", e).getSingleResult();
        if (bookings + tickets > 0) {
            throw LabLoopException.conflict(e.getAssetTag() + " has " + bookings + " booking(s) and " + tickets
                    + " maintenance record(s). Retire it instead of deleting, to keep the history.");
        }
        em.remove(e);
        record(actor, "EQUIPMENT_DELETED", e.getAssetTag() + " " + e.getName());
    }

    public void retire(Long id, String actor) {
        Equipment e = find(id);
        if (e.getStatus() == EquipmentStatus.IN_USE) {
            throw LabLoopException.conflict(e.getAssetTag() + " is checked out. Record the return first.");
        }
        long future = em.createQuery("SELECT COUNT(b) FROM Booking b WHERE b.equipment = :e AND b.status IN :st", Long.class)
                .setParameter("e", e).setParameter("st", List.of(BookingStatus.PENDING, BookingStatus.APPROVED))
                .getSingleResult();
        if (future > 0) {
            throw LabLoopException.conflict(e.getAssetTag() + " still has " + future
                    + " upcoming booking(s). Reject or cancel them first.");
        }
        e.setStatus(EquipmentStatus.RETIRED);
        record(actor, "EQUIPMENT_RETIRED", e.getAssetTag());
    }

    public List<Equipment> serviceDue() {
        List<Equipment> all = findAll();
        all.removeIf(e -> !e.isServiceOverdue());
        return all;
    }
}
