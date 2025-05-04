package com.planb.supportticket.controller;

import com.planb.supportticket.dto.RoleAssignmentRequest;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import com.planb.supportticket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Controller for managing user roles.
 */
@RestController
@RequestMapping("/role-management")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class RoleManagementController {

    private final UserService userService;

    /**
     * Gets the roles for a user.
     *
     * @param userId the user ID
     * @return the roles
     */
    @GetMapping("/{userId}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable UUID userId) {
        try {
            Set<UserRole> roles = userService.getUserRoles(userId);
            return ResponseEntity.ok(Map.of("roles", roles));
        } catch (Exception e) {
            log.error("Error getting user roles: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Adds a role to a user.
     *
     * @param userId the user ID
     * @param request the role assignment request
     * @return the updated user
     */
    @PostMapping("/{userId}/roles")
    public ResponseEntity<?> addRoleToUser(
            @PathVariable UUID userId,
            @RequestBody RoleAssignmentRequest request) {
        try {
            UserRole role = UserRole.valueOf(request.getRole());
            UserProfile userProfile = userService.addRoleToUser(userId, role);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Role " + role + " added to user",
                    "roles", userProfile.getRoles()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", request.getRole());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + request.getRole()));
        } catch (Exception e) {
            log.error("Error adding role to user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Removes a role from a user.
     *
     * @param userId the user ID
     * @param request the role assignment request
     * @return the updated user
     */
    @DeleteMapping("/{userId}/roles")
    public ResponseEntity<?> removeRoleFromUser(
            @PathVariable UUID userId,
            @RequestBody RoleAssignmentRequest request) {
        try {
            UserRole role = UserRole.valueOf(request.getRole());
            UserProfile userProfile = userService.removeRoleFromUser(userId, role);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Role " + role + " removed from user",
                    "roles", userProfile.getRoles()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", request.getRole());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + request.getRole()));
        } catch (Exception e) {
            log.error("Error removing role from user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
