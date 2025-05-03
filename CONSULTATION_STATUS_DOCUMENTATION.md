# Consultation Status Documentation

This document provides detailed information about the `ConsultationStatus` enum and related functionality in the Plan B Support Ticket System.

## Table of Contents
- [ConsultationStatus Enum](#consultationstatus-enum)
- [Consultation Entity](#consultation-entity)
- [Consultation Service](#consultation-service)
- [Consultation Controller](#consultation-controller)
- [Status Transition Flow](#status-transition-flow)
- [API Endpoints](#api-endpoints)
- [Examples](#examples)

## ConsultationStatus Enum

The `ConsultationStatus` enum defines the possible states of a consultation in the system.

### Location
`src/main/java/com/planb/supportticket/entity/enums/ConsultationStatus.java`

### Values
- `SCHEDULED`: The consultation is scheduled for a future date and time
- `IN_PROGRESS`: The consultation is currently in progress
- `COMPLETED`: The consultation has been completed successfully
- `CANCELLED`: The consultation was cancelled before it started
- `NO_SHOW`: The consultation did not occur because one of the participants did not show up

### Usage
```java
import com.planb.supportticket.entity.enums.ConsultationStatus;

// Example usage
Consultation consultation = new Consultation();
consultation.setStatus(ConsultationStatus.SCHEDULED);
```

## Consultation Entity

The `Consultation` entity represents a scheduled consultation between a user and an expert.

### Location
`src/main/java/com/planb/supportticket/entity/Consultation.java`

### Key Fields
- `id`: Unique identifier (UUID)
- `user`: The user participating in the consultation
- `expert`: The expert providing the consultation
- `ticket`: Optional related support ticket
- `scheduledAt`: Date and time when the consultation is scheduled
- `endTime`: Date and time when the consultation ended
- `durationMinutes`: Duration of the consultation in minutes
- `status`: Current status of the consultation (ConsultationStatus enum)
- `meetingLink`: URL for the virtual meeting
- `notes`: General notes about the consultation
- `userRating`: Rating provided by the user (1-5)
- `userFeedback`: Feedback provided by the user
- `expertNotes`: Notes provided by the expert
- `cancelledReason`: Reason for cancellation (if applicable)
- `cancelledAt`: Date and time when the consultation was cancelled
- `cancelledBy`: Who cancelled the consultation

### Key Methods
- `updateStatus(ConsultationStatus newStatus)`: Updates the status and performs related actions
- `cancel(String reason, String cancelledBy)`: Cancels the consultation
- `complete(String expertNotes)`: Marks the consultation as completed
- `startConsultation()`: Marks the consultation as in progress
- `rate(Integer rating, String feedback)`: Adds a rating and feedback to the consultation

## Consultation Service

The `ConsultationService` interface and its implementation handle business logic related to consultations.

### Location
- Interface: `src/main/java/com/planb/supportticket/service/ConsultationService.java`
- Implementation: `src/main/java/com/planb/supportticket/service/impl/ConsultationServiceImpl.java`

### Key Methods
- `scheduleConsultation(UUID userId, UUID expertId, ConsultationDTO consultationDTO)`: Schedules a new consultation
- `getConsultationById(UUID id)`: Retrieves a consultation by ID
- `updateConsultation(UUID id, ConsultationDTO consultationDTO)`: Updates a consultation
- `cancelConsultation(UUID id, String reason, UUID userId)`: Cancels a consultation
- `startConsultation(UUID id)`: Starts a consultation
- `completeConsultation(UUID id, String notes)`: Completes a consultation
- `markAsNoShow(UUID id)`: Marks a consultation as no-show
- `rateConsultation(UUID id, int rating, String feedback, UUID userId)`: Rates a consultation
- `getConsultationsByExpertId(UUID expertId, Pageable pageable)`: Gets consultations for an expert
- `getConsultationsByUserId(UUID userId, Pageable pageable)`: Gets consultations for a user
- `getConsultationsByStatus(ConsultationStatus status, Pageable pageable)`: Gets consultations by status
- `getUpcomingConsultationsForExpert(UUID expertId)`: Gets upcoming consultations for an expert
- `getUpcomingConsultationsForUser(UUID userId)`: Gets upcoming consultations for a user

## Consultation Controller

The `ConsultationController` class handles HTTP requests related to consultations.

### Location
`src/main/java/com/planb/supportticket/controller/ConsultationController.java`

### API Endpoints
- `POST /api/consultations/expert/{expertId}`: Schedules a consultation with an expert
- `GET /api/consultations/{id}`: Gets a consultation by ID
- `PUT /api/consultations/{id}`: Updates a consultation
- `POST /api/consultations/{id}/cancel`: Cancels a consultation
- `POST /api/consultations/{id}/start`: Starts a consultation
- `POST /api/consultations/{id}/complete`: Completes a consultation
- `POST /api/consultations/{id}/no-show`: Marks a consultation as no-show
- `POST /api/consultations/{id}/rate`: Rates a consultation
- `GET /api/consultations/expert/{expertId}`: Gets consultations for an expert
- `GET /api/consultations/user/{userId}`: Gets consultations for a user
- `GET /api/consultations/expert/{expertId}/upcoming`: Gets upcoming consultations for an expert
- `GET /api/consultations/user/{userId}/upcoming`: Gets upcoming consultations for a user
- `GET /api/consultations/status/{status}`: Gets consultations by status
- `GET /api/consultations/ticket/{ticketId}`: Gets consultations for a ticket

## Status Transition Flow

The typical flow of a consultation through different statuses:

1. **SCHEDULED**: Initial status when a consultation is created
2. **IN_PROGRESS**: When the consultation starts at the scheduled time
3. **COMPLETED**: When the consultation ends successfully
4. **CANCELLED**: If the consultation is cancelled before it starts
5. **NO_SHOW**: If one of the participants doesn't show up

### Valid Status Transitions
- SCHEDULED → IN_PROGRESS
- SCHEDULED → CANCELLED
- SCHEDULED → NO_SHOW
- IN_PROGRESS → COMPLETED
- IN_PROGRESS → CANCELLED

## API Endpoints

### Schedule a Consultation
```
POST /api/consultations/expert/{expertId}
```
Request Body:
```json
{
  "scheduledAt": "2025-05-15T14:30:00",
  "durationMinutes": 60,
  "notes": "Discuss technical issues with the application",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "meetingLink": "https://meet.example.com/abc123"
}
```

### Update a Consultation
```
PUT /api/consultations/{id}
```
Request Body:
```json
{
  "scheduledAt": "2025-05-16T15:30:00",
  "durationMinutes": 45,
  "notes": "Updated notes for the consultation",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "meetingLink": "https://meet.example.com/xyz789"
}
```

### Cancel a Consultation
```
POST /api/consultations/{id}/cancel
```
Request Parameters:
- `reason`: Reason for cancellation

### Start a Consultation
```
POST /api/consultations/{id}/start
```

### Complete a Consultation
```
POST /api/consultations/{id}/complete
```
Request Parameters:
- `notes`: Expert's notes about the consultation

### Rate a Consultation
```
POST /api/consultations/{id}/rate
```
Request Parameters:
- `rating`: Rating value (1-5)
- `feedback`: Optional feedback text

## Examples

### Creating a Consultation
```java
// Create a consultation DTO
ConsultationDTO consultationDTO = ConsultationDTO.builder()
    .scheduledAt(LocalDateTime.now().plusDays(1))
    .durationMinutes(60)
    .notes("Initial consultation to discuss requirements")
    .ticketId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    .meetingLink("https://meet.example.com/abc123")
    .build();

// Schedule the consultation
Consultation consultation = consultationService.scheduleConsultation(
    userId, expertId, consultationDTO);
```

### Updating a Consultation Status
```java
// Get the consultation
Consultation consultation = consultationService.getConsultationById(consultationId);

// Update the status
consultation.updateStatus(ConsultationStatus.IN_PROGRESS);

// Save the changes
consultationRepository.save(consultation);
```

### Cancelling a Consultation
```java
// Cancel the consultation
Consultation cancelledConsultation = consultationService.cancelConsultation(
    consultationId, "Expert unavailable", userId);
```

### Completing a Consultation
```java
// Complete the consultation
Consultation completedConsultation = consultationService.completeConsultation(
    consultationId, "All issues resolved successfully");
```

### Finding Consultations by Status
```java
// Get consultations with SCHEDULED status
Page<Consultation> scheduledConsultations = consultationService.getConsultationsByStatus(
    ConsultationStatus.SCHEDULED, PageRequest.of(0, 10));
```
