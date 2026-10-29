package com.util;

/**
 * Base class for failures that are the user's business: a bad form value, a
 * record that is not theirs, a workflow step that no longer applies.
 *
 * The message is written for the user and is safe to display. Anything that is
 * not an AppException is treated as a defect and shown as a generic error.
 */
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
