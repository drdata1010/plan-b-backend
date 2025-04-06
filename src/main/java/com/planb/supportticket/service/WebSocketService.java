package com.planb.supportticket.service;

import com.planb.supportticket.dto.NotificationDTO;
import com.planb.supportticket.entity.ChatMessage;

import java.util.UUID;

/**
 * Service interface for WebSocket operations.
 * Handles real-time messaging and notifications.
 */
public interface WebSocketService {

    /**
     * Sends a notification to a specific user via WebSocket.
     *
     * @param userId the user ID
     * @param notification the notification to send
     */
    void sendNotificationToUser(UUID userId, NotificationDTO notification);

    /**
     * Sends a notification to all users with a specific role via WebSocket.
     *
     * @param role the role
     * @param notification the notification to send
     */
    void sendNotificationToRole(String role, NotificationDTO notification);

    /**
     * Sends a notification to all users via WebSocket.
     *
     * @param notification the notification to send
     */
    void broadcastNotification(NotificationDTO notification);

    /**
     * Sends a chat message to a chat room via WebSocket.
     *
     * @param roomId the chat room ID
     * @param message the chat message
     */
    void sendMessageToRoom(UUID roomId, ChatMessage message);

    /**
     * Sends a chat message to a specific user via WebSocket.
     *
     * @param userId the user ID
     * @param message the chat message
     */
    void sendPrivateMessage(UUID userId, ChatMessage message);

    /**
     * Sends a typing indicator to a chat room via WebSocket.
     *
     * @param roomId the chat room ID
     * @param userId the ID of the user who is typing
     */
    void sendTypingIndicator(UUID roomId, UUID userId);

    /**
     * Sends a system message to a chat room via WebSocket.
     *
     * @param roomId the chat room ID
     * @param content the message content
     */
    void sendSystemMessage(UUID roomId, String content);

    /**
     * Notifies users that a user has joined a chat room.
     *
     * @param roomId the chat room ID
     * @param userId the ID of the user who joined
     */
    void notifyUserJoined(UUID roomId, UUID userId);

    /**
     * Notifies users that a user has left a chat room.
     *
     * @param roomId the chat room ID
     * @param userId the ID of the user who left
     */
    void notifyUserLeft(UUID roomId, UUID userId);
}
