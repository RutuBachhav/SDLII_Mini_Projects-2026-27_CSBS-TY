package com.labloop.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Immutable-ish snapshot of the numbers on the dashboard (also served at GET /api/stats). */
public class DashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class LabLoad implements Serializable {
        private static final long serialVersionUID = 1L;
        private String code;
        private String name;
        private long total;
        private long inUse;
        private long down;

        public LabLoad() { }
        public LabLoad(String code, String name, long total, long inUse, long down) {
            this.code = code; this.name = name; this.total = total; this.inUse = inUse; this.down = down;
        }
        public String getCode() { return code; }
        public String getName() { return name; }
        public long getTotal() { return total; }
        public long getInUse() { return inUse; }
        public long getDown() { return down; }
        public int getInUsePercent() { return total == 0 ? 0 : (int) Math.round(inUse * 100.0 / total); }
        public int getDownPercent() { return total == 0 ? 0 : (int) Math.round(down * 100.0 / total); }
    }

    private long totalEquipment;
    private long available;
    private long inUse;
    private long underMaintenance;
    private long retired;
    private long pendingApprovals;
    private long overdueReturns;
    private long openTickets;
    private long serviceDue;
    private long bookingsToday;
    private int utilizationPercent;
    private LocalDateTime generatedAt;
    private LocalDateTime lastHousekeeping;
    private List<LabLoad> labs = new ArrayList<>();

    public long getTotalEquipment() { return totalEquipment; }
    public void setTotalEquipment(long v) { totalEquipment = v; }
    public long getAvailable() { return available; }
    public void setAvailable(long v) { available = v; }
    public long getInUse() { return inUse; }
    public void setInUse(long v) { inUse = v; }
    public long getUnderMaintenance() { return underMaintenance; }
    public void setUnderMaintenance(long v) { underMaintenance = v; }
    public long getRetired() { return retired; }
    public void setRetired(long v) { retired = v; }
    public long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(long v) { pendingApprovals = v; }
    public long getOverdueReturns() { return overdueReturns; }
    public void setOverdueReturns(long v) { overdueReturns = v; }
    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long v) { openTickets = v; }
    public long getServiceDue() { return serviceDue; }
    public void setServiceDue(long v) { serviceDue = v; }
    public long getBookingsToday() { return bookingsToday; }
    public void setBookingsToday(long v) { bookingsToday = v; }
    public int getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(int v) { utilizationPercent = v; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getGeneratedTime() {
        return generatedAt == null ? "" : generatedAt.toLocalTime().withNano(0).toString();
    }
    public void setGeneratedAt(LocalDateTime v) { generatedAt = v; }
    public LocalDateTime getLastHousekeeping() { return lastHousekeeping; }
    public void setLastHousekeeping(LocalDateTime v) { lastHousekeeping = v; }
    public List<LabLoad> getLabs() { return labs; }
    public void setLabs(List<LabLoad> v) { labs = v; }
}
