package com.planb.supportticket.repository;

import com.planb.supportticket.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for ChatMessage entities.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Finds messages by chat session ID with pagination.
     *
     * @param chatSessionId the chat session ID
     * @param pageable the pagination information
     * @return a page of chat messages
     */
    Page<ChatMessage> findByChatSessionId(UUID chatSessionId, Pageable pageable);

    /**
     * Finds messages by sender ID with pagination.
     *
     * @param senderId the sender ID
     * @param pageable the pagination information
     * @return a page of chat messages
     */
    Page<ChatMessage> findBySenderId(UUID senderId, Pageable pageable);

    /**
     * Gets unread message count for a user.
     *
     * @param userId the user ID
     * @return the unread message count
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m JOIN m.chatSession cs " +
           "WHERE (cs.user.id = :userId OR (cs.expert IS NOT NULL AND cs.expert.userProfile.id = :userId)) " +
           "AND m.sender.id != :userId AND m.isRead = false")
    int getUnreadMessageCount(UUID userId);

    /**
     * Gets unread messages for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of unread chat messages
     */
    @Query("SELECT m FROM ChatMessage m JOIN m.chatSession cs " +
           "WHERE (cs.user.id = :userId OR (cs.expert IS NOT NULL AND cs.expert.userProfile.id = :userId)) " +
           "AND m.sender.id != :userId AND m.isRead = false " +
           "ORDER BY m.createdAt DESC")
    Page<ChatMessage> getUnreadMessages(UUID userId, Pageable pageable);
}
