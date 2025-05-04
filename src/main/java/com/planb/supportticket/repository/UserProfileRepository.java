package com.planb.supportticket.repository;

import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserProfile entities.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Finds a user profile by Firebase UID.
     *
     * @param firebaseUid the Firebase UID
     * @return the user profile, if found
     */
    Optional<UserProfile> findByFirebaseUid(String firebaseUid);

    /**
     * Finds a user profile by email.
     *
     * @param email the email
     * @return the user profile, if found
     */
    Optional<UserProfile> findByEmail(String email);

    /**
     * Finds user profiles by role.
     *
     * @param role the role
     * @return a list of user profiles with the specified role
     */
    List<UserProfile> findByRolesContaining(UserRole role);
}
