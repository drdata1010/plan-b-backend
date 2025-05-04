package com.planb.supportticket.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.planb.supportticket.dto.AuthRequest;
import com.planb.supportticket.dto.AuthResponse;
import com.planb.supportticket.dto.UserProfileDTO;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import com.planb.supportticket.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for authentication operations.
 * Handles Firebase token validation, role assignment, and profile management.
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserService userService;

    @Autowired(required = false)
    private FirebaseAuth firebaseAuth;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Validates a Firebase token and returns user information.
     *
     * @param request the authentication request containing the Firebase token
     * @return the authentication response with user information
     */
    @PostMapping("/validate-token")
    public ResponseEntity<AuthResponse> validateToken(@Valid @RequestBody AuthRequest request) {
        // Check if Firebase is enabled
        if (!firebaseEnabled || firebaseAuth == null) {
            log.warn("Firebase is disabled, creating mock user for token");
            // Create a mock user for development/testing
            String mockUid = "mock-" + Math.abs(request.getToken().hashCode());
            String mockEmail = mockUid + "@example.com";
            String mockDisplayName = "Mock User";

            // Get or create user profile
            UserProfile userProfile;
            try {
                userProfile = userService.getUserProfileByFirebaseUid(mockUid);
                // Update last login
                userService.updateLastLogin(mockUid);
            } catch (Exception e) {
                // User doesn't exist, create a new profile
                userProfile = userService.createUserProfile(mockUid, mockEmail, mockDisplayName);
            }

            // Create response
            AuthResponse response = new AuthResponse();
            response.setUid(mockUid);
            response.setEmail(userProfile.getEmail());
            response.setDisplayName(userProfile.getDisplayName());
            response.setProfileId(userProfile.getId());
            // Convert roles to strings
            Set<String> roleStrings = userProfile.getRoles().stream()
                    .map(UserRole::name)
                    .collect(Collectors.toSet());
            response.setRoles(roleStrings);
            response.setEmailVerified(userProfile.isEmailVerified());

            return ResponseEntity.ok(response);
        }

        try {
            // Verify the Firebase token
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(request.getToken());
            String uid = decodedToken.getUid();

            // Get or create user profile
            UserProfile userProfile;
            try {
                userProfile = userService.getUserProfileByFirebaseUid(uid);
                // Update last login
                userService.updateLastLogin(uid);
            } catch (Exception e) {
                // User doesn't exist, create a new profile
                String email = decodedToken.getEmail();
                String displayName = decodedToken.getName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = email.split("@")[0];
                }

                userProfile = userService.createUserProfile(uid, email, displayName);
            }

            // Create response
            AuthResponse response = new AuthResponse();
            response.setUid(uid);
            response.setEmail(userProfile.getEmail());
            response.setDisplayName(userProfile.getDisplayName());
            response.setProfileId(userProfile.getId());
            // Convert roles to strings
            Set<String> roleStrings = userProfile.getRoles().stream()
                    .map(UserRole::name)
                    .collect(Collectors.toSet());
            response.setRoles(roleStrings);
            response.setEmailVerified(userProfile.isEmailVerified());

            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            log.error("Firebase token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Assigns a role to a user.
     *
     * @param userId the user ID
     * @param role the role to assign
     * @return the updated user profile
     */
    @PostMapping("/roles/{userId}")
    public ResponseEntity<UserProfileDTO> assignRole(
            @PathVariable UUID userId,
            @RequestParam String role) {

        try {
            UserRole userRole = UserRole.valueOf(role);
            UserProfile userProfile = userService.addRoleToUser(userId, userRole);

            UserProfileDTO dto = convertToDTO(userProfile);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", role);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Removes a role from a user.
     *
     * @param userId the user ID
     * @param role the role to remove
     * @return the updated user profile
     */
    @DeleteMapping("/roles/{userId}")
    public ResponseEntity<UserProfileDTO> removeRole(
            @PathVariable UUID userId,
            @RequestParam String role) {

        try {
            UserRole userRole = UserRole.valueOf(role);
            UserProfile userProfile = userService.removeRoleFromUser(userId, userRole);

            UserProfileDTO dto = convertToDTO(userProfile);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", role);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Gets the roles for a user.
     *
     * @param userId the user ID
     * @return the user's roles
     */
    @GetMapping("/roles/{userId}")
    public ResponseEntity<Map<String, Object>> getRoles(@PathVariable UUID userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("roles", userService.getUserRoles(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a user's profile.
     *
     * @param userId the user ID
     * @param profileDTO the updated profile data
     * @return the updated user profile
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UserProfileDTO profileDTO) {

        UserProfile userProfile = userService.updateUserProfile(userId, profileDTO);

        UserProfileDTO dto = convertToDTO(userProfile);
        return ResponseEntity.ok(dto);
    }

    /**
     * Gets a user's profile.
     *
     * @param userId the user ID
     * @return the user profile
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable UUID userId) {
        UserProfile userProfile = userService.getUserProfileById(userId);

        UserProfileDTO dto = convertToDTO(userProfile);
        return ResponseEntity.ok(dto);
    }

    /**
     * Disables a user account.
     *
     * @param userId the user ID
     * @return the updated user profile
     */
    @PostMapping("/disable/{userId}")
    public ResponseEntity<UserProfileDTO> disableAccount(@PathVariable UUID userId) {
        UserProfile userProfile = userService.disableUserAccount(userId);

        UserProfileDTO dto = convertToDTO(userProfile);
        return ResponseEntity.ok(dto);
    }

    /**
     * Enables a user account.
     *
     * @param userId the user ID
     * @return the updated user profile
     */
    @PostMapping("/enable/{userId}")
    public ResponseEntity<UserProfileDTO> enableAccount(@PathVariable UUID userId) {
        UserProfile userProfile = userService.enableUserAccount(userId);

        UserProfileDTO dto = convertToDTO(userProfile);
        return ResponseEntity.ok(dto);
    }

    /**
     * Converts a UserProfile entity to a UserProfileDTO.
     *
     * @param userProfile the user profile entity
     * @return the user profile DTO
     */
    private UserProfileDTO convertToDTO(UserProfile userProfile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(userProfile.getId());
        dto.setFirebaseUid(userProfile.getFirebaseUid());
        dto.setEmail(userProfile.getEmail());
        dto.setDisplayName(userProfile.getDisplayName());
        dto.setFirstName(userProfile.getFirstName());
        dto.setLastName(userProfile.getLastName());
        dto.setPhoneNumber(userProfile.getMobileNumber());
        dto.setProfilePictureUrl(userProfile.getProfilePictureUrl());
        dto.setBio(userProfile.getBio());
        // Convert roles to strings
        Set<String> roleStrings = userProfile.getRoles().stream()
                .map(UserRole::name)
                .collect(Collectors.toSet());
        dto.setRoles(roleStrings);
        dto.setEmailVerified(userProfile.isEmailVerified());
        dto.setAccountDisabled(userProfile.isAccountDisabled());
        return dto;
    }
}
