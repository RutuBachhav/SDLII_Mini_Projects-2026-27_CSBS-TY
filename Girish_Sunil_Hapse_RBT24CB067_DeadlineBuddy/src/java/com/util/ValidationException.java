package com.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carries per-field messages back to the form that produced them, so the page
 * can be redisplayed with the user's input intact and each problem shown next
 * to the field it belongs to.
 */
public class ValidationException extends AppException {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super(summarise(fieldErrors));
        this.fieldErrors = Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public ValidationException(String field, String message) {
        this(one(field, message));
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    private static Map<String, String> one(String field, String message) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(field, message);
        return m;
    }

    private static String summarise(Map<String, String> errors) {
        if (errors.size() == 1) {
            return errors.values().iterator().next();
        }
        return "Please correct the " + errors.size() + " highlighted fields.";
    }
}
