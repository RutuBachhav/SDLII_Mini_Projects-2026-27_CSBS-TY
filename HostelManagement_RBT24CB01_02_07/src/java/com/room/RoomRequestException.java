package com.room;

public class RoomRequestException extends RuntimeException {

    public enum Reason {
        NOT_FOUND,
        FORBIDDEN,
        CONFLICT,
        VALIDATION
    }

    private final Reason reason;
    private final String code;

    public RoomRequestException(Reason reason, String code, String message) {
        super(message);
        this.reason = reason;
        this.code = code;
    }

    public Reason getReason() {
        return reason;
    }

    public String getCode() {
        return code;
    }
}
