package com.security;

/**
 * Every operation that can be authorized by the application.
 */
public enum Permission {
    HOSTEL_READ,
    HOSTEL_CREATE,
    HOSTEL_UPDATE,
    HOSTEL_DELETE,
    ROOM_READ,
    ROOM_CREATE,
    ROOM_UPDATE,
    ROOM_DELETE,
    ROOM_SEARCH,
    ROOM_REQUEST_CREATE,
    ROOM_REQUEST_READ_OWN,
    ROOM_REQUEST_MANAGE,
    ROOM_ALLOCATION_CONFIRM,
    USER_MANAGE
}
