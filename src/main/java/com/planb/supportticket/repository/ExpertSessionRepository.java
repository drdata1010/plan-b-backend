package com.planb.supportticket.repository;

import com.planb.supportticket.entity.ExpertSession;
import com.planb.supportticket.entity.enums.ExpertSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ExpertSession entities.
 */
@Repository
public interface ExpertSessionRepository extends JpaRepository<ExpertSession, UUID> {
    
    /**
     * Finds expert sessions by expert ID with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of expert sessions
     */
    Page<ExpertSession> findByExpertId(UUID expertId, Pageable pageable);
    
    /**
     * Finds expert sessions by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of expert sessions
     */
    Page<ExpertSession> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Finds expert sessions by status with pagination.
     *
     * @param status the expert session status
     * @param pageable the pagination information
     * @return a page of expert sessions
     */
    Page<ExpertSession> findByStatus(ExpertSessionStatus status, Pageable pageable);
    
    /**
     * Finds upcoming expert sessions for an expert.
     *
     * @param expertId the expert ID
     * @param now the current date and time
     * @return a list of upcoming expert sessions
     */
    @Query("SELECT e FROM ExpertSession e WHERE e.expert.id = :expertId AND e.scheduledAt > :now AND e.status = 'SCHEDULED' ORDER BY e.scheduledAt ASC")
    List<ExpertSession> findUpcomingSessionsForExpert(UUID expertId, LocalDateTime now);
    
    /**
     * Finds upcoming expert sessions for a user.
     *
     * @param userId the user ID
     * @param now the current date and time
     * @return a list of upcoming expert sessions
     */
    @Query("SELECT e FROM ExpertSession e WHERE e.user.id = :userId AND e.scheduledAt > :now AND e.status = 'SCHEDULED' ORDER BY e.scheduledAt ASC")
    List<ExpertSession> findUpcomingSessionsForUser(UUID userId, LocalDateTime now);
}
