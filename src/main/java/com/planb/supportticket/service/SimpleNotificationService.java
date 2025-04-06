package com.planb.supportticket.service;

import com.planb.supportticket.dto.NotificationDTO;
import com.planb.supportticket.enums.NotificationType;

/**
 * Service interface for simplified notification operations.
 * Handles email notifications via AWS SES, real-time notifications via WebSocket, and status updates.
 */
public interface SimpleNotificationService {

    /**
     * Sends a notification to a user.
     *
     * @param notification the notification data
     */
    void sendNotification(NotificationDTO notification);

    /**
     * Sends a system notification to all users.
     *
     * @param message the notification message
     * @param type the notification type
     */
    void sendSystemNotification(String message, NotificationType type);

    /**
     * Notifies an expert about an event.
     *
     * @param expertId the expert ID
     * @param message the notification message
     */
    void notifyExpert(String expertId, String message);

    /**
     * Notifies a user about a ticket update.
     *
     * @param ticketId the ticket ID
     * @param userId the user ID
     * @param message the notification message
     */
    void notifyTicketUpdate(String ticketId, String userId, String message);
}
