package com.planb.supportticket.service.impl;

import com.planb.supportticket.dto.NotificationDTO;
import com.planb.supportticket.entity.ChatMessage;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import com.planb.supportticket.repository.UserProfileRepository;
import com.planb.supportticket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the WebSocketService interface.
 * Handles real-time messaging and notifications via WebSocket.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserProfileRepository userProfileRepository;

    @Override
    public void sendNotificationToUser(UUID userId, NotificationDTO notification) {
        // Set timestamp if not already set
        if (notification.getTimestamp() == null) {
            notification.setTimestamp(LocalDateTime.now());
        }

        // Set ID if not already set
        if (notification.getId() == null) {
            notification.setId(UUID.randomUUID());
        }

        // Set user ID
        notification.setUserId(userId);

        // Send notification to user's private queue
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );

        log.debug("Sent notification to user {}: {}", userId, notification.getTitle());
    }

    @Override
    public void sendNotificationToRole(String role, NotificationDTO notification) {
        try {
            // Convert string role to UserRole enum
            UserRole userRole = UserRole.valueOf(role);

            // Find all users with the specified role
            List<UserProfile> users = userProfileRepository.findByRolesContaining(userRole);

            // Send notification to each user
            for (UserProfile user : users) {
                sendNotificationToUser(user.getId(), notification);
            }

            log.debug("Sent notification to {} users with role {}: {}", users.size(), role, notification.getTitle());
        } catch (IllegalArgumentException e) {
            log.error("Invalid role: {}", role);
        }
    }

    @Override
    public void broadcastNotification(NotificationDTO notification) {
        // Set timestamp if not already set
        if (notification.getTimestamp() == null) {
            notification.setTimestamp(LocalDateTime.now());
        }

        // Set ID if not already set
        if (notification.getId() == null) {
            notification.setId(UUID.randomUUID());
        }

        // Broadcast notification to all users
        messagingTemplate.convertAndSend("/topic/notifications", notification);

        log.debug("Broadcast notification: {}", notification.getTitle());
    }

    @Override
    public void sendMessageToRoom(UUID roomId, ChatMessage message) {
        // Send message to room topic
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                message
        );

        log.debug("Sent message to room {}: {}", roomId, message.getId());
    }

    @Override
    public void sendPrivateMessage(UUID userId, ChatMessage message) {
        // Send message to user's private queue
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                message
        );

        log.debug("Sent private message to user {}: {}", userId, message.getId());
    }

    @Override
    public void sendTypingIndicator(UUID roomId, UUID userId) {
        // Get user display name
        String displayName = userProfileRepository.findById(userId)
                .map(UserProfile::getDisplayName)
                .orElse("Unknown User");

        // Create typing indicator message
        ChatMessage typingMessage = new ChatMessage();
        typingMessage.setId(UUID.randomUUID());
        typingMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
        typingMessage.setContent(displayName + " is typing...");

        // Send typing indicator to room
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/typing",
                typingMessage
        );

        log.debug("Sent typing indicator for user {} to room {}", userId, roomId);
    }

    @Override
    public void sendSystemMessage(UUID roomId, String content) {
        // Create system message
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setId(UUID.randomUUID());
        systemMessage.setMessageType(ChatMessage.MessageType.SYSTEM);
        systemMessage.setContent(content);

        // Send system message to room
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                systemMessage
        );

        log.debug("Sent system message to room {}: {}", roomId, content);
    }

    @Override
    public void notifyUserJoined(UUID roomId, UUID userId) {
        // Get user display name
        String displayName = userProfileRepository.findById(userId)
                .map(UserProfile::getDisplayName)
                .orElse("Unknown User");

        // Create user joined message
        ChatMessage joinMessage = new ChatMessage();
        joinMessage.setId(UUID.randomUUID());
        joinMessage.setMessageType(ChatMessage.MessageType.USER_JOINED);
        joinMessage.setContent(displayName + " joined the chat");

        // Send user joined message to room
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                joinMessage
        );

        log.debug("Notified that user {} joined room {}", userId, roomId);
    }

    @Override
    public void notifyUserLeft(UUID roomId, UUID userId) {
        // Get user display name
        String displayName = userProfileRepository.findById(userId)
                .map(UserProfile::getDisplayName)
                .orElse("Unknown User");

        // Create user left message
        ChatMessage leftMessage = new ChatMessage();
        leftMessage.setId(UUID.randomUUID());
        leftMessage.setMessageType(ChatMessage.MessageType.USER_LEFT);
        leftMessage.setContent(displayName + " left the chat");

        // Send user left message to room
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                leftMessage
        );

        log.debug("Notified that user {} left room {}", userId, roomId);
    }
}
