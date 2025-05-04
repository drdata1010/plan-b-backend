package com.planb.supportticket.dto;

import lombok.Data;

import java.util.Set;

/**
 * DTO for login responses.
 */
@Data
public class LoginResponse {
    
    private String token;
    private String userId;
    private String email;
    private String displayName;
    private Set<String> roles;
}
