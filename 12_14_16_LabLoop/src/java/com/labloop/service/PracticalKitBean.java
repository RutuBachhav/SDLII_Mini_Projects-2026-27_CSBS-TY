package com.labloop.service;

import com.labloop.entity.Equipment;
import com.labloop.util.LabLoopException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.Stateful;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * STATEFUL SESSION BEAN - the "Practical Kit".
 *
 * A practical usually needs several items together (oscilloscope + function generator + logic analyzer).
 * The user collects them into a kit over several page visits; the bean REMEMBERS the list between
 * requests (that is what "stateful" means). On submit, all items are booked for one slot in a single
 * all-or-nothing transaction.
 *
 * Why @SessionScoped?  In Assignment 2A the stateful bean was injected with @EJB into a servlet field.
 * A servlet has ONE instance shared by every user, so every user would share ONE stateful bean -
 * Rutuja's data could be seen by the next user. Giving the stateful bean a CDI @SessionScoped scope
 * and injecting it with @Inject gives each browser session its own private instance.
 */
@Stateful
@SessionScoped
public class PracticalKitBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PracticalKitBean.class.getName());
    public static final int MAX_ITEMS = 6;

    @EJB
    private BookingService bookingService;

    @EJB
    private EquipmentService equipmentService;

    private final List<KitItem> items = new ArrayList<>();
    private String lastPurpose;

    @PostConstruct
    void created() { LOG.info("[LabLoop] Stateful PracticalKitBean created for a new session"); }

    @PreDestroy
    void destroyed() { LOG.info("[LabLoop] Stateful PracticalKitBean destroyed (session ended)"); }

    @PrePassivate
    void passivating() { LOG.info("[LabLoop] PracticalKitBean passivated to disk"); }

    @PostActivate
    void activated() { LOG.info("[LabLoop] PracticalKitBean activated from disk"); }

    public void add(Long equipmentId) {
        Equipment e = equipmentService.find(equipmentId);
        if (!e.isBookable()) {
            throw LabLoopException.validation(e.getAssetTag() + " is " + e.getStatus().getLabel().toLowerCase()
                    + " and cannot be added to a kit.");
        }
        for (KitItem i : items) {
            if (i.getEquipmentId().equals(equipmentId)) {
                throw LabLoopException.validation(e.getAssetTag() + " is already in your kit.");
            }
        }
        if (items.size() >= MAX_ITEMS) {
            throw LabLoopException.validation("A kit can hold at most " + MAX_ITEMS + " items.");
        }
        items.add(new KitItem(e.getId(), e.getAssetTag(), e.getName(), e.getLab().getCode()));
    }

    public void remove(Long equipmentId) {
        items.removeIf(i -> i.getEquipmentId().equals(equipmentId));
    }

    public List<KitItem> getItems() { return Collections.unmodifiableList(new ArrayList<>(items)); }

    public int getSize() { return items.size(); }

    public boolean contains(Long equipmentId) {
        for (KitItem i : items) {
            if (i.getEquipmentId().equals(equipmentId)) return true;
        }
        return false;
    }

    public String getLastPurpose() { return lastPurpose; }

    /** Books every item; on success the kit is emptied, on failure it is kept so the user can adjust. */
    public String submit(Long userId, LocalDateTime start, LocalDateTime end, String purpose) {
        lastPurpose = purpose;
        List<Long> ids = new ArrayList<>();
        for (KitItem i : items) ids.add(i.getEquipmentId());
        String ref = bookingService.submitKit(userId, ids, start, end, purpose);
        items.clear();
        lastPurpose = null;
        return ref;
    }

    public void clear() {
        items.clear();
        lastPurpose = null;
    }
}
