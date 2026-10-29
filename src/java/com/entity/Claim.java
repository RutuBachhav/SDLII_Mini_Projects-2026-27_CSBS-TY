package com.entity;

import com.util.Dates;
import java.time.LocalDateTime;

/**
 * An ownership claim against a found-item report.
 *
 * {@code evidence} and {@code contact} are private. They are only ever loaded
 * for the claimant, the reporter of the claimed item, and administrators.
 */
public class Claim {

    private int id;
    private int itemId;
    private String itemTitle;
    private String itemType;
    private int itemReporterId;
    private String itemReporterName;
    private int claimantId;
    private String claimantName;
    private String evidence;
    private String contact;
    private String status = "PENDING";
    private String reviewNote;
    private Integer reviewedBy;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private boolean returnRecorded;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getItemReporterId() { return itemReporterId; }
    public void setItemReporterId(int itemReporterId) { this.itemReporterId = itemReporterId; }

    public String getItemReporterName() { return itemReporterName; }
    public void setItemReporterName(String itemReporterName) { this.itemReporterName = itemReporterName; }

    public int getClaimantId() { return claimantId; }
    public void setClaimantId(int claimantId) { this.claimantId = claimantId; }

    public String getClaimantName() { return claimantName; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReturnRecorded() { return returnRecorded; }
    public void setReturnRecorded(boolean returnRecorded) { this.returnRecorded = returnRecorded; }

    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isApproved() { return "APPROVED".equals(status); }
    public boolean isCompleted() { return "COMPLETED".equals(status); }

    public String getCreatedLabel() { return Dates.dayTime(createdAt); }
    public String getCreatedAgo() { return Dates.ago(createdAt); }
    public String getReviewedLabel() { return Dates.dayTime(reviewedAt); }
}
