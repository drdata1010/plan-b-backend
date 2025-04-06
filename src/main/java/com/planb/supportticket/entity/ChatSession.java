package com.planb.supportticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a chat session between users and experts.
 * Stores chat metadata and references to messages.
 */
@Entity
@Table(name = "chat_sessions",
       indexes = {
           @Index(name = "idx_chat_session_user", columnList = "user_id"),
           @Index(name = "idx_chat_session_expert", columnList = "expert_id"),
           @Index(name = "idx_chat_session_ticket", columnList = "ticket_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession extends BaseEntity {

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private Expert expert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "session_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatSessionType sessionType;

    /**
     * Adds a message to this chat session.
     *
     * @param message the message to add
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setChatSession(this);
    }

    /**
     * Removes a message from this chat session.
     *
     * @param message the message to remove
     */
    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setChatSession(null);
    }

    /**
     * Ends this chat session.
     */
    public void endSession() {
        this.isActive = false;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Enum representing the type of chat session.
     */
    public enum ChatSessionType {
        USER_EXPERT,
        USER_AI,
        TICKET_RELATED,
        CONSULTATION,
        SUPPORT
    }
}
