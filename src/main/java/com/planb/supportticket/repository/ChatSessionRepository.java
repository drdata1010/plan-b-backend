package com.planb.supportticket.repository;

import com.planb.supportticket.entity.ChatSession;
import com.planb.supportticket.entity.enums.ChatSessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ChatSession entities.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    /**
     * Finds chat sessions by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> findByUserId(UUID userId, Pageable pageable);

    /**
     * Finds chat sessions by expert ID with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> findByExpertId(UUID expertId, Pageable pageable);

    /**
     * Finds chat sessions by ticket ID with pagination.
     *
     * @param ticketId the ticket ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> findByTicketId(UUID ticketId, Pageable pageable);

    /**
     * Finds active chat sessions for a user.
     *
     * @param userId the user ID
     * @return a list of active chat sessions
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.user.id = :userId AND cs.isActive = true ORDER BY cs.startedAt DESC")
    List<ChatSession> findActiveChatSessionsForUser(UUID userId);

    /**
     * Finds active chat sessions for an expert.
     *
     * @param expertId the expert ID
     * @return a list of active chat sessions
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.expert.id = :expertId AND cs.isActive = true ORDER BY cs.startedAt DESC")
    List<ChatSession> findActiveChatSessionsForExpert(UUID expertId);

    /**
     * Finds chat sessions by type with pagination.
     *
     * @param sessionType the session type
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> findBySessionType(ChatSessionType sessionType, Pageable pageable);
}
