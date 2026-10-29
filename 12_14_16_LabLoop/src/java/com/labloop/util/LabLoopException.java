package com.labloop.util;

import jakarta.ejb.ApplicationException;
import java.util.Collections;
import java.util.List;

/**
 * Business-rule failure (validation error, double booking, permission...).
 * @ApplicationException(rollback = true): the EJB container rolls the transaction back
 * but passes this exception to the caller unchanged, so servlets and REST can show the messages.
 */
@ApplicationException(rollback = true)
public class LabLoopException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public enum Kind { VALIDATION, CONFLICT, NOT_FOUND, FORBIDDEN }

    private final Kind kind;
    private final List<String> messages;

    public LabLoopException(Kind kind, List<String> messages) {
        super(String.join("; ", messages));
        this.kind = kind;
        this.messages = Collections.unmodifiableList(messages);
    }

    public static LabLoopException validation(List<String> msgs) { return new LabLoopException(Kind.VALIDATION, msgs); }
    public static LabLoopException validation(String msg) { return new LabLoopException(Kind.VALIDATION, List.of(msg)); }
    public static LabLoopException conflict(String msg) { return new LabLoopException(Kind.CONFLICT, List.of(msg)); }
    public static LabLoopException notFound(String msg) { return new LabLoopException(Kind.NOT_FOUND, List.of(msg)); }
    public static LabLoopException forbidden(String msg) { return new LabLoopException(Kind.FORBIDDEN, List.of(msg)); }

    public Kind getKind() { return kind; }
    public List<String> getMessages() { return messages; }
}
