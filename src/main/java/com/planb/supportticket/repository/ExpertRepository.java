package com.planb.supportticket.repository;

import com.planb.supportticket.entity.Expert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Expert entities.
 */
@Repository
public interface ExpertRepository extends JpaRepository<Expert, UUID> {

    /**
     * Finds an expert by user profile ID.
     *
     * @param userProfileId the user profile ID
     * @return the expert, if found
     */
    Optional<Expert> findByUserProfileId(UUID userProfileId);

    /**
     * Finds available experts with pagination.
     *
     * @param pageable the pagination information
     * @return a page of available experts
     */
    Page<Expert> findByAvailableTrue(Pageable pageable);

    /**
     * Finds experts by technology with pagination.
     *
     * @param technology the technology
     * @param pageable the pagination information
     * @return a page of experts
     */
    @Query("SELECT e FROM Expert e JOIN e.technologies t WHERE t = :technology")
    Page<Expert> findBySpecializationsContaining(String technology, Pageable pageable);

    /**
     * Searches for experts by name, bio, or technology with pagination.
     *
     * @param keyword the search keyword
     * @param pageable the pagination information
     * @return a page of experts
     */
    @Query("SELECT DISTINCT e FROM Expert e JOIN e.userProfile u LEFT JOIN e.technologies t WHERE " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.bio) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Expert> searchExperts(String keyword, Pageable pageable);
}
