package com.planb.supportticket.dto.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for chat messages exchanged via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessage {

    /**
     * Enum representing different types of chat messages.
     */
    public enum MessageType {
        CHAT,       // Regular chat message
        JOIN,       // User joined notification
        LEAVE,      // User left notification
        TYPING,     // User is typing notification
        AI_REQUEST, // Request to AI model
        AI_RESPONSE, // Response from AI model
        ERROR,      // Error message
        SYSTEM,     // System notification
        USER_JOINED, // User joined notification (for compatibility)
        USER_LEFT    // User left notification (for compatibility)
    }

    /**
     * Unique identifier for the message.
     */
    private String id;

    /**
     * The room/conversation this message belongs to.
     */
    private String roomId;

    /**
     * Type of message.
     */
    private MessageType type;

    /**
     * Content of the message.
     */
    private String content;

    /**
     * Sender of the message.
     */
    private String sender;

    /**
     * Recipient of the message (for private messages).
     */
    private String recipient;

    /**
     * Timestamp when the message was sent.
     */
    private LocalDateTime timestamp;

    /**
     * AI model type for AI_REQUEST and AI_RESPONSE messages.
     */
    private AIModelType aiModelType;

    /**
     * Additional metadata for the message.
     */
    private Map<String, Object> metadata;

    /**
     * Factory method to create a chat message.
     */
    public static ChatMessage createChatMessage(String roomId, String sender, String content) {
        return ChatMessage.builder()
                .id(java.util.UUID.randomUUID().toString())
                .roomId(roomId)
                .type(MessageType.CHAT)
                .content(content)
                .sender(sender)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create a join message.
     */
    public static ChatMessage createJoinMessage(String roomId, String username) {
        return ChatMessage.builder()
                .id(java.util.UUID.randomUUID().toString())
                .roomId(roomId)
                .type(MessageType.JOIN)
                .content(username + " joined the chat")
                .sender(username)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create a leave message.
     */
    public static ChatMessage createLeaveMessage(String roomId, String username) {
        return ChatMessage.builder()
                .id(java.util.UUID.randomUUID().toString())
                .roomId(roomId)
                .type(MessageType.LEAVE)
                .content(username + " left the chat")
                .sender(username)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create an AI request message.
     */
    public static ChatMessage createAIRequestMessage(String roomId, String sender, String content, AIModelType modelType) {
        return ChatMessage.builder()
                .id(java.util.UUID.randomUUID().toString())
                .roomId(roomId)
                .type(MessageType.AI_REQUEST)
                .content(content)
                .sender(sender)
                .aiModelType(modelType)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create an AI response message.
     */
    public static ChatMessage createAIResponseMessage(String roomId, String content, AIModelType modelType, String requestId) {
        ChatMessage message = ChatMessage.builder()
                .id(java.util.UUID.randomUUID().toString())
                .roomId(roomId)
                .type(MessageType.AI_RESPONSE)
                .content(content)
                .sender(modelType.getDisplayName())
                .aiModelType(modelType)
                .timestamp(LocalDateTime.now())
                .build();

        // Add the request ID to the metadata
        message.setMetadata(Map.of("requestId", requestId));

        return message;
    }

    /**
     * Factory method to create an error message.
     */
    public static ChatMessage createErrorMessage(String roomId, String errorMessage) {
        return ChatMessage.builder()
                .id(java.util.UUID.randomUUID().toString())
                .roomId(roomId)
                .type(MessageType.ERROR)
                .content(errorMessage)
                .sender("System")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
