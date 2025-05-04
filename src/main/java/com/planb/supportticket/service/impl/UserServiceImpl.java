package com.planb.supportticket.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.planb.supportticket.dto.UserProfileDTO;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import com.planb.supportticket.exception.ResourceNotFoundException;
import com.planb.supportticket.repository.UserProfileRepository;
import com.planb.supportticket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Implementation of the UserService interface.
 * Handles Firebase user management, profile operations, and role management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;

    @Autowired(required = false)
    private FirebaseAuth firebaseAuth;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Override
    public UserProfile createUserProfile(String firebaseUid, String email, String displayName) {
        // Check if user already exists
        Optional<UserProfile> existingUser = userProfileRepository.findByFirebaseUid(firebaseUid);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create new user profile
        UserProfile userProfile = new UserProfile();
        userProfile.setFirebaseUid(firebaseUid);
        userProfile.setEmail(email);
        userProfile.setDisplayName(displayName);
        userProfile.setLastLogin(LocalDateTime.now());
        userProfile.setEmailVerified(false);
        userProfile.setAccountDisabled(false);

        // Add default role
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.USER);
        userProfile.setRoles(roles);

        // Save and return
        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile getUserProfileById(UUID id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found with id: " + id));
    }

    @Override
    public UserProfile getUserProfileByFirebaseUid(String firebaseUid) {
        return userProfileRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found with Firebase UID: " + firebaseUid));
    }

    @Override
    public UserProfile getUserProfileByEmail(String email) {
        return userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found with email: " + email));
    }

    @Override
    public UserProfile updateUserProfile(UUID id, UserProfileDTO userProfileDTO) {
        UserProfile userProfile = getUserProfileById(id);

        // Update fields
        if (userProfileDTO.getDisplayName() != null) {
            userProfile.setDisplayName(userProfileDTO.getDisplayName());
        }
        if (userProfileDTO.getFirstName() != null) {
            userProfile.setFirstName(userProfileDTO.getFirstName());
        }
        if (userProfileDTO.getLastName() != null) {
            userProfile.setLastName(userProfileDTO.getLastName());
        }
        if (userProfileDTO.getMobileNumber() != null) {
            userProfile.setMobileNumber(userProfileDTO.getMobileNumber());
        }
        if (userProfileDTO.getBio() != null) {
            userProfile.setBio(userProfileDTO.getBio());
        }

        // Update Firebase user if needed
        if (firebaseEnabled && firebaseAuth != null) {
            try {
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid());

                if (userProfileDTO.getDisplayName() != null) {
                    request.setDisplayName(userProfileDTO.getDisplayName());
                }
                if (userProfileDTO.getMobileNumber() != null) {
                    request.setPhoneNumber(userProfileDTO.getMobileNumber());
                }

                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                log.error("Error updating Firebase user: {}", e.getMessage());
                // Continue with local update even if Firebase update fails
            }
        }

        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile updateProfilePicture(UUID id, String profilePictureUrl) {
        UserProfile userProfile = getUserProfileById(id);
        userProfile.setProfilePictureUrl(profilePictureUrl);

        // Update Firebase user
        if (firebaseEnabled && firebaseAuth != null) {
            try {
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid())
                        .setPhotoUrl(profilePictureUrl);
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                log.error("Error updating Firebase user photo: {}", e.getMessage());
                // Continue with local update even if Firebase update fails
            }
        }

        return userProfileRepository.save(userProfile);
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
    }

    @Override
    public List<UserProfile> getUserProfilesByRole(UserRole role) {
        return userProfileRepository.findByRolesContaining(role);
    }

    @Override
    public UserProfile addRoleToUser(UUID id, UserRole role) {
        UserProfile userProfile = getUserProfileById(id);

        // Add role
        userProfile.addRole(role);

        // Update Firebase custom claims
        try {
            updateFirebaseCustomClaims(userProfile);
        } catch (FirebaseAuthException e) {
            log.error("Error updating Firebase custom claims: {}", e.getMessage());
            // Continue with local update even if Firebase update fails
        }

        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile removeRoleFromUser(UUID id, UserRole role) {
        UserProfile userProfile = getUserProfileById(id);

        // Remove role
        userProfile.removeRole(role);

        // Update Firebase custom claims
        try {
            updateFirebaseCustomClaims(userProfile);
        } catch (FirebaseAuthException e) {
            log.error("Error updating Firebase custom claims: {}", e.getMessage());
            // Continue with local update even if Firebase update fails
        }

        return userProfileRepository.save(userProfile);
    }

    @Override
    public Set<UserRole> getUserRoles(UUID id) {
        UserProfile userProfile = getUserProfileById(id);
        return userProfile.getRoles();
    }

    @Override
    public UserProfile disableUserAccount(UUID id) {
        UserProfile userProfile = getUserProfileById(id);
        userProfile.setAccountDisabled(true);

        // Disable Firebase user
        if (firebaseEnabled && firebaseAuth != null) {
            try {
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid())
                        .setDisabled(true);
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                log.error("Error disabling Firebase user: {}", e.getMessage());
                // Continue with local update even if Firebase update fails
            }
        }

        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile enableUserAccount(UUID id) {
        UserProfile userProfile = getUserProfileById(id);
        userProfile.setAccountDisabled(false);

        // Enable Firebase user
        if (firebaseEnabled && firebaseAuth != null) {
            try {
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid())
                        .setDisabled(false);
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                log.error("Error enabling Firebase user: {}", e.getMessage());
                // Continue with local update even if Firebase update fails
            }
        }

        return userProfileRepository.save(userProfile);
    }

    @Override
    public void updateLastLogin(String firebaseUid) {
        try {
            UserProfile userProfile = getUserProfileByFirebaseUid(firebaseUid);
            userProfile.setLastLogin(LocalDateTime.now());
            userProfileRepository.save(userProfile);
        } catch (ResourceNotFoundException e) {
            log.warn("Attempted to update last login for non-existent user: {}", firebaseUid);
        }
    }

    @Override
    public boolean hasRole(UUID id, UserRole role) {
        UserProfile userProfile = getUserProfileById(id);
        return userProfile.getRoles().contains(role);
    }

    @Override
    public void deleteUserProfile(UUID id) {
        UserProfile userProfile = getUserProfileById(id);

        // Delete Firebase user
        if (firebaseEnabled && firebaseAuth != null) {
            try {
                firebaseAuth.deleteUser(userProfile.getFirebaseUid());
            } catch (FirebaseAuthException e) {
                log.error("Error deleting Firebase user: {}", e.getMessage());
                // Continue with local deletion even if Firebase deletion fails
            }
        }

        userProfileRepository.delete(userProfile);
    }

    /**
     * Updates Firebase custom claims with user roles.
     *
     * @param userProfile the user profile
     * @throws FirebaseAuthException if an error occurs
     */
    private void updateFirebaseCustomClaims(UserProfile userProfile) throws FirebaseAuthException {
        // Skip if Firebase is disabled or not available
        if (!firebaseEnabled || firebaseAuth == null) {
            log.debug("Skipping Firebase custom claims update - Firebase is disabled or not available");
            return;
        }

        // Convert roles to simple strings for Firebase
        List<String> firebaseRoles = new ArrayList<>();
        for (UserRole role : userProfile.getRoles()) {
            firebaseRoles.add(role.name());
        }

        // Create claims map
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", firebaseRoles);

        // Update Firebase custom claims
        firebaseAuth.setCustomUserClaims(userProfile.getFirebaseUid(), claims);
    }

    @Override
    public Page<UserProfile> getAllUserProfiles(Pageable pageable) {
        return userProfileRepository.findAll(pageable);
    }

    @Override
    public UserProfileDTO convertToDTO(UserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(userProfile.getId());
        dto.setFirebaseUid(userProfile.getFirebaseUid());
        dto.setEmail(userProfile.getEmail());
        dto.setDisplayName(userProfile.getDisplayName());
        dto.setFirstName(userProfile.getFirstName());
        dto.setLastName(userProfile.getLastName());
        dto.setMobileNumber(userProfile.getMobileNumber());
        dto.setProfilePictureUrl(userProfile.getProfilePictureUrl());
        dto.setBio(userProfile.getBio());
        dto.setLastLogin(userProfile.getLastLogin());
        dto.setEmailVerified(userProfile.isEmailVerified());
        dto.setAccountDisabled(userProfile.isAccountDisabled());

        // Convert roles to strings
        Set<String> roleDTOs = new HashSet<>();
        for (UserRole role : userProfile.getRoles()) {
            roleDTOs.add(role.name());
        }
        dto.setRoles(roleDTOs);

        return dto;
    }
}
