package com.security;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Central permission matrix. Unknown or missing roles deny by default.
 */
public final class AuthorizationService {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_WARDEN = "WARDEN";
    public static final String ROLE_VIEWER = "VIEWER";

    private static final Map<String, Set<Permission>> MATRIX;

    static {
        Map<String, Set<Permission>> matrix = new HashMap<>();

        matrix.put(ROLE_ADMIN, Collections.unmodifiableSet(
                EnumSet.allOf(Permission.class)));

        matrix.put(ROLE_WARDEN, Collections.unmodifiableSet(EnumSet.of(
                Permission.HOSTEL_READ,
                Permission.ROOM_READ,
                Permission.ROOM_CREATE,
                Permission.ROOM_UPDATE,
                Permission.ROOM_SEARCH,
                Permission.ROOM_REQUEST_CREATE,
                Permission.ROOM_REQUEST_READ_OWN,
                Permission.ROOM_REQUEST_MANAGE,
                Permission.ROOM_ALLOCATION_CONFIRM)));

        matrix.put(ROLE_VIEWER, Collections.unmodifiableSet(EnumSet.of(
                Permission.HOSTEL_READ,
                Permission.ROOM_READ,
                Permission.ROOM_SEARCH,
                Permission.ROOM_REQUEST_CREATE,
                Permission.ROOM_REQUEST_READ_OWN)));

        MATRIX = Collections.unmodifiableMap(matrix);
    }

    private AuthorizationService() {
    }

    public static boolean allows(String role, Permission permission) {
        if (role == null || permission == null) {
            return false;
        }
        Set<Permission> permissions = MATRIX.get(
                role.trim().toUpperCase(Locale.ROOT));
        return permissions != null && permissions.contains(permission);
    }

    public static boolean allowsAny(Collection<String> roles,
                                    Permission permission) {
        if (roles == null || permission == null) {
            return false;
        }
        for (String role : roles) {
            if (allows(role, permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validRole(String role) {
        return role != null
                && MATRIX.containsKey(role.trim().toUpperCase(Locale.ROOT));
    }

    public static String highestRole(Collection<String> roles) {
        if (roles == null) {
            return null;
        }
        for (String candidate : List.of(ROLE_ADMIN, ROLE_WARDEN, ROLE_VIEWER)) {
            for (String role : roles) {
                if (candidate.equalsIgnoreCase(role)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public static Set<Permission> permissionsFor(String role) {
        if (role == null) {
            return Collections.emptySet();
        }
        Set<Permission> permissions = MATRIX.get(
                role.trim().toUpperCase(Locale.ROOT));
        return permissions == null ? Collections.emptySet() : permissions;
    }
}
