package com.planb.supportticket.dto;

import com.planb.supportticket.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    // Using the external NotificationType enum from com.planb.supportticket.enums

    /**
     * Unique identifier for the notification.
     */
    private UUID id;

    /**
     * Type of notification.
     */
    private NotificationType type;

    /**
     * Title of the notification.
     */
    private String title;

    /**
     * Content of the notification.
     */
    private String content;

    /**
     * Whether the notification has been read.
     */
    private boolean read;

    /**
     * Timestamp when the notification was created.
     */
    private LocalDateTime timestamp;

    /**
     * ID of the user the notification is for.
     */
    private UUID userId;

    /**
     * ID of the related entity (ticket, comment, consultation, etc.).
     */
    private UUID entityId;

    /**
     * Type of the related entity.
     */
    private String entityType;

    /**
     * Additional data for the notification.
     */
    private Map<String, Object> data;

    /**
     * URL to redirect to when the notification is clicked.
     */
    private String redirectUrl;

    /**
     * Email address to send the notification to.
     */
    private String email;

    /**
     * Whether to send an email notification.
     */
    private boolean emailEnabled;

    /**
     * Whether this is a system notification.
     */
    private boolean systemNotification;

    /**
     * Whether the notification should auto-close.
     */
    private boolean autoClose;

    /**
     * Delay in milliseconds before auto-closing.
     */
    private int autoCloseDelay;

    /**
     * ID of the related ticket.
     */
    private String ticketId;

    /**
     * Gets the message content (combines title and content).
     *
     * @return the message
     */
    public String getMessage() {
        return title + ": " + content;
    }
}
