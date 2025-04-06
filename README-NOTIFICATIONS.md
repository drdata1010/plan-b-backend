# Notification System Documentation

This document describes the notification system in the Support Ticket System.

## Overview

The notification system provides real-time notifications to users via WebSocket and email notifications via AWS SES. It supports different types of notifications, including system notifications, user-specific notifications, expert notifications, and ticket update notifications.

## Components

The notification system consists of the following components:

1. **NotificationType** - Enum representing different types of notifications
2. **NotificationDto** - Data Transfer Object for notifications
3. **SimpleNotificationService** - Service interface for notification operations
4. **SimpleNotificationServiceImpl** - Implementation of the notification service
5. **WebSocket Configuration** - Configuration for WebSocket messaging

## Notification Types

The `NotificationType` enum defines the following notification types:

- `SUCCESS` - Success notification
- `ERROR` - Error notification
- `WARNING` - Warning notification
- `INFO` - Information notification
- `TICKET_CREATED` - Ticket created notification
- `TICKET_UPDATED` - Ticket updated notification
- `TICKET_ASSIGNED` - Ticket assigned notification
- `TICKET_RESOLVED` - Ticket resolved notification
- `COMMENT_ADDED` - Comment added notification
- `CONSULTATION_SCHEDULED` - Consultation scheduled notification
- `CONSULTATION_CANCELLED` - Consultation cancelled notification
- `NEW_MESSAGE` - New message notification

## Notification DTO

The `NotificationDto` class represents a notification with the following fields:

- `id` - Unique identifier for the notification
- `userId` - ID of the user the notification is for
- `type` - Type of notification (NotificationType)
- `message` - Message content of the notification
- `read` - Whether the notification has been read
- `timestamp` - Timestamp when the notification was created
- `ticketId` - ID of the related ticket (if applicable)
- `email` - Email address to send notification to
- `emailEnabled` - Whether to send an email notification
- `systemNotification` - Whether this is a system notification
- `autoClose` - Whether the notification should auto-close (default: true)
- `autoCloseDelay` - Delay in milliseconds before auto-closing (default: 5000)

## Notification Service

The `SimpleNotificationService` interface defines the following methods:

- `sendNotification(NotificationDto)` - Sends a notification to a user
- `sendSystemNotification(String, NotificationType)` - Sends a system notification to all users
- `notifyExpert(String, String)` - Notifies an expert about an event
- `notifyTicketUpdate(String, String, String)` - Notifies a user about a ticket update

## WebSocket Destinations

The notification system uses the following WebSocket destinations:

- `/queue/notifications/{userId}` - User-specific notifications
- `/topic/system-notifications` - System-wide notifications
- `/queue/expert-notifications/{expertId}` - Expert-specific notifications

## Email Notifications

The notification system supports email notifications via AWS SES. Email notifications are sent when the `emailEnabled` flag is set to `true` in the `NotificationDto`.

## Usage Examples

### Sending a User Notification

```java
NotificationDto notification = NotificationDto.builder()
    .userId("user123")
    .message("Your ticket has been updated")
    .type(NotificationType.INFO)
    .ticketId("ticket456")
    .build();

notificationService.sendNotification(notification);
```

### Sending a System Notification

```java
notificationService.sendSystemNotification(
    "System maintenance scheduled for tomorrow",
    NotificationType.WARNING
);
```

### Notifying an Expert

```java
notificationService.notifyExpert(
    "expert123",
    "You have been assigned a new ticket"
);
```

### Notifying a User about a Ticket Update

```java
notificationService.notifyTicketUpdate(
    "ticket456",
    "user123",
    "Your ticket has been assigned to an expert"
);
```

## Client-Side Integration

On the client side, you need to subscribe to the appropriate WebSocket destinations to receive notifications:

```javascript
// Subscribe to user-specific notifications
stompClient.subscribe('/queue/notifications/' + userId, function(notification) {
    const notificationData = JSON.parse(notification.body);
    showNotification(notificationData);
});

// Subscribe to system notifications
stompClient.subscribe('/topic/system-notifications', function(notification) {
    const notificationData = JSON.parse(notification.body);
    showNotification(notificationData);
});

// For experts, subscribe to expert notifications
stompClient.subscribe('/queue/expert-notifications/' + expertId, function(notification) {
    const notificationData = JSON.parse(notification.body);
    showNotification(notificationData);
});
```

## Notification Display

Notifications are displayed on the client side with the following behavior:

- Success notifications are displayed with a green background
- Error notifications are displayed with a red background
- Warning notifications are displayed with a yellow background
- Info notifications are displayed with a blue background
- Notifications auto-close after 5 seconds by default
- Notifications can be manually closed by the user
- Notifications can be configured to not auto-close for important messages

## Email Templates

Email notifications use simple text templates with the following format:

```
Hello,

You have a new notification:
[Notification Message]

Best regards,
Support Team
```

In a production environment, you would use more sophisticated HTML templates with proper styling and branding.
