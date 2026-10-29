package com.labloop.entity;

import com.labloop.util.Fmt;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** A bookable, trackable asset: oscilloscope, GPU workstation, robot arm... */
@Entity
@Table(name = "EQUIPMENT")
public class Equipment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Asset tag is required")
    @Pattern(regexp = "LL-[A-Z]{3}-\\d{4}", message = "Asset tag must look like LL-ELC-0042")
    @Column(nullable = false, unique = true, length = 16)
    private String assetTag;

    @NotBlank(message = "Equipment name is required")
    @Size(min = 3, max = 80, message = "Name must be 3-80 characters")
    @Column(nullable = false, length = 80)
    private String name;

    @Size(max = 60)
    @Column(length = 60)
    private String model;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EquipmentCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    @Positive(message = "Purchase cost must be positive")
    private double purchaseCost;

    /** How often the item must be serviced/calibrated. */
    @Min(value = 7, message = "Service interval must be at least 7 days")
    @Max(value = 730, message = "Service interval cannot exceed 730 days")
    private int serviceIntervalDays = 180;

    private LocalDate lastServicedOn;

    @Size(max = 250)
    @Column(length = 250)
    private String notes;

    /** Many Equipment -> one Lab. Creates the LAB_ID foreign key column. */
    @NotNull(message = "Lab is required")
    @ManyToOne
    @JoinColumn(name = "LAB_ID", nullable = false)
    private Lab lab;

    @OneToMany(mappedBy = "equipment")
    @JsonbTransient
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "equipment")
    @JsonbTransient
    private List<MaintenanceTicket> tickets = new ArrayList<>();

    public Equipment() { }

    // ---------- derived values (not stored in DB) ----------

    /** Date the next service/calibration is due. */
    public LocalDate getNextServiceDue() {
        LocalDate base = lastServicedOn != null ? lastServicedOn : purchaseDate;
        return base == null ? null : base.plusDays(serviceIntervalDays);
    }

    public boolean isServiceOverdue() {
        LocalDate due = getNextServiceDue();
        return due != null && due.isBefore(LocalDate.now());
    }

    /** Only AVAILABLE items can take new bookings. */
    public boolean isBookable() { return status == EquipmentStatus.AVAILABLE; }

    @JsonbTransient public String getNextServiceDueDisplay() { return Fmt.date(getNextServiceDue()); }
    @JsonbTransient public String getPurchaseDateDisplay() { return Fmt.date(purchaseDate); }
    @JsonbTransient public String getLastServicedDisplay() { return Fmt.date(lastServicedOn); }

    // ---------- getters / setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public EquipmentCategory getCategory() { return category; }
    public void setCategory(EquipmentCategory category) { this.category = category; }
    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public double getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(double purchaseCost) { this.purchaseCost = purchaseCost; }
    public int getServiceIntervalDays() { return serviceIntervalDays; }
    public void setServiceIntervalDays(int serviceIntervalDays) { this.serviceIntervalDays = serviceIntervalDays; }
    public LocalDate getLastServicedOn() { return lastServicedOn; }
    public void setLastServicedOn(LocalDate lastServicedOn) { this.lastServicedOn = lastServicedOn; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Lab getLab() { return lab; }
    public void setLab(Lab lab) { this.lab = lab; }
    public List<Booking> getBookings() { return bookings; }
    public List<MaintenanceTicket> getTickets() { return tickets; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Equipment)) return false;
        Equipment other = (Equipment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public String toString() { return "Equipment[" + assetTag + "]"; }
}
