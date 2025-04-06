package com.planb.supportticket.controller.websocket;

import com.planb.supportticket.dto.websocket.ChatMessage;
import com.planb.supportticket.dto.websocket.ChatRoom;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for chat messages.
 * Manages chat rooms and message routing.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;
    
    // In-memory store of active chat rooms (in a real app, use a persistent store)
    private final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

    /**
     * Handles messages sent to a specific chat room.
     * 
     * @param roomId the room ID
     * @param message the chat message
     * @param headerAccessor the message headers
     * @param principal the authenticated user
     * @return the processed message to be broadcast to subscribers
     */
    @MessageMapping("/chat.room/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                  @Payload ChatMessage message,
                                  SimpMessageHeaderAccessor headerAccessor,
                                  Principal principal) {
        
        String username = principal.getName();
        log.debug("Received message from user {} in room {}: {}", username, roomId, message.getContent());
        
        // Validate that the room exists
        ChatRoom room = chatRooms.get(roomId);
        if (room == null) {
            log.warn("User {} attempted to send message to non-existent room {}", username, roomId);
            throw new IllegalArgumentException("Chat room does not exist: " + roomId);
        }
        
        // Validate that the user is a participant in the room
        if (!room.hasParticipant(username)) {
            log.warn("User {} attempted to send message to room {} without being a participant", username, roomId);
            throw new IllegalArgumentException("You are not a participant in this chat room");
        }
        
        // Set sender and timestamp if not already set
        if (message.getSender() == null) {
            message.setSender(username);
        }
        
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        
        // Set room ID if not already set
        if (message.getRoomId() == null) {
            message.setRoomId(roomId);
        }
        
        // Set message ID if not already set
        if (message.getId() == null) {
            message.setId(java.util.UUID.randomUUID().toString());
        }
        
        // In a real application, you would persist the message here
        
        return message;
    }
    
    /**
     * Handles private messages between users.
     * 
     * @param message the chat message
     * @param principal the authenticated user
     */
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage message, Principal principal) {
        String sender = principal.getName();
        String recipient = message.getRecipient();
        
        if (recipient == null || recipient.isEmpty()) {
            log.warn("User {} attempted to send private message without recipient", sender);
            sendErrorMessage(sender, "Recipient is required for private messages");
            return;
        }
        
        log.debug("Received private message from {} to {}: {}", sender, recipient, message.getContent());
        
        // Set sender and timestamp if not already set
        if (message.getSender() == null) {
            message.setSender(sender);
        }
        
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        
        // Set message ID if not already set
        if (message.getId() == null) {
            message.setId(java.util.UUID.randomUUID().toString());
        }
        
        // Send to recipient
        messagingTemplate.convertAndSendToUser(
            recipient,
            "/queue/private",
            message
        );
        
        // Also send a copy to the sender
        messagingTemplate.convertAndSendToUser(
            sender,
            "/queue/private",
            message
        );
        
        // In a real application, you would persist the message here
    }
    
    /**
     * Handles user join events for chat rooms.
     * 
     * @param roomId the room ID
     * @param headerAccessor the message headers
     * @param principal the authenticated user
     */
    @MessageMapping("/chat.join/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId,
                              SimpMessageHeaderAccessor headerAccessor,
                              Principal principal) {
        
        String username = principal.getName();
        log.debug("User {} joining room {}", username, roomId);
        
        // Get the chat room
        ChatRoom room = chatRooms.get(roomId);
        if (room == null) {
            log.warn("User {} attempted to join non-existent room {}", username, roomId);
            throw new IllegalArgumentException("Chat room does not exist: " + roomId);
        }
        
        // Add user to the room
        room.addParticipant(username);
        
        // Add username to WebSocket session
        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("room_id", roomId);
        
        // Create and return join message
        return ChatMessage.createJoinMessage(roomId, username);
    }
    
    /**
     * Creates a new chat room.
     * 
     * @param room the chat room details
     * @param principal the authenticated user
     * @return the created chat room
     */
    @MessageMapping("/chat.createRoom")
    public ChatRoom createRoom(@Payload ChatRoom room, Principal principal) {
        String username = principal.getName();
        log.debug("User {} creating new chat room: {}", username, room.getName());
        
        // Set creator if not already set
        if (room.getCreatedBy() == null) {
            room.setCreatedBy(username);
        }
        
        // Set creation time if not already set
        if (room.getCreatedAt() == null) {
            room.setCreatedAt(LocalDateTime.now());
        }
        
        // Set room ID if not already set
        if (room.getId() == null) {
            room.setId(java.util.UUID.randomUUID().toString());
        }
        
        // Add creator as participant
        room.addParticipant(username);
        
        // Set active flag
        room.setActive(true);
        
        // Store the room
        chatRooms.put(room.getId(), room);
        
        // Notify the creator
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/room.created",
            room
        );
        
        // Notify other participants
        for (String participant : room.getParticipants()) {
            if (!participant.equals(username)) {
                messagingTemplate.convertAndSendToUser(
                    participant,
                    "/queue/room.invitation",
                    room
                );
            }
        }
        
        // In a real application, you would persist the room here
        
        return room;
    }
    
    /**
     * Sends a typing notification to a chat room.
     * 
     * @param roomId the room ID
     * @param principal the authenticated user
     */
    @MessageMapping("/chat.typing/{roomId}")
    @SendTo("/topic/room/{roomId}/typing")
    public ChatMessage sendTypingNotification(@DestinationVariable String roomId, Principal principal) {
        String username = principal.getName();
        
        // Create typing notification
        ChatMessage typingMessage = new ChatMessage();
        typingMessage.setType(ChatMessage.MessageType.TYPING);
        typingMessage.setSender(username);
        typingMessage.setRoomId(roomId);
        typingMessage.setTimestamp(LocalDateTime.now());
        
        return typingMessage;
    }
    
    /**
     * Sends an error message to a user.
     * 
     * @param username the username
     * @param errorMessage the error message
     */
    private void sendErrorMessage(String username, String errorMessage) {
        ChatMessage error = ChatMessage.createErrorMessage(null, errorMessage);
        
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/errors",
            error
        );
        
        log.error("Sent error to user {}: {}", username, errorMessage);
    }
}
