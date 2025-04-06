package com.planb.supportticket.repository;

import com.planb.supportticket.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for TicketComment entities.
 */
@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {
    
    /**
     * Finds comments by ticket ID.
     *
     * @param ticketId the ticket ID
     * @return a list of comments
     */
    List<TicketComment> findByTicketId(UUID ticketId);
    
    /**
     * Finds comments by user ID.
     *
     * @param userId the user ID
     * @return a list of comments
     */
    List<TicketComment> findByUserId(UUID userId);
    
    /**
     * Finds replies to a comment.
     *
     * @param parentId the parent comment ID
     * @return a list of replies
     */
    List<TicketComment> findByParentId(UUID parentId);
}
