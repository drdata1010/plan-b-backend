# REST Controllers Documentation

This document describes the REST controllers in the Support Ticket System.

## Overview

The application provides the following REST controllers:

1. **AuthController** - Firebase token validation, role assignment, profile management
2. **TicketController** - Ticket CRUD operations, comments, attachments
3. **ExpertController** - Expert profile management, availability, specializations
4. **ConsultationController** - Consultation scheduling and management
5. **ChatController** - Chat sessions, messages, AI interactions
6. **UserController** - User profile management, role operations
7. **AdminController** - Administrative operations (requires ADMIN role)

## API Endpoints

### Auth API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/validate-token` | Validates a Firebase token and returns user information |
| POST | `/api/auth/roles/{userId}` | Assigns a role to a user |
| DELETE | `/api/auth/roles/{userId}` | Removes a role from a user |
| GET | `/api/auth/roles/{userId}` | Gets the roles for a user |
| PUT | `/api/auth/profile/{userId}` | Updates a user's profile |
| GET | `/api/auth/profile/{userId}` | Gets a user's profile |
| POST | `/api/auth/disable/{userId}` | Disables a user account |
| POST | `/api/auth/enable/{userId}` | Enables a user account |

### Ticket API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tickets` | Creates a new ticket |
| GET | `/api/tickets` | Gets tickets with filtering and pagination |
| GET | `/api/tickets/{id}` | Gets a ticket by ID |
| PUT | `/api/tickets/{id}` | Updates a ticket |
| DELETE | `/api/tickets/{id}` | Deletes a ticket |
| PATCH | `/api/tickets/{id}/status` | Updates the status of a ticket |
| PATCH | `/api/tickets/{id}/priority` | Updates the priority of a ticket |
| POST | `/api/tickets/{id}/assign` | Assigns a ticket to an expert |
| POST | `/api/tickets/{id}/unassign` | Unassigns a ticket from an expert |
| POST | `/api/tickets/{id}/comments` | Adds a comment to a ticket |
| GET | `/api/tickets/{id}/comments` | Gets comments for a ticket |
| POST | `/api/tickets/{id}/comments/{commentId}/replies` | Adds a reply to a comment |
| POST | `/api/tickets/{id}/attachments` | Adds an attachment to a ticket |
| GET | `/api/tickets/{id}/attachments` | Gets attachments for a ticket |
| POST | `/api/tickets/{id}/resolve` | Resolves a ticket |
| POST | `/api/tickets/{id}/reopen` | Reopens a ticket |

### Expert API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/experts` | Creates a new expert profile |
| GET | `/api/experts/{id}` | Gets an expert profile by ID |
| GET | `/api/experts/user/{userId}` | Gets an expert profile by user ID |
| PUT | `/api/experts/{id}` | Updates an expert profile |
| GET | `/api/experts` | Gets all expert profiles with pagination |
| GET | `/api/experts/available` | Gets available experts with pagination |
| GET | `/api/experts/specialization` | Gets experts by specialization with pagination |
| PATCH | `/api/experts/{id}/availability-status` | Sets an expert's availability status |
| PATCH | `/api/experts/{id}/availability-time` | Sets an expert's availability time range |
| POST | `/api/experts/{id}/schedule` | Adds an availability schedule for an expert |
| PUT | `/api/experts/{id}/schedule/{scheduleId}` | Updates an availability schedule |
| DELETE | `/api/experts/{id}/schedule/{scheduleId}` | Deletes an availability schedule |
| GET | `/api/experts/{id}/schedule` | Gets availability schedules for an expert |
| GET | `/api/experts/{id}/schedule/day` | Gets availability schedules for an expert by day of week |
| PATCH | `/api/experts/{id}/hourly-rate` | Updates an expert's hourly rate |
| POST | `/api/experts/{id}/specializations` | Adds a specialization to an expert |
| DELETE | `/api/experts/{id}/specializations` | Removes a specialization from an expert |
| GET | `/api/experts/{id}/specializations` | Gets specializations for an expert |
| GET | `/api/experts/{id}/rating` | Gets the average rating for an expert |
| GET | `/api/experts/search` | Searches for experts by keyword |

### Consultation API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/consultations/expert/{expertId}` | Schedules a consultation with an expert |
| GET | `/api/consultations/{id}` | Gets a consultation by ID |
| PUT | `/api/consultations/{id}` | Updates a consultation |
| POST | `/api/consultations/{id}/cancel` | Cancels a consultation |
| GET | `/api/consultations/expert/{expertId}` | Gets consultations for an expert with pagination |
| GET | `/api/consultations/user/{userId}` | Gets consultations for a user with pagination |
| GET | `/api/consultations/expert/{expertId}/upcoming` | Gets upcoming consultations for an expert |
| GET | `/api/consultations/user/{userId}/upcoming` | Gets upcoming consultations for a user |
| POST | `/api/consultations/{id}/complete` | Completes a consultation |
| POST | `/api/consultations/{id}/rate` | Rates a consultation |

### Chat API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat/sessions` | Creates a new chat session |
| GET | `/api/chat/sessions/{sessionId}` | Gets a chat session by ID |
| GET | `/api/chat/sessions/user/{userId}` | Gets chat sessions for a user with pagination |
| GET | `/api/chat/sessions/expert/{expertId}` | Gets chat sessions for an expert with pagination |
| GET | `/api/chat/sessions/ticket/{ticketId}` | Gets chat sessions for a ticket with pagination |
| GET | `/api/chat/sessions/user/{userId}/active` | Gets active chat sessions for a user |
| GET | `/api/chat/sessions/expert/{expertId}/active` | Gets active chat sessions for an expert |
| POST | `/api/chat/sessions/{sessionId}/end` | Ends a chat session |
| POST | `/api/chat/sessions/{sessionId}/messages` | Adds a message to a chat session |
| GET | `/api/chat/sessions/{sessionId}/messages` | Gets messages for a chat session with pagination |
| GET | `/api/chat/messages/{messageId}` | Gets a chat message by ID |
| POST | `/api/chat/messages/{messageId}/read` | Marks a message as read |
| POST | `/api/chat/messages/{messageId}/attachments` | Adds an attachment to a chat message |
| GET | `/api/chat/messages/{messageId}/attachments` | Gets attachments for a chat message |
| POST | `/api/chat/sessions/{sessionId}/ai` | Sends a message to an AI model and gets a response |
| POST | `/api/chat/ai/sessions` | Creates an AI chat session |
| GET | `/api/chat/ai/models` | Gets available AI models |
| GET | `/api/chat/sessions/type` | Gets chat sessions by type with pagination |
| GET | `/api/chat/messages/unread/count/{userId}` | Gets unread message count for a user |
| GET | `/api/chat/messages/unread/{userId}` | Gets unread messages for a user with pagination |
| DELETE | `/api/chat/messages/{messageId}` | Deletes a chat message |
| DELETE | `/api/chat/sessions/{sessionId}` | Deletes a chat session and all its messages |

### User API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Gets all user profiles |
| GET | `/api/users/{id}` | Gets a user profile by ID |
| GET | `/api/users/firebase/{firebaseUid}` | Gets a user profile by Firebase UID |
| GET | `/api/users/email` | Gets a user profile by email |
| PUT | `/api/users/{id}` | Updates a user profile |
| POST | `/api/users/{id}/profile-picture` | Updates a user's profile picture |
| GET | `/api/users/role/{role}` | Gets user profiles by role |
| POST | `/api/users/{id}/roles` | Adds a role to a user |
| DELETE | `/api/users/{id}/roles` | Removes a role from a user |
| GET | `/api/users/{id}/roles` | Gets the roles for a user |
| POST | `/api/users/{id}/disable` | Disables a user account |
| POST | `/api/users/{id}/enable` | Enables a user account |
| GET | `/api/users/{id}/has-role` | Checks if a user has a specific role |
| GET | `/api/users/me` | Gets the current user's profile |
| PUT | `/api/users/me` | Updates the current user's profile |
| DELETE | `/api/users/{id}` | Deletes a user profile |

### Admin API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/stats` | Gets system statistics |
| GET | `/api/admin/users` | Gets all user profiles with pagination |
| GET | `/api/admin/tickets` | Gets all tickets with pagination |
| GET | `/api/admin/experts` | Gets all experts with pagination |
| GET | `/api/admin/consultations` | Gets all consultations with pagination |
| GET | `/api/admin/chat-sessions` | Gets all chat sessions with pagination |
| POST | `/api/admin/notifications/system` | Sends a system notification to all users |
| POST | `/api/admin/notifications/role` | Sends a notification to users with a specific role |
| POST | `/api/admin/users/{userId}/roles` | Assigns a role to a user |
| DELETE | `/api/admin/users/{userId}/roles` | Removes a role from a user |
| POST | `/api/admin/users/{userId}/disable` | Disables a user account |
| POST | `/api/admin/users/{userId}/enable` | Enables a user account |

## Authentication and Authorization

All endpoints (except for public ones like `/api/auth/validate-token`) require authentication using Firebase JWT tokens. The token should be included in the `Authorization` header as a Bearer token:

```
Authorization: Bearer <firebase-jwt-token>
```

Role-based authorization is implemented for certain endpoints:
- Admin endpoints (`/api/admin/**`) require the `ADMIN` role
- Expert endpoints may require the `EXPERT` role
- Some operations on tickets and consultations require specific roles or ownership

## Request and Response Format

All requests and responses use JSON format. For file uploads, `multipart/form-data` is used.

### Common Response Format

Success responses follow this format:
```json
{
  "id": "uuid",
  "field1": "value1",
  "field2": "value2",
  ...
}
```

Error responses follow this format:
```json
{
  "timestamp": "2023-05-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/endpoint",
  "errors": [
    {
      "field": "fieldName",
      "message": "Error message"
    }
  ]
}
```

## Pagination and Filtering

Endpoints that return collections support pagination and filtering:

### Pagination Parameters

- `page`: Page number (0-based)
- `size`: Page size
- `sort`: Sort field and direction (e.g., `sort=createdAt,desc`)

Example: `/api/tickets?page=0&size=10&sort=createdAt,desc`

### Filtering Parameters

Filtering is supported through query parameters:

- `/api/tickets?status=OPEN`: Filter tickets by status
- `/api/tickets?priority=HIGH`: Filter tickets by priority
- `/api/tickets?userId=<uuid>`: Filter tickets by user ID
- `/api/tickets?expertId=<uuid>`: Filter tickets by expert ID
- `/api/tickets?keyword=search`: Search tickets by keyword

## WebSocket Integration

Some controllers (especially ChatController) integrate with WebSocket for real-time communication. See the WebSocket documentation for details on WebSocket endpoints and message formats.
