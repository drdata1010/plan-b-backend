package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for user profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private UUID id;
    private String firebaseUid;
    private String email;
    private String displayName;
    private String firstName;
    private String lastName;
    private String mobileNumber;

    // For backward compatibility
    public String getPhoneNumber() {
        return mobileNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mobileNumber = phoneNumber;
    }
    private String profilePictureUrl;
    private String bio;
    private LocalDateTime lastLogin;
    private boolean emailVerified;
    private boolean accountDisabled;
    private Set<String> roles;
}
