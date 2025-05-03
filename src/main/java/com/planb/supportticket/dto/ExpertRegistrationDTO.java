package com.planb.supportticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Data Transfer Object for expert registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertRegistrationDTO {
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be less than 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Mobile number must be valid")
    private String mobileNumber;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).*$", 
             message = "Password must contain at least one letter, one number, and one special character")
    private String password;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;
    
    @NotBlank(message = "At least one technology/domain is required")
    private Set<String> technologies;
    
    private Set<String> modules;
    
    @Size(max = 500, message = "Other expertise must be less than 500 characters")
    private String otherExpertise;
    
    @NotBlank(message = "Years of experience is required")
    @Pattern(regexp = "^(0-1|2-4|5-8|9-12|12\\+)$", message = "Years of experience must be one of: 0-1, 2-4, 5-8, 9-12, 12+")
    private String yearsOfExperience;
    
    @NotBlank(message = "Job role is required")
    @Size(max = 255, message = "Job role must be less than 255 characters")
    private String jobRole;
    
    @Pattern(regexp = "^(Within 2 hours|Within 5 hours|Within 8 hours)$", 
             message = "Response time must be one of: Within 2 hours, Within 5 hours, Within 8 hours")
    private String responseTime;
    
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", 
             message = "LinkedIn profile must be a valid URL")
    private String linkedinProfile;
    
    private boolean acceptTerms;
}
