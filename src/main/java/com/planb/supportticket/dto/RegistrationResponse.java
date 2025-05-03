package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for user and expert registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    
    private boolean success;
    private String message;
    private UUID userId;
    private UUID expertId;
    private String email;
    private String customerType;
}
