package com.planb.supportticket.service;

import com.planb.supportticket.dto.UserProfileDTO;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for user management operations.
 * Handles Firebase user management, profile operations, and role management.
 */
public interface UserService {

    /**
     * Creates a new user profile from Firebase user data.
     *
     * @param firebaseUid the Firebase UID
     * @param email the user's email
     * @param displayName the user's display name
     * @return the created user profile
     */
    UserProfile createUserProfile(String firebaseUid, String email, String displayName);

    /**
     * Gets a user profile by ID.
     *
     * @param id the user profile ID
     * @return the user profile
     */
    UserProfile getUserProfileById(UUID id);

    /**
     * Gets a user profile by Firebase UID.
     *
     * @param firebaseUid the Firebase UID
     * @return the user profile
     */
    UserProfile getUserProfileByFirebaseUid(String firebaseUid);

    /**
     * Gets a user profile by email.
     *
     * @param email the user's email
     * @return the user profile
     */
    UserProfile getUserProfileByEmail(String email);

    /**
     * Updates a user profile.
     *
     * @param id the user profile ID
     * @param userProfileDTO the updated user profile data
     * @return the updated user profile
     */
    UserProfile updateUserProfile(UUID id, UserProfileDTO userProfileDTO);

    /**
     * Updates a user's profile picture.
     *
     * @param id the user profile ID
     * @param profilePictureUrl the profile picture URL
     * @return the updated user profile
     */
    UserProfile updateProfilePicture(UUID id, String profilePictureUrl);

    /**
     * Gets all user profiles.
     *
     * @return a list of all user profiles
     */
    List<UserProfile> getAllUserProfiles();

    /**
     * Gets all user profiles with pagination.
     *
     * @param pageable the pagination information
     * @return a page of user profiles
     */
    Page<UserProfile> getAllUserProfiles(Pageable pageable);

    /**
     * Gets all user profiles with a specific role.
     *
     * @param role the role to filter by
     * @return a list of user profiles with the specified role
     */
    List<UserProfile> getUserProfilesByRole(UserRole role);

    /**
     * Adds a role to a user.
     *
     * @param id the user profile ID
     * @param role the role to add
     * @return the updated user profile
     */
    UserProfile addRoleToUser(UUID id, UserRole role);

    /**
     * Removes a role from a user.
     *
     * @param id the user profile ID
     * @param role the role to remove
     * @return the updated user profile
     */
    UserProfile removeRoleFromUser(UUID id, UserRole role);

    /**
     * Gets the roles for a user.
     *
     * @param id the user profile ID
     * @return the set of roles for the user
     */
    Set<UserRole> getUserRoles(UUID id);

    /**
     * Disables a user account.
     *
     * @param id the user profile ID
     * @return the updated user profile
     */
    UserProfile disableUserAccount(UUID id);

    /**
     * Enables a user account.
     *
     * @param id the user profile ID
     * @return the updated user profile
     */
    UserProfile enableUserAccount(UUID id);

    /**
     * Updates a user's last login time.
     *
     * @param firebaseUid the Firebase UID
     */
    void updateLastLogin(String firebaseUid);

    /**
     * Checks if a user has a specific role.
     *
     * @param id the user profile ID
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    boolean hasRole(UUID id, UserRole role);

    /**
     * Deletes a user profile.
     *
     * @param id the user profile ID
     */
    void deleteUserProfile(UUID id);

    /**
     * Converts a UserProfile entity to a UserProfileDTO.
     *
     * @param userProfile the user profile entity
     * @return the user profile DTO
     */
    UserProfileDTO convertToDTO(UserProfile userProfile);
}
