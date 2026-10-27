package com.labloop.service;

import com.labloop.cdi.Audited;
import com.labloop.entity.EquipmentStatus;
import com.labloop.entity.Lab;
import com.labloop.util.LabLoopException;
import jakarta.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** STATELESS SESSION BEAN - lab rooms. */
@Stateless
@Audited
public class LabService extends ServiceSupport {

    public List<Lab> findAll() {
        return em.createQuery("SELECT l FROM Lab l ORDER BY l.code", Lab.class).getResultList();
    }

    public Lab find(Long id) {
        return require(Lab.class, id, "Lab");
    }

    /** labId -> number of (non-retired) equipment items. One query instead of one per lab. */
    public Map<Long, Long> equipmentCounts() {
        List<Object[]> rows = em.createQuery(
                "SELECT e.lab.id, COUNT(e) FROM Equipment e "
                + "WHERE e.status <> :retired GROUP BY e.lab.id", Object[].class)
                .setParameter("retired", EquipmentStatus.RETIRED)
                .getResultList();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put((Long) r[0], ((Number) r[1]).longValue());
        }
        return map;
    }

    public Lab create(Lab lab, String actor) {
        normalise(lab);
        validate(lab);
        ensureCodeUnique(lab.getCode(), null);
        em.persist(lab);
        record(actor, "LAB_CREATED", lab.getCode() + " " + lab.getName());
        return lab;
    }

    public Lab update(Long id, Lab form, String actor) {
        Lab lab = find(id);
        normalise(form);
        lab.setCode(form.getCode());
        lab.setName(form.getName());
        lab.setLocation(form.getLocation());
        lab.setCapacity(form.getCapacity());
        lab.setInCharge(form.getInCharge());
        validate(lab);
        ensureCodeUnique(lab.getCode(), id);
        record(actor, "LAB_UPDATED", lab.getCode());
        return lab;
    }

    public void delete(Long id, String actor) {
        Lab lab = find(id);
        long items = em.createQuery("SELECT COUNT(e) FROM Equipment e WHERE e.lab = :lab", Long.class)
                .setParameter("lab", lab).getSingleResult();
        if (items > 0) {
            throw LabLoopException.conflict("Lab " + lab.getCode() + " still has " + items
                    + " equipment item(s). Move or delete them first.");
        }
        em.remove(lab);
        record(actor, "LAB_DELETED", lab.getCode());
    }

    private void normalise(Lab lab) {
        if (lab.getCode() != null) lab.setCode(lab.getCode().trim().toUpperCase());
    }

    private void ensureCodeUnique(String code, Long selfId) {
        List<Lab> same = em.createQuery("SELECT l FROM Lab l WHERE l.code = :c", Lab.class)
                .setParameter("c", code).getResultList();
        for (Lab l : same) {
            if (!l.getId().equals(selfId)) {
                throw LabLoopException.conflict("Lab code " + code + " is already used.");
            }
        }
    }
}
