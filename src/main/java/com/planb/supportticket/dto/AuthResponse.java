package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for authentication responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * The Firebase UID.
     */
    private String uid;
    
    /**
     * The user's email.
     */
    private String email;
    
    /**
     * The user's display name.
     */
    private String displayName;
    
    /**
     * The user's profile ID.
     */
    private UUID profileId;
    
    /**
     * The user's roles.
     */
    private Set<String> roles;
    
    /**
     * Whether the user's email is verified.
     */
    private boolean emailVerified;
}
