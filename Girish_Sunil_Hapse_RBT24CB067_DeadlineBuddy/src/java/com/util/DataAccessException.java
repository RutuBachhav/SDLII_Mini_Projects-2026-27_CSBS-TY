package com.util;

/**
 * Thrown when the database itself fails - unreachable server, bad credentials,
 * a constraint the application did not anticipate. The cause is logged by
 * {@link Jpa}; only this message reaches the browser.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
