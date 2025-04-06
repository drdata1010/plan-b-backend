package com.planb.supportticket.service;

import com.planb.supportticket.dto.ChatMessageDTO;
import com.planb.supportticket.dto.ChatSessionDTO;
import com.planb.supportticket.dto.websocket.AIModelType;
import com.planb.supportticket.entity.Attachment;
import com.planb.supportticket.entity.ChatMessage;
import com.planb.supportticket.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for chat management operations.
 * Handles session management, AI model support, WebSocket integration, and message persistence.
 */
public interface ChatService {

    /**
     * Creates a new chat session.
     *
     * @param chatSessionDTO the chat session data
     * @param userId the ID of the user creating the session
     * @return the created chat session
     */
    ChatSession createChatSession(ChatSessionDTO chatSessionDTO, UUID userId);

    /**
     * Gets a chat session by ID.
     *
     * @param sessionId the chat session ID
     * @return the chat session
     */
    ChatSession getChatSessionById(UUID sessionId);

    /**
     * Gets chat sessions for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> getChatSessionsByUserId(UUID userId, Pageable pageable);

    /**
     * Gets chat sessions for an expert with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> getChatSessionsByExpertId(UUID expertId, Pageable pageable);

    /**
     * Gets chat sessions for a ticket with pagination.
     *
     * @param ticketId the ticket ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> getChatSessionsByTicketId(UUID ticketId, Pageable pageable);

    /**
     * Gets active chat sessions for a user.
     *
     * @param userId the user ID
     * @return a list of active chat sessions
     */
    List<ChatSession> getActiveChatSessionsForUser(UUID userId);

    /**
     * Gets active chat sessions for an expert.
     *
     * @param expertId the expert ID
     * @return a list of active chat sessions
     */
    List<ChatSession> getActiveChatSessionsForExpert(UUID expertId);

    /**
     * Ends a chat session.
     *
     * @param sessionId the chat session ID
     * @return the ended chat session
     */
    ChatSession endChatSession(UUID sessionId);

    /**
     * Adds a message to a chat session.
     *
     * @param sessionId the chat session ID
     * @param messageDTO the message data
     * @param senderId the ID of the user sending the message
     * @return the created chat message
     */
    ChatMessage addMessage(UUID sessionId, ChatMessageDTO messageDTO, UUID senderId);

    /**
     * Gets messages for a chat session with pagination.
     *
     * @param sessionId the chat session ID
     * @param pageable the pagination information
     * @return a page of chat messages
     */
    Page<ChatMessage> getMessagesByChatSessionId(UUID sessionId, Pageable pageable);

    /**
     * Gets a chat message by ID.
     *
     * @param messageId the message ID
     * @return the chat message
     */
    ChatMessage getMessageById(UUID messageId);

    /**
     * Marks a message as read.
     *
     * @param messageId the message ID
     * @return the updated chat message
     */
    ChatMessage markMessageAsRead(UUID messageId);

    /**
     * Adds an attachment to a chat message.
     *
     * @param messageId the message ID
     * @param file the file to attach
     * @param userId the ID of the user adding the attachment
     * @return the created attachment
     */
    Attachment addMessageAttachment(UUID messageId, MultipartFile file, UUID userId);

    /**
     * Gets attachments for a chat message.
     *
     * @param messageId the message ID
     * @return a list of attachments
     */
    List<Attachment> getMessageAttachments(UUID messageId);

    /**
     * Sends a message to an AI model and gets a response.
     *
     * @param sessionId the chat session ID
     * @param messageDTO the message data
     * @param userId the ID of the user sending the message
     * @param modelType the AI model type
     * @return the AI response message
     */
    ChatMessage sendMessageToAI(UUID sessionId, ChatMessageDTO messageDTO, UUID userId, AIModelType modelType);

    /**
     * Creates an AI chat session.
     *
     * @param userId the user ID
     * @param modelType the AI model type
     * @return the created chat session
     */
    ChatSession createAIChatSession(UUID userId, AIModelType modelType);

    /**
     * Gets available AI models.
     *
     * @return a list of available AI model types
     */
    List<AIModelType> getAvailableAIModels();

    /**
     * Gets chat sessions by type with pagination.
     *
     * @param sessionType the session type
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    Page<ChatSession> getChatSessionsByType(ChatSession.ChatSessionType sessionType, Pageable pageable);

    /**
     * Gets unread message count for a user.
     *
     * @param userId the user ID
     * @return the unread message count
     */
    int getUnreadMessageCount(UUID userId);

    /**
     * Gets unread messages for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of unread chat messages
     */
    Page<ChatMessage> getUnreadMessages(UUID userId, Pageable pageable);

    /**
     * Deletes a chat message.
     *
     * @param messageId the message ID
     */
    void deleteMessage(UUID messageId);

    /**
     * Deletes a chat session and all its messages.
     *
     * @param sessionId the chat session ID
     */
    void deleteChatSession(UUID sessionId);

    /**
     * Gets a user profile by ID.
     *
     * @param userId the user ID
     * @return the user profile
     */
    com.planb.supportticket.entity.UserProfile getUserProfileById(UUID userId);
}
