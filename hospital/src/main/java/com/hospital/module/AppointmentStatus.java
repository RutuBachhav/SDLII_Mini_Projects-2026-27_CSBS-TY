package com.hospital.module;

/**
 * Appointment lifecycle.
 *
 * Allowed transitions (enforced server-side in HospitalDAO):
 * PENDING   -> CONFIRMED (doctor accepts), REJECTED (doctor declines),
 *              CANCELLED (patient or doctor cancels)
 * CONFIRMED -> COMPLETED (doctor), CANCELLED (patient or doctor)
 * REJECTED, CANCELLED, COMPLETED are terminal.
 */
public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    COMPLETED;

    /** Active bookings block the slot against double-booking. */
    public boolean blocksSlot() {
        return this == PENDING || this == CONFIRMED;
    }

    public boolean isTerminal() {
        return this == REJECTED || this == CANCELLED || this == COMPLETED;
    }
}
