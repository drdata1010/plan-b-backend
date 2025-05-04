package com.planb.supportticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for role assignment requests.
 */
@Data
public class RoleAssignmentRequest {
    
    @NotBlank(message = "Role is required")
    private String role;
}
