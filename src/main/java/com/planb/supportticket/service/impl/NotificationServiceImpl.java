package com.planb.supportticket.service.impl;


import com.planb.supportticket.dto.NotificationDTO;
import com.planb.supportticket.entity.*;
import com.planb.supportticket.entity.enums.TicketStatus;
import com.planb.supportticket.enums.NotificationType;
import com.planb.supportticket.service.NotificationService;
import com.planb.supportticket.service.SESService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the NotificationService interface.
 * Handles email notifications via AWS SES, real-time notifications via WebSocket, and status updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final SESService sesService;

    // Simple implementation for now - we'll add the full implementation later

    @Override
    public void sendNotification(UUID userId, NotificationDTO notificationDTO) {
        // Create notification
        NotificationDTO notification = NotificationDTO.builder()
                .id(notificationDTO.getId())
                .userId(userId)
                .title(notificationDTO.getTitle())
                .content(notificationDTO.getContent())
                .type(convertNotificationType(notificationDTO.getType()))
                .timestamp(LocalDateTime.now())
                .build();

        // Send real-time notification via WebSocket
        String destination = String.format("/queue/notifications/%s", notification.getUserId());
        messagingTemplate.convertAndSend(destination, notification);

        log.info("Notification sent to user {}: {}", userId, notification.getMessage());
    }

    @Override
    public void sendNotificationToUsers(List<UUID> userIds, NotificationDTO notificationDTO) {
        for (UUID userId : userIds) {
            sendNotification(userId, notificationDTO);
        }
    }

    @Override
    public void sendNotificationToRole(String role, NotificationDTO notificationDTO) {
        // For now, just broadcast to a role-specific topic
        NotificationDTO notification = NotificationDTO.builder()
                .id(notificationDTO.getId())
                .title(notificationDTO.getTitle())
                .content(notificationDTO.getContent())
                .type(convertNotificationType(notificationDTO.getType()))
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/role/" + role, notification);
        log.info("Notification sent to role {}: {}", role, notification.getMessage());
    }

    @Override
    public void sendTicketCreatedNotification(Ticket ticket) {
        // Simple implementation for now
        String message = "Ticket created: " + ticket.getTitle();

        // Notify the ticket creator
        NotificationDTO notification = NotificationDTO.builder()
                .userId(ticket.getUser().getId())
                .title("Notification")
                .content(message)
                .type(NotificationType.TICKET_CREATED)
                .ticketId(ticket.getId().toString())
                .timestamp(LocalDateTime.now())
                .build();

        String destination = String.format("/queue/notifications/%s", notification.getUserId().toString());
        messagingTemplate.convertAndSend(destination, notification);

        // Also notify support staff
        messagingTemplate.convertAndSend("/topic/role/SUPPORT", notification);

        log.info("Ticket created notification sent: {}", message);
    }

    @Override
    public void sendTicketUpdatedNotification(Ticket ticket) {
        // Simple implementation
        notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                "Ticket updated: " + ticket.getTitle());
    }

    @Override
    public void sendTicketStatusChangedNotification(Ticket ticket, TicketStatus oldStatus) {
        // Simple implementation
        notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                "Ticket status changed from " + oldStatus + " to " + ticket.getStatus() + ": " + ticket.getTitle());
    }

    @Override
    public void sendTicketPriorityChangedNotification(Ticket ticket) {
        // Simple implementation
        notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                "Ticket priority changed to " + ticket.getPriority() + ": " + ticket.getTitle());
    }

    @Override
    public void sendTicketAssignedNotification(Ticket ticket) {
        // Simple implementation
        if (ticket.getAssignedExpert() != null) {
            notifyExpert(ticket.getAssignedExpert().getId().toString(),
                    "Ticket assigned to you: " + ticket.getTitle());

            notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                    "Ticket assigned to " + ticket.getAssignedExpert().getUserProfile().getDisplayName());
        }
    }

    @Override
    public void sendTicketUnassignedNotification(Ticket ticket, Expert previousExpert) {
        // Simple implementation
        if (previousExpert != null) {
            notifyExpert(previousExpert.getId().toString(),
                    "Ticket unassigned from you: " + ticket.getTitle());

            notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                    "Ticket unassigned from " + previousExpert.getUserProfile().getDisplayName());
        }
    }

    @Override
    public void sendCommentAddedNotification(TicketComment comment) {
        // Simple implementation
        Ticket ticket = comment.getTicket();
        notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                "New comment added to ticket: " + ticket.getTitle());

        // If assigned to an expert, notify them too
        if (ticket.getAssignedExpert() != null) {
            notifyExpert(ticket.getAssignedExpert().getId().toString(),
                    "New comment added to ticket: " + ticket.getTitle());
        }
    }

    @Override
    public void sendReplyAddedNotification(TicketComment reply) {
        // Simple implementation - notify the parent comment author
        if (reply.getParent() != null) {
            UserProfile parentAuthor = reply.getParent().getUser();
            notifyTicketUpdate(reply.getTicket().getId().toString(), parentAuthor.getId().toString(),
                    "New reply to your comment on ticket: " + reply.getTicket().getTitle());
        }
    }

    @Override
    public void sendTicketResolvedNotification(Ticket ticket) {
        // Simple implementation
        notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                "Ticket resolved: " + ticket.getTitle());
    }

    @Override
    public void sendTicketReopenedNotification(Ticket ticket) {
        // Simple implementation
        notifyTicketUpdate(ticket.getId().toString(), ticket.getUser().getId().toString(),
                "Ticket reopened: " + ticket.getTitle());

        // If assigned to an expert, notify them too
        if (ticket.getAssignedExpert() != null) {
            notifyExpert(ticket.getAssignedExpert().getId().toString(),
                    "Ticket reopened: " + ticket.getTitle());
        }
    }

    @Override
    public void sendConsultationScheduledNotification(Consultation consultation) {
        // Simple implementation - to be expanded later
        // Notify user
        notifyUser(consultation.getUser().getId().toString(),
                "Consultation scheduled with " + consultation.getExpert().getUserProfile().getDisplayName());

        // Notify expert
        notifyExpert(consultation.getExpert().getId().toString(),
                "Consultation scheduled with " + consultation.getUser().getDisplayName());
    }

    @Override
    public void sendConsultationCancelledNotification(Consultation consultation) {
        // Simple implementation - to be expanded later
        // Notify user
        notifyUser(consultation.getUser().getId().toString(),
                "Consultation cancelled with " + consultation.getExpert().getUserProfile().getDisplayName());

        // Notify expert
        notifyExpert(consultation.getExpert().getId().toString(),
                "Consultation cancelled with " + consultation.getUser().getDisplayName());
    }

    @Override
    public void sendConsultationReminderNotification(Consultation consultation) {
        // Simple implementation - to be expanded later
        // Notify user
        notifyUser(consultation.getUser().getId().toString(),
                "Reminder: Upcoming consultation with " + consultation.getExpert().getUserProfile().getDisplayName());

        // Notify expert
        notifyExpert(consultation.getExpert().getId().toString(),
                "Reminder: Upcoming consultation with " + consultation.getUser().getDisplayName());
    }

    @Override
    public void sendConsultationCompletedNotification(Consultation consultation) {
        // Simple implementation - to be expanded later
        // Notify user
        notifyUser(consultation.getUser().getId().toString(),
                "Consultation completed with " + consultation.getExpert().getUserProfile().getDisplayName());

        // Notify expert
        notifyExpert(consultation.getExpert().getId().toString(),
                "Consultation completed with " + consultation.getUser().getDisplayName());
    }

    @Override
    public void sendNewMessageNotification(ChatMessage message) {
        // Simple implementation - to be expanded later
        // Notify recipient
        UserProfile recipient = message.getChatSession().getUser();
        if (!message.getSender().getId().equals(recipient.getId())) {
            notifyUser(recipient.getId().toString(),
                    "New message from " + message.getSender().getDisplayName());
        }

        // If it's a chat with an expert, notify the expert
        if (message.getChatSession().getExpert() != null &&
                !message.getSender().getId().equals(message.getChatSession().getExpert().getUserProfile().getId())) {
            notifyExpert(message.getChatSession().getExpert().getId().toString(),
                    "New message from " + message.getSender().getDisplayName());
        }
    }

    @Override
    public void sendWelcomeEmail(UserProfile user) {
        // Simple implementation - to be expanded later
        try {
            sesService.sendEmail(
                user.getEmail(),
                "Welcome to Support Ticket System",
                "Hello " + user.getDisplayName() + ",\n\n" +
                "Welcome to the Support Ticket System! We're glad to have you on board.\n\n" +
                "Best regards,\n" +
                "Support Team"
            );
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendPasswordResetEmail(UserProfile user, String resetToken) {
        // Simple implementation - to be expanded later
        try {
            sesService.sendEmail(
                user.getEmail(),
                "Password Reset Request",
                "Hello " + user.getDisplayName() + ",\n\n" +
                "You requested a password reset. Use the following token to reset your password: " + resetToken + "\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Support Team"
            );
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendEmailVerificationEmail(UserProfile user, String verificationToken) {
        // Simple implementation - to be expanded later
        try {
            sesService.sendEmail(
                user.getEmail(),
                "Email Verification",
                "Hello " + user.getDisplayName() + ",\n\n" +
                "Please verify your email address by using the following token: " + verificationToken + "\n\n" +
                "Best regards,\n" +
                "Support Team"
            );
        } catch (Exception e) {
            log.error("Failed to send email verification to {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendTicketSummaryEmail(UserProfile user, List<Ticket> tickets) {
        // Simple implementation - to be expanded later
        try {
            StringBuilder summary = new StringBuilder();
            summary.append("Hello ").append(user.getDisplayName()).append(",\n\n");
            summary.append("Here's a summary of your tickets:\n\n");

            for (Ticket ticket : tickets) {
                summary.append("- ").append(ticket.getTitle())
                       .append(" (").append(ticket.getStatus()).append(")\n");
            }

            summary.append("\nBest regards,\n");
            summary.append("Support Team");

            sesService.sendEmail(
                user.getEmail(),
                "Your Ticket Summary",
                summary.toString()
            );
        } catch (Exception e) {
            log.error("Failed to send ticket summary email to {}", user.getEmail(), e);
        }
    }

    @Override
    public void sendConsultationConfirmationEmail(Consultation consultation) {
        // Simple implementation - to be expanded later
        try {
            UserProfile user = consultation.getUser();
            Expert expert = consultation.getExpert();

            String message = "Hello " + user.getDisplayName() + ",\n\n" +
                "Your consultation with " + expert.getUserProfile().getDisplayName() +
                " has been confirmed for " + consultation.getScheduledAt() + ".\n\n" +
                "Best regards,\n" +
                "Support Team";

            sesService.sendEmail(
                user.getEmail(),
                "Consultation Confirmation",
                message
            );
        } catch (Exception e) {
            log.error("Failed to send consultation confirmation email", e);
        }
    }

    @Override
    public void sendConsultationReminderEmail(Consultation consultation) {
        // Simple implementation - to be expanded later
        try {
            UserProfile user = consultation.getUser();
            Expert expert = consultation.getExpert();

            String message = "Hello " + user.getDisplayName() + ",\n\n" +
                "This is a reminder about your upcoming consultation with " +
                expert.getUserProfile().getDisplayName() + " scheduled for " +
                consultation.getScheduledAt() + ".\n\n" +
                "Best regards,\n" +
                "Support Team";

            sesService.sendEmail(
                user.getEmail(),
                "Consultation Reminder",
                message
            );
        } catch (Exception e) {
            log.error("Failed to send consultation reminder email", e);
        }
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(UUID userId) {
        // Placeholder implementation - to be expanded later
        // In a real implementation, we would fetch from a database
        return new ArrayList<>();
    }

    @Override
    public void markNotificationAsRead(UUID notificationId) {
        // Placeholder implementation - to be expanded later
        // In a real implementation, we would update the database
        log.info("Marking notification as read: {}", notificationId);
    }

    @Override
    public void markAllNotificationsAsRead(UUID userId) {
        // Placeholder implementation - to be expanded later
        // In a real implementation, we would update the database
        log.info("Marking all notifications as read for user: {}", userId);
    }

    @Override
    public int getNotificationCount(UUID userId) {
        // Placeholder implementation - to be expanded later
        // In a real implementation, we would query the database
        return 0;
    }

    // Additional methods to support the simplified interface

    /**
     * Sends a system notification to all users.
     *
     * @param message the notification message
     * @param type the notification type
     */
    public void sendSystemNotification(String message, NotificationType type) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .title("System Notification")
                .content(message)
                .type(type)
                .systemNotification(true)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/system-notifications", notification);
        log.info("System notification sent: {}", message);
    }

    /**
     * Notifies an expert about an event.
     *
     * @param expertId the expert ID
     * @param message the notification message
     */
    public void notifyExpert(String expertId, String message) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(expertId))
                .title("Expert Notification")
                .content(message)
                .type(NotificationType.INFO)
                .timestamp(LocalDateTime.now())
                .build();

        String destination = String.format("/queue/expert-notifications/%s", expertId);
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Expert notification sent to {}: {}", expertId, message);
    }

    /**
     * Notifies a user about an event.
     *
     * @param userId the user ID
     * @param message the notification message
     */
    private void notifyUser(String userId, String message) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userId))
                .title("User Notification")
                .content(message)
                .type(NotificationType.INFO)
                .timestamp(LocalDateTime.now())
                .build();

        String destination = String.format("/queue/notifications/%s", userId);
        messagingTemplate.convertAndSend(destination, notification);
        log.info("User notification sent to {}: {}", userId, message);
    }

    /**
     * Notifies a user about a ticket update.
     *
     * @param ticketId the ticket ID
     * @param userId the user ID
     * @param message the notification message
     */
    public void notifyTicketUpdate(String ticketId, String userId, String message) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userId))
                .title("Ticket Update")
                .content(message)
                .type(NotificationType.INFO)
                .ticketId(ticketId)
                .timestamp(LocalDateTime.now())
                .build();

        String destination = String.format("/queue/notifications/%s", userId);
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Ticket update notification sent to {}: {}", userId, message);
    }

    /**
     * This method is no longer needed since we're using the same NotificationType enum.
     * Kept for backward compatibility.
     *
     * @param type the NotificationType
     * @return the same NotificationType
     */
    private NotificationType convertNotificationType(NotificationType type) {
        if (type == null) {
            return NotificationType.INFO;
        }
        return type;
    }
}
