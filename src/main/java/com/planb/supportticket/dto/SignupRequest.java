package com.planb.supportticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for signup requests.
 */
@Data
public class SignupRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String country;
    private String customerType; // User or Client
    private String companyName; // Required if customerType is Client
    private String[] preferredTechnologies;
    private String[] preferredModules;
    private String otherPreferences;
}
