package com.planb.supportticket.controller;

import com.planb.supportticket.dto.UserProfileDTO;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for user operations.
 * Handles user profile management and role operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    
    /**
     * Gets all user profiles.
     *
     * @return a list of user profiles
     */
    @GetMapping
    public ResponseEntity<List<UserProfileDTO>> getAllUserProfiles() {
        List<UserProfile> userProfiles = userService.getAllUserProfiles();
        
        List<UserProfileDTO> response = userProfiles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets a user profile by ID.
     *
     * @param id the user profile ID
     * @return the user profile
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfileById(@PathVariable UUID id) {
        UserProfile userProfile = userService.getUserProfileById(id);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets a user profile by Firebase UID.
     *
     * @param firebaseUid the Firebase UID
     * @return the user profile
     */
    @GetMapping("/firebase/{firebaseUid}")
    public ResponseEntity<UserProfileDTO> getUserProfileByFirebaseUid(@PathVariable String firebaseUid) {
        UserProfile userProfile = userService.getUserProfileByFirebaseUid(firebaseUid);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets a user profile by email.
     *
     * @param email the user's email
     * @return the user profile
     */
    @GetMapping("/email")
    public ResponseEntity<UserProfileDTO> getUserProfileByEmail(@RequestParam String email) {
        UserProfile userProfile = userService.getUserProfileByEmail(email);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates a user profile.
     *
     * @param id the user profile ID
     * @param userProfileDTO the updated user profile data
     * @return the updated user profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileDTO userProfileDTO) {
        
        UserProfile userProfile = userService.updateUserProfile(id, userProfileDTO);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates a user's profile picture.
     *
     * @param id the user profile ID
     * @param file the profile picture file
     * @return the updated user profile
     */
    @PostMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDTO> updateProfilePicture(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        
        // In a real implementation, you would upload the file to S3 and get the URL
        String profilePictureUrl = "https://example.com/profile-pictures/" + id.toString() + ".jpg";
        
        UserProfile userProfile = userService.updateProfilePicture(id, profilePictureUrl);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets user profiles by role.
     *
     * @param role the role to filter by
     * @return a list of user profiles
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserProfileDTO>> getUserProfilesByRole(@PathVariable String role) {
        List<UserProfile> userProfiles = userService.getUserProfilesByRole(role);
        
        List<UserProfileDTO> response = userProfiles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Adds a role to a user.
     *
     * @param id the user profile ID
     * @param role the role to add
     * @return the updated user profile
     */
    @PostMapping("/{id}/roles")
    public ResponseEntity<UserProfileDTO> addRoleToUser(
            @PathVariable UUID id,
            @RequestParam String role) {
        
        UserProfile userProfile = userService.addRoleToUser(id, role);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Removes a role from a user.
     *
     * @param id the user profile ID
     * @param role the role to remove
     * @return the updated user profile
     */
    @DeleteMapping("/{id}/roles")
    public ResponseEntity<UserProfileDTO> removeRoleFromUser(
            @PathVariable UUID id,
            @RequestParam String role) {
        
        UserProfile userProfile = userService.removeRoleFromUser(id, role);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets the roles for a user.
     *
     * @param id the user profile ID
     * @return the set of roles for the user
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable UUID id) {
        Set<String> roles = userService.getUserRoles(id);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Disables a user account.
     *
     * @param id the user profile ID
     * @return the updated user profile
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<UserProfileDTO> disableUserAccount(@PathVariable UUID id) {
        UserProfile userProfile = userService.disableUserAccount(id);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enables a user account.
     *
     * @param id the user profile ID
     * @return the updated user profile
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<UserProfileDTO> enableUserAccount(@PathVariable UUID id) {
        UserProfile userProfile = userService.enableUserAccount(id);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Checks if a user has a specific role.
     *
     * @param id the user profile ID
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    @GetMapping("/{id}/has-role")
    public ResponseEntity<Boolean> hasRole(
            @PathVariable UUID id,
            @RequestParam String role) {
        
        boolean hasRole = userService.hasRole(id, role);
        return ResponseEntity.ok(hasRole);
    }
    
    /**
     * Gets the current user's profile.
     *
     * @param userDetails the authenticated user
     * @return the user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = getUserIdFromUserDetails(userDetails);
        UserProfile userProfile = userService.getUserProfileById(userId);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates the current user's profile.
     *
     * @param userProfileDTO the updated user profile data
     * @param userDetails the authenticated user
     * @return the updated user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileDTO userProfileDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = getUserIdFromUserDetails(userDetails);
        UserProfile userProfile = userService.updateUserProfile(userId, userProfileDTO);
        
        UserProfileDTO response = convertToDTO(userProfile);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deletes a user profile.
     *
     * @param id the user profile ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable UUID id) {
        userService.deleteUserProfile(id);
        return ResponseEntity.noContent().build();
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
        dto.setPhoneNumber(userProfile.getPhoneNumber());
        dto.setProfilePictureUrl(userProfile.getProfilePictureUrl());
        dto.setBio(userProfile.getBio());
        dto.setLastLogin(userProfile.getLastLogin());
        dto.setEmailVerified(userProfile.isEmailVerified());
        dto.setAccountDisabled(userProfile.isAccountDisabled());
        dto.setRoles(userProfile.getRoles());
        return dto;
    }
    
    /**
     * Gets the user ID from the UserDetails.
     *
     * @param userDetails the user details
     * @return the user ID
     */
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // In a real implementation, you would extract the user ID from the UserDetails
        // This is a placeholder implementation
        return UUID.fromString(userDetails.getUsername());
    }
}
