package com.labloop.entity;

import com.labloop.util.Fmt;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/** A reservation of one equipment item for a time slot. */
@Entity
@Table(name = "BOOKING")
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Bookings submitted together from one Practical Kit share the same reference, e.g. KIT-7F3A21. */
    @Column(length = 16)
    private String requestRef;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "EQUIPMENT_ID", nullable = false)
    private Equipment equipment;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "REQUESTED_BY", nullable = false)
    private LabUser requestedBy;

    /** Lab assistant/admin who approved or rejected (null while pending). */
    @ManyToOne
    @JoinColumn(name = "DECIDED_BY")
    private LabUser decidedBy;

    @NotNull(message = "Start time is required")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(nullable = false)
    private LocalDateTime endTime;

    @NotBlank(message = "Purpose is required")
    @Size(min = 5, max = 200, message = "Purpose must be 5-200 characters")
    @Column(nullable = false, length = 200)
    private String purpose;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Size(max = 200)
    @Column(length = 200)
    private String staffRemark;

    private LocalDateTime createdAt;
    private LocalDateTime checkedOutAt;
    private LocalDateTime returnedAt;

    public Booking() { }

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    // ---------- derived ----------
    /** Checked out and the end time has passed without a return. */
    public boolean isOverdue() {
        return status == BookingStatus.CHECKED_OUT && endTime != null && endTime.isBefore(LocalDateTime.now());
    }

    public long getDurationMinutes() {
        return (startTime == null || endTime == null) ? 0 : Duration.between(startTime, endTime).toMinutes();
    }

    @JsonbTransient public String getStartDisplay() { return Fmt.dateTime(startTime); }
    @JsonbTransient public String getEndDisplay() { return Fmt.dateTime(endTime); }
    @JsonbTransient public String getCreatedDisplay() { return Fmt.dateTime(createdAt); }
    @JsonbTransient public String getReturnedDisplay() { return Fmt.dateTime(returnedAt); }

    // ---------- getters / setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestRef() { return requestRef; }
    public void setRequestRef(String requestRef) { this.requestRef = requestRef; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public LabUser getRequestedBy() { return requestedBy; }
    public void setRequestedBy(LabUser requestedBy) { this.requestedBy = requestedBy; }
    public LabUser getDecidedBy() { return decidedBy; }
    public void setDecidedBy(LabUser decidedBy) { this.decidedBy = decidedBy; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public String getStaffRemark() { return staffRemark; }
    public void setStaffRemark(String staffRemark) { this.staffRemark = staffRemark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCheckedOutAt() { return checkedOutAt; }
    public void setCheckedOutAt(LocalDateTime checkedOutAt) { this.checkedOutAt = checkedOutAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Booking)) return false;
        Booking other = (Booking) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public String toString() { return "Booking[" + id + "]"; }
}
