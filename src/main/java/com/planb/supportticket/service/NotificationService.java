package com.planb.supportticket.service;

import com.planb.supportticket.dto.NotificationDTO;
import com.planb.supportticket.entity.*;
import com.planb.supportticket.entity.enums.TicketStatus;
import com.planb.supportticket.enums.NotificationType;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for notification operations.
 * Handles email notifications via AWS SES, real-time notifications via WebSocket, and status updates.
 */
public interface NotificationService {

    /**
     * Sends a notification to a user.
     *
     * @param userId the user ID
     * @param notificationDTO the notification data
     */
    void sendNotification(UUID userId, NotificationDTO notificationDTO);

    /**
     * Sends a notification to multiple users.
     *
     * @param userIds the user IDs
     * @param notificationDTO the notification data
     */
    void sendNotificationToUsers(List<UUID> userIds, NotificationDTO notificationDTO);

    /**
     * Sends a system notification to all users.
     *
     * @param message the notification message
     * @param type the notification type
     */
    void sendSystemNotification(String message, NotificationType type);

    /**
     * Sends a notification to users with a specific role.
     *
     * @param role the role
     * @param notificationDTO the notification data
     */
    void sendNotificationToRole(String role, NotificationDTO notificationDTO);

    /**
     * Sends a ticket created notification.
     *
     * @param ticket the created ticket
     */
    void sendTicketCreatedNotification(Ticket ticket);

    /**
     * Sends a ticket updated notification.
     *
     * @param ticket the updated ticket
     */
    void sendTicketUpdatedNotification(Ticket ticket);

    /**
     * Sends a ticket status changed notification.
     *
     * @param ticket the ticket
     * @param oldStatus the old status
     */
    void sendTicketStatusChangedNotification(Ticket ticket, TicketStatus oldStatus);

    /**
     * Sends a ticket priority changed notification.
     *
     * @param ticket the ticket
     */
    void sendTicketPriorityChangedNotification(Ticket ticket);

    /**
     * Sends a ticket assigned notification.
     *
     * @param ticket the assigned ticket
     */
    void sendTicketAssignedNotification(Ticket ticket);

    /**
     * Sends a ticket unassigned notification.
     *
     * @param ticket the unassigned ticket
     * @param previousExpert the previous expert
     */
    void sendTicketUnassignedNotification(Ticket ticket, Expert previousExpert);

    /**
     * Sends a comment added notification.
     *
     * @param comment the added comment
     */
    void sendCommentAddedNotification(TicketComment comment);

    /**
     * Sends a reply added notification.
     *
     * @param reply the added reply
     */
    void sendReplyAddedNotification(TicketComment reply);

    /**
     * Sends a ticket resolved notification.
     *
     * @param ticket the resolved ticket
     */
    void sendTicketResolvedNotification(Ticket ticket);

    /**
     * Sends a ticket reopened notification.
     *
     * @param ticket the reopened ticket
     */
    void sendTicketReopenedNotification(Ticket ticket);

    /**
     * Sends a consultation scheduled notification.
     *
     * @param consultation the scheduled consultation
     */
    void sendConsultationScheduledNotification(Consultation consultation);

    /**
     * Sends a consultation cancelled notification.
     *
     * @param consultation the cancelled consultation
     */
    void sendConsultationCancelledNotification(Consultation consultation);

    /**
     * Sends a consultation reminder notification.
     *
     * @param consultation the consultation
     */
    void sendConsultationReminderNotification(Consultation consultation);

    /**
     * Sends a consultation completed notification.
     *
     * @param consultation the completed consultation
     */
    void sendConsultationCompletedNotification(Consultation consultation);

    /**
     * Sends a new message notification.
     *
     * @param message the new message
     */
    void sendNewMessageNotification(ChatMessage message);

    /**
     * Sends a welcome email to a new user.
     *
     * @param user the new user
     */
    void sendWelcomeEmail(UserProfile user);

    /**
     * Sends a password reset email.
     *
     * @param user the user
     * @param resetToken the password reset token
     */
    void sendPasswordResetEmail(UserProfile user, String resetToken);

    /**
     * Sends an email verification email.
     *
     * @param user the user
     * @param verificationToken the email verification token
     */
    void sendEmailVerificationEmail(UserProfile user, String verificationToken);

    /**
     * Sends a ticket summary email.
     *
     * @param user the user
     * @param tickets the tickets
     */
    void sendTicketSummaryEmail(UserProfile user, List<Ticket> tickets);

    /**
     * Sends a consultation confirmation email.
     *
     * @param consultation the consultation
     */
    void sendConsultationConfirmationEmail(Consultation consultation);

    /**
     * Sends a consultation reminder email.
     *
     * @param consultation the consultation
     */
    void sendConsultationReminderEmail(Consultation consultation);

    /**
     * Gets unread notifications for a user.
     *
     * @param userId the user ID
     * @return a list of unread notifications
     */
    List<NotificationDTO> getUnreadNotifications(UUID userId);

    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification ID
     */
    void markNotificationAsRead(UUID notificationId);

    /**
     * Marks all notifications as read for a user.
     *
     * @param userId the user ID
     */
    void markAllNotificationsAsRead(UUID userId);

    /**
     * Gets notification count for a user.
     *
     * @param userId the user ID
     * @return the notification count
     */
    int getNotificationCount(UUID userId);
}
