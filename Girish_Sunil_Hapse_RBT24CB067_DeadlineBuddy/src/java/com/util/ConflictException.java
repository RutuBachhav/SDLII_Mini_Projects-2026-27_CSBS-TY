package com.util;

/**
 * The operation no longer applies to the record's current state - a task that
 * was already completed, a double-submitted form, two tabs racing.
 *
 * Every status change is written as a conditional UPDATE; when it matches no
 * row, this is what comes back.
 */
public class ConflictException extends AppException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }
}
