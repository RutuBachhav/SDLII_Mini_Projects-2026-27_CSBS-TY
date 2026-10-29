package com.splitease.split;

import com.splitease.exception.ApiException;

/** Factory: returns the right SplitStrategy object for "EQUAL", "EXACT" or "PERCENT". */
public final class SplitFactory {

    private SplitFactory() { }

    public static SplitStrategy forType(String type) {
        if (type == null) {
            throw ApiException.badRequest("Split type is required");
        }
        switch (type.trim().toUpperCase()) {
            case "EQUAL":
                return new EqualSplit();
            case "EXACT":
                return new ExactSplit();
            case "PERCENT":
                return new PercentSplit();
            default:
                throw ApiException.badRequest("Unknown split type: " + type);
        }
    }
}
