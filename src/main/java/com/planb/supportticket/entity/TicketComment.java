package com.planb.supportticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing comments on tickets.
 * Supports threaded comments with parent-child relationships.
 */
@Entity
@Table(name = "ticket_comments", 
       indexes = {
           @Index(name = "idx_comment_ticket", columnList = "ticket_id"),
           @Index(name = "idx_comment_user", columnList = "user_id"),
           @Index(name = "idx_comment_parent", columnList = "parent_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketComment extends BaseEntity {

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private TicketComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketComment> replies = new ArrayList<>();

    @Column(name = "is_internal_note")
    private boolean isInternalNote;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    /**
     * Adds a reply to this comment.
     *
     * @param reply the reply to add
     */
    public void addReply(TicketComment reply) {
        replies.add(reply);
        reply.setParent(this);
    }

    /**
     * Removes a reply from this comment.
     *
     * @param reply the reply to remove
     */
    public void removeReply(TicketComment reply) {
        replies.remove(reply);
        reply.setParent(null);
    }
    
    /**
     * Adds an attachment to this comment.
     *
     * @param attachment the attachment to add
     */
    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        attachment.setComment(this);
    }
    
    /**
     * Removes an attachment from this comment.
     *
     * @param attachment the attachment to remove
     */
    public void removeAttachment(Attachment attachment) {
        attachments.remove(attachment);
        attachment.setComment(null);
    }
}
