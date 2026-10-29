package com.campuscare.security;

import com.campuscare.exception.AppException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

public class AuthContext {

    public static TokenUtil.TokenClaims requireUser(HttpHeaders headers) {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException("Authorization token is missing", Response.Status.UNAUTHORIZED);
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        TokenUtil.TokenClaims claims = TokenUtil.validateToken(token);
        if (claims == null) {
            throw new AppException("Invalid or expired authorization token", Response.Status.UNAUTHORIZED);
        }
        return claims;
    }

    public static TokenUtil.TokenClaims requireRole(HttpHeaders headers, String... allowedRoles) {
        TokenUtil.TokenClaims claims = requireUser(headers);
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(claims.getRole())) {
                return claims;
            }
        }
        throw new AppException("Access forbidden for role: " + claims.getRole(), Response.Status.FORBIDDEN);
    }
}
