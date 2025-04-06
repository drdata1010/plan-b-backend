package com.planb.supportticket.repository;

import com.planb.supportticket.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Attachment entities.
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    
    /**
     * Finds attachments by ticket ID.
     *
     * @param ticketId the ticket ID
     * @return a list of attachments
     */
    List<Attachment> findByTicketId(UUID ticketId);
    
    /**
     * Finds attachments by comment ID.
     *
     * @param commentId the comment ID
     * @return a list of attachments
     */
    List<Attachment> findByCommentId(UUID commentId);
    
    /**
     * Finds attachments by chat message ID.
     *
     * @param messageId the chat message ID
     * @return a list of attachments
     */
    List<Attachment> findByMessageId(UUID messageId);
    
    /**
     * Finds attachments by user ID.
     *
     * @param userId the user ID
     * @return a list of attachments
     */
    List<Attachment> findByUserId(UUID userId);
}
