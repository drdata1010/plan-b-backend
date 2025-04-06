package com.planb.supportticket.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for chat rooms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    /**
     * Enum representing different types of chat rooms.
     */
    public enum RoomType {
        SUPPORT,    // Support chat between user and support agent
        AI,         // Chat with AI model
        GROUP,      // Group chat with multiple participants
        PRIVATE,    // Private chat between two users
        TICKET      // Chat related to a specific ticket
    }

    /**
     * Unique identifier for the room.
     */
    private String id;

    /**
     * Name of the room.
     */
    private String name;

    /**
     * Type of room.
     */
    private RoomType type;

    /**
     * Creator of the room.
     */
    private String createdBy;

    /**
     * Timestamp when the room was created.
     */
    private LocalDateTime createdAt;

    /**
     * Set of participants in the room.
     */
    @Builder.Default
    private Set<String> participants = new HashSet<>();

    /**
     * Associated ticket ID (for TICKET type rooms).
     */
    private Long ticketId;

    /**
     * Associated AI model type (for AI type rooms).
     */
    private AIModelType aiModelType;

    /**
     * Whether the room is active.
     */
    private boolean active;

    /**
     * Factory method to create a support chat room.
     */
    public static ChatRoom createSupportRoom(String userId, String supportAgentId) {
        ChatRoom room = ChatRoom.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name("Support Chat")
                .type(RoomType.SUPPORT)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
        
        room.getParticipants().add(userId);
        room.getParticipants().add(supportAgentId);
        
        return room;
    }

    /**
     * Factory method to create an AI chat room.
     */
    public static ChatRoom createAIRoom(String userId, AIModelType modelType) {
        ChatRoom room = ChatRoom.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name("Chat with " + modelType.getDisplayName())
                .type(RoomType.AI)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .aiModelType(modelType)
                .active(true)
                .build();
        
        room.getParticipants().add(userId);
        
        return room;
    }

    /**
     * Factory method to create a ticket chat room.
     */
    public static ChatRoom createTicketRoom(String userId, Long ticketId) {
        ChatRoom room = ChatRoom.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name("Ticket #" + ticketId)
                .type(RoomType.TICKET)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .ticketId(ticketId)
                .active(true)
                .build();
        
        room.getParticipants().add(userId);
        
        return room;
    }

    /**
     * Factory method to create a private chat room.
     */
    public static ChatRoom createPrivateRoom(String user1, String user2) {
        ChatRoom room = ChatRoom.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name("Private Chat")
                .type(RoomType.PRIVATE)
                .createdBy(user1)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
        
        room.getParticipants().add(user1);
        room.getParticipants().add(user2);
        
        return room;
    }

    /**
     * Factory method to create a group chat room.
     */
    public static ChatRoom createGroupRoom(String creatorId, String name, Set<String> participants) {
        ChatRoom room = ChatRoom.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name(name)
                .type(RoomType.GROUP)
                .createdBy(creatorId)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
        
        room.getParticipants().add(creatorId);
        room.getParticipants().addAll(participants);
        
        return room;
    }

    /**
     * Add a participant to the room.
     */
    public void addParticipant(String userId) {
        this.participants.add(userId);
    }

    /**
     * Remove a participant from the room.
     */
    public void removeParticipant(String userId) {
        this.participants.remove(userId);
    }

    /**
     * Check if a user is a participant in the room.
     */
    public boolean hasParticipant(String userId) {
        return this.participants.contains(userId);
    }
}
