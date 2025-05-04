package com.planb.supportticket.controller;

import com.planb.supportticket.dto.UserProfileDTO;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import com.planb.supportticket.security.jwt.JwtUserDetails;
import com.planb.supportticket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for expert profile management.
 */
@RestController
@RequestMapping("/expert")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('EXPERT')")
public class ExpertProfileController {

    private final UserService userService;

    /**
     * Gets the current expert's profile.
     *
     * @return the expert profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentExpertProfile() {
        try {
            // Get the current user's ID from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId;
            String email;

            if (authentication.getPrincipal() instanceof JwtUserDetails) {
                userId = ((JwtUserDetails) authentication.getPrincipal()).getId();
                email = ((JwtUserDetails) authentication.getPrincipal()).getEmail();
            } else {
                userId = authentication.getName();
                email = "expert@example.com";
            }

            // For testing purposes, create a mock user profile
            UserProfile userProfile = new UserProfile();
            userProfile.setId(UUID.fromString(userId));
            userProfile.setEmail(email);
            userProfile.setDisplayName("Expert User");
            userProfile.setFirstName("Expert");
            userProfile.setLastName("User");

            // Set roles based on email
            Set<UserRole> roles = new HashSet<>();
            roles.add(UserRole.EXPERT);
            userProfile.setRoles(roles);

            // Convert to DTO
            UserProfileDTO userProfileDTO = userService.convertToDTO(userProfile);

            return ResponseEntity.ok(userProfileDTO);
        } catch (Exception e) {
            log.error("Error getting current expert profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Updates the current expert's profile.
     *
     * @param userProfileDTO the updated expert profile
     * @return the updated expert profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentExpertProfile(@RequestBody UserProfileDTO userProfileDTO) {
        try {
            // Get the current user's ID from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId;

            if (authentication.getPrincipal() instanceof JwtUserDetails) {
                userId = ((JwtUserDetails) authentication.getPrincipal()).getId();
            } else {
                userId = authentication.getName();
            }

            // Update the user profile
            UserProfile userProfile = userService.updateUserProfile(UUID.fromString(userId), userProfileDTO);

            // Convert to DTO
            UserProfileDTO updatedUserProfileDTO = userService.convertToDTO(userProfile);

            return ResponseEntity.ok(updatedUserProfileDTO);
        } catch (Exception e) {
            log.error("Error updating current expert profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Sets the expert's availability.
     *
     * @param availability the availability
     * @return the updated expert profile
     */
    @PostMapping("/availability")
    public ResponseEntity<?> setExpertAvailability(@RequestBody Map<String, Object> availability) {
        try {
            // Get the current user's ID from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId;

            if (authentication.getPrincipal() instanceof JwtUserDetails) {
                userId = ((JwtUserDetails) authentication.getPrincipal()).getId();
            } else {
                userId = authentication.getName();
            }

            // TODO: Implement expert availability setting

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Expert availability updated"
            ));
        } catch (Exception e) {
            log.error("Error setting expert availability: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
