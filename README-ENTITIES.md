# Entity Relationship Structure

This document describes the entity relationships in the Support Ticket System.

## Entity Overview

The system consists of the following main entities:

1. **UserProfile**: Represents a user in the system, extending Firebase authentication
2. **Expert**: Represents a support expert with specializations and availability
3. **Ticket**: Represents a support ticket with status, priority, and category
4. **TicketComment**: Represents comments on tickets, with support for threaded replies
5. **Consultation**: Represents scheduled consultations between users and experts
6. **ChatSession**: Represents a chat session between users and experts or AI
7. **ChatMessage**: Represents messages within a chat session
8. **ExpertAvailabilitySchedule**: Represents an expert's availability schedule
9. **Attachment**: Represents file attachments for tickets, comments, or messages

## Entity Relationships

### UserProfile
- Has many Tickets (one-to-many)
- Has many TicketComments (one-to-many)
- Has many Consultations (one-to-many)
- Has many ChatSessions (one-to-many)
- Has many ChatMessages as sender (one-to-many)
- Has many Attachments (one-to-many)
- Can be associated with one Expert profile (one-to-one)

### Expert
- Belongs to one UserProfile (one-to-one)
- Has many assigned Tickets (one-to-many)
- Has many Consultations (one-to-many)
- Has many ExpertAvailabilitySchedules (one-to-many)
- Has many ChatSessions (one-to-many)
- Has multiple Specializations (element collection)

### Ticket
- Belongs to one UserProfile (many-to-one)
- Can be assigned to one Expert (many-to-one)
- Has many TicketComments (one-to-many)
- Has many Consultations (one-to-many)
- Has many Attachments (one-to-many)
- Has many ChatSessions (one-to-many)

### TicketComment
- Belongs to one Ticket (many-to-one)
- Belongs to one UserProfile (many-to-one)
- Can have a parent TicketComment (self-referencing)
- Can have many replies (one-to-many, self-referencing)
- Has many Attachments (one-to-many)

### Consultation
- Belongs to one UserProfile (many-to-one)
- Belongs to one Expert (many-to-one)
- Can be associated with one Ticket (many-to-one)
- Can have one ChatSession (one-to-one)

### ChatSession
- Belongs to one UserProfile (many-to-one)
- Can belong to one Expert (many-to-one)
- Can be associated with one Ticket (many-to-one)
- Can be associated with one Consultation (one-to-one)
- Has many ChatMessages (one-to-many)

### ChatMessage
- Belongs to one ChatSession (many-to-one)
- Belongs to one UserProfile as sender (many-to-one)
- Has many Attachments (one-to-many)

### ExpertAvailabilitySchedule
- Belongs to one Expert (many-to-one)

### Attachment
- Belongs to one UserProfile (many-to-one)
- Can belong to one Ticket (many-to-one)
- Can belong to one TicketComment (many-to-one)
- Can belong to one ChatMessage (many-to-one)

## Auditing

All entities extend the `BaseEntity` class which provides:
- UUID ID field
- Creation timestamp
- Last update timestamp
- Created by user (Firebase UID)
- Updated by user (Firebase UID)
- Version field (for optimistic locking)

## Indexes

The following indexes are defined for performance optimization:

### UserProfile
- Email (for quick lookup)
- Firebase ID (for authentication)

### Expert
- Availability (for filtering available experts)
- Rating (for sorting by rating)

### Ticket
- Status (for filtering by status)
- Priority (for filtering by priority)
- Category (for filtering by category)
- User ID (for finding user's tickets)
- Expert ID (for finding expert's tickets)

### TicketComment
- Ticket ID (for finding ticket's comments)
- User ID (for finding user's comments)
- Parent ID (for finding replies to a comment)

### Consultation
- User ID (for finding user's consultations)
- Expert ID (for finding expert's consultations)
- Ticket ID (for finding consultations related to a ticket)
- Status (for filtering by status)
- Scheduled time (for finding upcoming consultations)

### ChatSession
- User ID (for finding user's chat sessions)
- Expert ID (for finding expert's chat sessions)
- Ticket ID (for finding chat sessions related to a ticket)

### ChatMessage
- Chat Session ID (for finding messages in a session)
- Sender ID (for finding messages sent by a user)

### ExpertAvailabilitySchedule
- Expert ID (for finding expert's availability)
- Day of Week (for finding availability on specific days)

### Attachment
- User ID (for finding user's attachments)
- Ticket ID (for finding attachments related to a ticket)
- Comment ID (for finding attachments related to a comment)
- Message ID (for finding attachments related to a message)

## Enumerations

The system uses the following enumerations:

- **TicketStatus**: OPEN, IN_PROGRESS, WAITING_FOR_CUSTOMER, WAITING_FOR_EXPERT, RESOLVED, CLOSED, CANCELLED
- **TicketPriority**: LOW, MEDIUM, HIGH, CRITICAL
- **TicketCategory**: TECHNICAL, BILLING, ACCOUNT, PRODUCT, FEATURE_REQUEST, BUG, GENERAL_INQUIRY, OTHER
- **ExpertSpecialization**: SOFTWARE_DEVELOPMENT, NETWORK_INFRASTRUCTURE, DATABASE_MANAGEMENT, CLOUD_SERVICES, CYBERSECURITY, MOBILE_DEVELOPMENT, WEB_DEVELOPMENT, DEVOPS, MACHINE_LEARNING, BUSINESS_INTELLIGENCE, GENERAL_IT
- **ExpertAvailability**: AVAILABLE, BUSY, AWAY, OFFLINE
- **ConsultationStatus**: SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
- **ChatSessionType**: USER_EXPERT, USER_AI, TICKET_RELATED, CONSULTATION, SUPPORT
- **MessageType**: TEXT, IMAGE, FILE, SYSTEM, AI_RESPONSE, USER_JOINED, USER_LEFT
