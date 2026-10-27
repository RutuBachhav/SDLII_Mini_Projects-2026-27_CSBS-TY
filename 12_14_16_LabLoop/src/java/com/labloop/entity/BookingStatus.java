package com.labloop.entity;

import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {
    PENDING("Pending approval"),
    APPROVED("Approved"),
    CHECKED_OUT("Checked out"),
    RETURNED("Returned"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");

    private final String label;
    BookingStatus(String label) { this.label = label; }
    public String getLabel() { return label; }

    /** Statuses that block the equipment's time slot for anybody else. */
    public static Set<BookingStatus> blockingSlot() {
        return EnumSet.of(PENDING, APPROVED, CHECKED_OUT);
    }
}
