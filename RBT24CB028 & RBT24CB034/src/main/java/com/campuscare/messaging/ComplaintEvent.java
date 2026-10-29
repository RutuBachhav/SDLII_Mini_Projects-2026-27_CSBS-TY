package com.campuscare.messaging;

import java.io.Serializable;
import java.util.Date;

public class ComplaintEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType; // CREATED, ASSIGNED, STATUS_UPDATED, RESOLVED, CLOSED
    private Long complaintId;
    private String complaintTitle;
    private Long targetUserId; // Student or Staff or Admin User ID
    private String message;
    private Date timestamp;

    public ComplaintEvent() {
        this.timestamp = new Date();
    }

    public ComplaintEvent(String eventType, Long complaintId, String complaintTitle, Long targetUserId, String message) {
        this.eventType = eventType;
        this.complaintId = complaintId;
        this.complaintTitle = complaintTitle;
        this.targetUserId = targetUserId;
        this.message = message;
        this.timestamp = new Date();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintTitle() {
        return complaintTitle;
    }

    public void setComplaintTitle(String complaintTitle) {
        this.complaintTitle = complaintTitle;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
