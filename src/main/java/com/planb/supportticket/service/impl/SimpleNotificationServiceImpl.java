package com.planb.supportticket.service.impl;

import com.planb.supportticket.dto.NotificationDTO;
import com.planb.supportticket.enums.NotificationType;
import com.planb.supportticket.service.EmailService;
import com.planb.supportticket.service.SimpleNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the SimpleNotificationService interface.
 * Handles email notifications via SMTP/Gmail, real-time notifications via WebSocket, and status updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleNotificationServiceImpl implements SimpleNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    @Override
    public void sendNotification(NotificationDTO notification) {
        // Set timestamp if not already set
        if (notification.getTimestamp() == null) {
            notification.setTimestamp(LocalDateTime.now());
        }

        // Set ID if not already set
        if (notification.getId() == null) {
            notification.setId(UUID.randomUUID());
        }

        // Send real-time notification via WebSocket
        String destination = String.format("/queue/notifications/%s", notification.getUserId());
        messagingTemplate.convertAndSend(destination, notification);

        // If email notification is enabled, send email
        if (notification.isEmailEnabled()) {
            sendEmailNotification(notification);
        }

        log.info("Notification sent: {}", notification);
    }

    @Override
    public void sendSystemNotification(String message, NotificationType type) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .title("System Notification")
                .content(message)
                .type(type)
                .systemNotification(true)
                .timestamp(LocalDateTime.now())
                .autoClose(true)
                .autoCloseDelay(5000)
                .build();

        messagingTemplate.convertAndSend("/topic/system-notifications", notification);
        log.info("System notification sent: {}", message);
    }

    @Override
    public void notifyExpert(String expertId, String message) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(expertId))
                .title("Expert Notification")
                .content(message)
                .type(NotificationType.INFO)
                .timestamp(LocalDateTime.now())
                .autoClose(true)
                .autoCloseDelay(5000)
                .build();

        String destination = String.format("/queue/expert-notifications/%s", expertId);
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Expert notification sent to {}: {}", expertId, message);
    }

    @Override
    public void notifyTicketUpdate(String ticketId, String userId, String message) {
        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userId))
                .title("Ticket Update")
                .content(message)
                .type(NotificationType.INFO)
                .ticketId(ticketId)
                .timestamp(LocalDateTime.now())
                .autoClose(true)
                .autoCloseDelay(5000)
                .build();

        sendNotification(notification);
    }

    /**
     * Sends an email notification.
     *
     * @param notification the notification data
     */
    private void sendEmailNotification(NotificationDTO notification) {
        try {
            emailService.sendEmail(
                notification.getEmail(),
                "New Notification",
                buildEmailContent(notification)
            );
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            // Don't throw exception as email is secondary notification
        }
    }

    /**
     * Builds the email content for a notification.
     *
     * @param notification the notification data
     * @return the email content
     */
    private String buildEmailContent(NotificationDTO notification) {
        return String.format("""
            Hello,

            You have a new notification:
            %s

            Best regards,
            Support Team
            """, notification.getMessage());
    }
}
