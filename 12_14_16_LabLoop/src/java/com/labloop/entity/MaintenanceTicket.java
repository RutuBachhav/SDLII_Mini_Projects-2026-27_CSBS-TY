package com.labloop.entity;

import com.labloop.util.Fmt;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/** A fault report / repair job raised against one equipment item. */
@Entity
@Table(name = "MAINTENANCE_TICKET")
public class MaintenanceTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "EQUIPMENT_ID", nullable = false)
    private Equipment equipment;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "REPORTED_BY", nullable = false)
    private LabUser reportedBy;

    @NotBlank(message = "Describe the issue")
    @Size(min = 5, max = 250, message = "Issue must be 5-250 characters")
    @Column(nullable = false, length = 250)
    private String issue;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TicketSeverity severity = TicketSeverity.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TicketStatus status = TicketStatus.OPEN;

    @Size(max = 250)
    @Column(length = 250)
    private String resolutionNotes;

    @PositiveOrZero(message = "Repair cost cannot be negative")
    private double repairCost;

    private LocalDateTime openedAt;
    private LocalDateTime resolvedAt;

    public MaintenanceTicket() { }

    @PrePersist
    void onCreate() { if (openedAt == null) openedAt = LocalDateTime.now(); }

    @JsonbTransient public String getOpenedDisplay() { return Fmt.dateTime(openedAt); }
    @JsonbTransient public String getResolvedDisplay() { return Fmt.dateTime(resolvedAt); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public LabUser getReportedBy() { return reportedBy; }
    public void setReportedBy(LabUser reportedBy) { this.reportedBy = reportedBy; }
    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }
    public TicketSeverity getSeverity() { return severity; }
    public void setSeverity(TicketSeverity severity) { this.severity = severity; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public double getRepairCost() { return repairCost; }
    public void setRepairCost(double repairCost) { this.repairCost = repairCost; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MaintenanceTicket)) return false;
        MaintenanceTicket other = (MaintenanceTicket) o;
        return id != null && id.equals(other.id);
    }
}
