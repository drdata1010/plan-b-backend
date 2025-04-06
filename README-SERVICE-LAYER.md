# Service Layer Documentation

This document describes the service layer architecture in the Support Ticket System.

## Overview

The service layer provides business logic and operations for the application. It consists of the following main services:

1. **UserService** - Firebase user management, profile operations, role management
2. **TicketService** - CRUD operations, status management, assignment logic, comment handling, attachment processing
3. **ExpertService** - Profile management, availability handling, consultation scheduling, rating system
4. **ChatService** - Session management, AI model support, WebSocket integration, message persistence
5. **NotificationService** - Email notifications via AWS SES, real-time notifications via WebSocket, status updates

## Service Interfaces

### UserService

Handles user management operations including:
- Firebase user integration
- User profile CRUD operations
- Role management
- Account status management

Key methods:
- `createUserProfile` - Creates a new user profile from Firebase user data
- `getUserProfileByFirebaseUid` - Gets a user profile by Firebase UID
- `updateUserProfile` - Updates a user profile
- `addRoleToUser` / `removeRoleFromUser` - Manages user roles
- `disableUserAccount` / `enableUserAccount` - Manages account status

### TicketService

Handles ticket management operations including:
- Ticket CRUD operations
- Status and priority management
- Expert assignment
- Comment handling
- Attachment processing with S3

Key methods:
- `createTicket` - Creates a new support ticket
- `updateTicketStatus` - Updates the status of a ticket
- `assignTicketToExpert` - Assigns a ticket to an expert
- `addComment` / `addReply` - Manages ticket comments
- `addAttachment` - Adds file attachments to tickets or comments
- `resolveTicket` / `reopenTicket` - Handles ticket resolution workflow

### ExpertService

Handles expert management operations including:
- Expert profile management
- Availability scheduling
- Consultation management
- Rating system

Key methods:
- `createExpertProfile` - Creates a new expert profile
- `setAvailabilityStatus` - Sets an expert's availability status
- `addAvailabilitySchedule` - Adds availability schedule for an expert
- `scheduleConsultation` - Schedules a consultation with an expert
- `rateConsultation` - Rates a completed consultation
- `getAverageRating` - Gets the average rating for an expert

### ChatService

Handles chat operations including:
- Chat session management
- Message handling
- AI model integration
- WebSocket communication

Key methods:
- `createChatSession` - Creates a new chat session
- `addMessage` - Adds a message to a chat session
- `sendMessageToAI` - Sends a message to an AI model and gets a response
- `createAIChatSession` - Creates an AI chat session
- `getUnreadMessages` - Gets unread messages for a user

### NotificationService

Handles notification operations including:
- Email notifications via AWS SES
- Real-time notifications via WebSocket
- Status updates

Key methods:
- `sendNotification` - Sends a notification to a user
- `sendTicketCreatedNotification` - Sends a ticket created notification
- `sendNewMessageNotification` - Sends a new message notification
- `sendWelcomeEmail` - Sends a welcome email to a new user
- `getUnreadNotifications` - Gets unread notifications for a user

## Service Implementation Details

### Firebase Integration

The `UserServiceImpl` integrates with Firebase Authentication:
- Creates and updates user profiles in sync with Firebase
- Manages custom claims for roles
- Handles account status changes

### AWS Integration

The services integrate with AWS services:
- `TicketServiceImpl` uses S3Service for file storage
- `NotificationServiceImpl` uses SESService for email notifications
- `ChatServiceImpl` uses AIService for AI model integration

### WebSocket Integration

The `ChatServiceImpl` and `NotificationServiceImpl` integrate with WebSocket:
- Send real-time messages and notifications
- Handle message delivery status
- Support AI model responses

### Transaction Management

All service implementations use Spring's `@Transactional` annotation to ensure data consistency:
- Atomic operations for complex workflows
- Proper rollback in case of errors
- Isolation levels for concurrent operations

## Service Dependencies

The services have the following dependencies:

### UserServiceImpl
- UserProfileRepository
- FirebaseAuth

### TicketServiceImpl
- TicketRepository
- UserProfileRepository
- ExpertRepository
- TicketCommentRepository
- AttachmentRepository
- S3Service
- NotificationService

### ExpertServiceImpl
- ExpertRepository
- UserProfileRepository
- ExpertAvailabilityScheduleRepository
- ConsultationRepository
- NotificationService

### ChatServiceImpl
- ChatSessionRepository
- ChatMessageRepository
- UserProfileRepository
- ExpertRepository
- TicketRepository
- AttachmentRepository
- S3Service
- AIService
- NotificationService

### NotificationServiceImpl
- UserProfileRepository
- SESService
- WebSocketService

## Error Handling

The service layer implements comprehensive error handling:

- Custom exceptions for different error scenarios
- Proper logging of errors
- Consistent error responses
- Transaction rollback for failed operations

Common exceptions:
- `ResourceNotFoundException` - When a requested resource is not found
- `UnauthorizedException` - When a user is not authorized to perform an operation
- `ValidationException` - When input data fails validation
- `AWSServiceException` - When an AWS service operation fails

## Auditing

All service operations are audited using Spring Data JPA's auditing support:
- Creation and modification timestamps
- User tracking for all operations
- Version control for optimistic locking

## Security

The service layer implements security checks:
- Role-based access control
- Owner-based access control
- Data isolation between users
- Input validation and sanitization

## Pagination and Sorting

List operations support pagination and sorting:
- Page-based results for large data sets
- Customizable page size
- Multiple sort criteria
- Filtering options

## Asynchronous Operations

Long-running operations are handled asynchronously:
- File uploads and downloads
- Email sending
- AI model interactions
- Batch operations

## Caching

Frequently accessed data is cached:
- User profiles
- Expert profiles
- Ticket metadata
- Configuration settings
