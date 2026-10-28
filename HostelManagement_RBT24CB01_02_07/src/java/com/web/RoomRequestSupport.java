package com.web;

import com.entity.RoomRequestStatus;
import com.room.AvailableRoom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class RoomRequestSupport {

    private RoomRequestSupport() {
    }

    static String requestBadge(RoomRequestStatus status) {
        if (status == null) {
            return "<span class=\"badge badge-blue\">-</span>";
        }
        switch (status) {
            case PENDING:
                return "<span class=\"badge badge-amber\">PENDING</span>";
            case APPROVED:
                return "<span class=\"badge badge-green\">APPROVED</span>";
            case REJECTED:
                return "<span class=\"badge badge-red\">REJECTED</span>";
            case CANCELLED:
                return "<span class=\"badge badge-blue\">CANCELLED</span>";
            default:
                return "<span class=\"badge badge-blue\">"
                        + Layout.esc(status.name()) + "</span>";
        }
    }

    static String availabilityBadge(AvailableRoom availableRoom) {
        if (availableRoom != null && availableRoom.isAvailable()) {
            return "<span class=\"badge badge-green\">AVAILABLE</span>";
        }
        return "<span class=\"badge badge-red\">UNAVAILABLE</span>";
    }

    static String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT)
                .format(date);
    }

    static String money(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    static String successMessage(String code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case "requested":
                return "Your room request was submitted and is waiting for review.";
            case "cancelled":
                return "Your pending room request was cancelled.";
            case "approved":
                return "The request was approved and the room allocation was recorded.";
            case "rejected":
                return "The room request was rejected.";
            case "released":
                return "The room allocation was released and the room is available again.";
            default:
                return null;
        }
    }

    static String errorMessage(String code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case "notfound":
                return "The room or request was not found.";
            case "unavailable":
                return "That room is no longer available. Refresh the results and try another room.";
            case "duplicate":
                return "You already have a pending request for that room.";
            case "allocated":
                return "You already have an approved room allocation.";
            case "inactive":
                return "The requesting account is not active.";
            case "self":
                return "You cannot approve your own room request.";
            case "conflict":
                return "This request changed while it was being processed. Refresh and try again.";
            case "invalid":
                return "Please check the submitted values and try again.";
            case "forbidden":
                return "Your account cannot perform that operation.";
            case "server":
                return "The operation could not be completed. Please try again.";
            default:
                return null;
        }
    }
}
