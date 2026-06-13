package com.sabormayor.common.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Helpers to read the authenticated user from the JWT placed in the
 * SecurityContext by the resource-server filter of each service.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UUID> currentUserId() {
        return currentJwt().map(jwt -> UUID.fromString(jwt.getSubject()));
    }

    public static UUID requireUserId() {
        return currentUserId().orElseThrow(() -> new IllegalStateException("No authenticated user in context"));
    }

    public static Optional<String> currentEmail() {
        return currentJwt().map(jwt -> jwt.getClaimAsString("email"));
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return authorities.contains("ROLE_" + role) || authorities.contains(role);
    }

    public static Optional<Jwt> currentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }
}
