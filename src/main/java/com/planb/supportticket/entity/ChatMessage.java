package com.planb.supportticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a message in a chat session.
 * Stores message content and metadata.
 */
@Entity
@Table(name = "chat_messages", 
       indexes = {
           @Index(name = "idx_message_chat_session", columnList = "chat_session_id"),
           @Index(name = "idx_message_sender", columnList = "sender_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "ai_model")
    private String aiModel;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Adds an attachment to this message.
     *
     * @param attachment the attachment to add
     */
    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }

    /**
     * Removes an attachment from this message.
     *
     * @param attachment the attachment to remove
     */
    public void removeAttachment(Attachment attachment) {
        attachments.remove(attachment);
        attachment.setMessage(null);
    }
    
    /**
     * Enum representing the type of message.
     */
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM,
        AI_RESPONSE,
        USER_JOINED,
        USER_LEFT
    }
}
