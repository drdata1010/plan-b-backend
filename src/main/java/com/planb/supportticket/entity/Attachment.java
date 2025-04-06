package com.planb.supportticket.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a file attachment.
 * Can be attached to tickets, comments, or chat messages.
 */
@Entity
@Table(name = "attachments", 
       indexes = {
           @Index(name = "idx_attachment_user", columnList = "user_id"),
           @Index(name = "idx_attachment_ticket", columnList = "ticket_id"),
           @Index(name = "idx_attachment_comment", columnList = "comment_id"),
           @Index(name = "idx_attachment_message", columnList = "message_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "s3_bucket")
    private String s3Bucket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private TicketComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * Gets the file extension from the file name.
     *
     * @return the file extension
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    /**
     * Checks if the attachment is an image.
     *
     * @return true if the attachment is an image, false otherwise
     */
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * Checks if the attachment is a PDF.
     *
     * @return true if the attachment is a PDF, false otherwise
     */
    public boolean isPdf() {
        return contentType != null && contentType.equals("application/pdf");
    }
}
