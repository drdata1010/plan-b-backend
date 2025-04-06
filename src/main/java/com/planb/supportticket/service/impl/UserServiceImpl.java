package com.planb.supportticket.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.planb.supportticket.dto.UserProfileDTO;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.exception.ResourceNotFoundException;
import com.planb.supportticket.repository.UserProfileRepository;
import com.planb.supportticket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    private final FirebaseAuth firebaseAuth;

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
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
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
        if (userProfileDTO.getPhoneNumber() != null) {
            userProfile.setPhoneNumber(userProfileDTO.getPhoneNumber());
        }
        if (userProfileDTO.getBio() != null) {
            userProfile.setBio(userProfileDTO.getBio());
        }
        
        // Update Firebase user if needed
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid());
            
            if (userProfileDTO.getDisplayName() != null) {
                request.setDisplayName(userProfileDTO.getDisplayName());
            }
            if (userProfileDTO.getPhoneNumber() != null) {
                request.setPhoneNumber(userProfileDTO.getPhoneNumber());
            }
            
            firebaseAuth.updateUser(request);
        } catch (FirebaseAuthException e) {
            log.error("Error updating Firebase user: {}", e.getMessage());
            // Continue with local update even if Firebase update fails
        }
        
        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile updateProfilePicture(UUID id, String profilePictureUrl) {
        UserProfile userProfile = getUserProfileById(id);
        userProfile.setProfilePictureUrl(profilePictureUrl);
        
        // Update Firebase user
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid())
                    .setPhotoUrl(profilePictureUrl);
            firebaseAuth.updateUser(request);
        } catch (FirebaseAuthException e) {
            log.error("Error updating Firebase user photo: {}", e.getMessage());
            // Continue with local update even if Firebase update fails
        }
        
        return userProfileRepository.save(userProfile);
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
    }

    @Override
    public List<UserProfile> getUserProfilesByRole(String role) {
        return userProfileRepository.findByRolesContaining(role);
    }

    @Override
    public UserProfile addRoleToUser(UUID id, String role) {
        UserProfile userProfile = getUserProfileById(id);
        
        // Add role if not already present
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        
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
    public UserProfile removeRoleFromUser(UUID id, String role) {
        UserProfile userProfile = getUserProfileById(id);
        
        // Remove role if present
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        
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
    public Set<String> getUserRoles(UUID id) {
        UserProfile userProfile = getUserProfileById(id);
        return userProfile.getRoles();
    }

    @Override
    public UserProfile disableUserAccount(UUID id) {
        UserProfile userProfile = getUserProfileById(id);
        userProfile.setAccountDisabled(true);
        
        // Disable Firebase user
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid())
                    .setDisabled(true);
            firebaseAuth.updateUser(request);
        } catch (FirebaseAuthException e) {
            log.error("Error disabling Firebase user: {}", e.getMessage());
            // Continue with local update even if Firebase update fails
        }
        
        return userProfileRepository.save(userProfile);
    }

    @Override
    public UserProfile enableUserAccount(UUID id) {
        UserProfile userProfile = getUserProfileById(id);
        userProfile.setAccountDisabled(false);
        
        // Enable Firebase user
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userProfile.getFirebaseUid())
                    .setDisabled(false);
            firebaseAuth.updateUser(request);
        } catch (FirebaseAuthException e) {
            log.error("Error enabling Firebase user: {}", e.getMessage());
            // Continue with local update even if Firebase update fails
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
    public boolean hasRole(UUID id, String role) {
        UserProfile userProfile = getUserProfileById(id);
        
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        
        return userProfile.getRoles().contains(role);
    }

    @Override
    public void deleteUserProfile(UUID id) {
        UserProfile userProfile = getUserProfileById(id);
        
        // Delete Firebase user
        try {
            firebaseAuth.deleteUser(userProfile.getFirebaseUid());
        } catch (FirebaseAuthException e) {
            log.error("Error deleting Firebase user: {}", e.getMessage());
            // Continue with local deletion even if Firebase deletion fails
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
        // Convert roles to simple strings without ROLE_ prefix for Firebase
        List<String> firebaseRoles = new ArrayList<>();
        for (String role : userProfile.getRoles()) {
            if (role.startsWith("ROLE_")) {
                firebaseRoles.add(role.substring(5));
            } else {
                firebaseRoles.add(role);
            }
        }
        
        // Create claims map
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", firebaseRoles);
        
        // Update Firebase custom claims
        firebaseAuth.setCustomUserClaims(userProfile.getFirebaseUid(), claims);
    }
}
