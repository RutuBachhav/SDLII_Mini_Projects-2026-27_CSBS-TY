package com.entity;

import com.util.Dates;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A report. One class covers both sides of the board; {@code type} is LOST or
 * FOUND.
 *
 * {@code privateMarks} is the detail a genuine owner should be able to state.
 * It is loaded for everyone but the JSPs only render it when
 * {@code canSeePrivate} is true, which the servlet decides.
 */
public class Item {

    private int id;
    private int reporterId;
    private String reporterName;
    private String type;
    private String title;
    private int categoryId;
    private String categoryName;
    private String description;
    private String color;
    private String privateMarks;
    private String location;
    private LocalDate itemDate;
    private String status = "OPEN";
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer photoId;
    private int openClaimCount;
    private boolean canSeePrivate;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getPrivateMarks() { return privateMarks; }
    public void setPrivateMarks(String privateMarks) { this.privateMarks = privateMarks; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getItemDate() { return itemDate; }
    public void setItemDate(LocalDate itemDate) { this.itemDate = itemDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public Integer getPhotoId() { return photoId; }
    public void setPhotoId(Integer photoId) { this.photoId = photoId; }

    public int getOpenClaimCount() { return openClaimCount; }
    public void setOpenClaimCount(int openClaimCount) { this.openClaimCount = openClaimCount; }

    public boolean isCanSeePrivate() { return canSeePrivate; }
    public void setCanSeePrivate(boolean canSeePrivate) { this.canSeePrivate = canSeePrivate; }

    public boolean isLost() { return "LOST".equals(type); }
    public boolean isFound() { return "FOUND".equals(type); }
    public boolean isOpen() { return "OPEN".equals(status); }
    public boolean isHasPhoto() { return photoId != null; }

    public String getItemDateLabel() { return Dates.day(itemDate); }
    public String getCreatedLabel() { return Dates.dayTime(createdAt); }
    public String getCreatedAgo() { return Dates.ago(createdAt); }
    public String getResolvedLabel() { return Dates.dayTime(resolvedAt); }
}
