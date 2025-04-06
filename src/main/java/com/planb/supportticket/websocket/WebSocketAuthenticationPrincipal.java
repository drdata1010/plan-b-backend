package com.planb.supportticket.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.UUID;

/**
 * Custom Principal implementation for WebSocket authentication.
 * Holds the user ID and roles for the authenticated user.
 */
@RequiredArgsConstructor
@Getter
public class WebSocketAuthenticationPrincipal implements Principal {

    private final String name;
    private final UUID userId;
    private final String[] roles;

    /**
     * Gets the name of the principal.
     * In this case, it's the user ID as a string.
     *
     * @return the name of the principal
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Checks if the principal has a specific role.
     *
     * @param role the role to check
     * @return true if the principal has the role, false otherwise
     */
    public boolean hasRole(String role) {
        if (roles == null) {
            return false;
        }
        
        for (String r : roles) {
            if (r.equals(role)) {
                return true;
            }
        }
        
        return false;
    }
}
