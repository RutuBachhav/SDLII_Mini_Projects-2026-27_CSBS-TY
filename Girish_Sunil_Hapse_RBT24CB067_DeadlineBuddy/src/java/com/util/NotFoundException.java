package com.util;

/**
 * The record does not exist, or it exists but belongs to another user.
 *
 * Both cases deliberately produce the same exception and the same 404: telling
 * one user that task 41 exists but is someone else's is already a leak.
 */
public class NotFoundException extends AppException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
