package com.planb.supportticket.controller;

import com.planb.supportticket.dto.*;
import com.planb.supportticket.entity.*;
import com.planb.supportticket.entity.enums.TicketStatus;
import com.planb.supportticket.enums.NotificationType;
import com.planb.supportticket.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for administrative operations.
 * Requires ADMIN role for all endpoints.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;
    private final TicketService ticketService;
    private final ExpertService expertService;
    private final ChatService chatService;
    private final NotificationService notificationService;

    /**
     * Gets system statistics.
     *
     * @return the system statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        List<UserProfile> users = userService.getAllUserProfiles();
        stats.put("totalUsers", users.size());
        stats.put("activeUsers", users.stream().filter(u -> !u.isAccountDisabled()).count());

        // Expert statistics
        List<UserProfile> experts = userService.getUserProfilesByRole("ROLE_EXPERT");
        stats.put("totalExperts", experts.size());

        // Ticket statistics
        Page<Ticket> tickets = ticketService.getAllTicketsOrderByCreatedAtDesc(Pageable.unpaged());
        stats.put("totalTickets", tickets.getTotalElements());
        stats.put("openTickets", tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count());
        stats.put("resolvedTickets", tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count());

        return ResponseEntity.ok(stats);
    }

    /**
     * Gets all user profiles with pagination.
     *
     * @param pageable the pagination information
     * @return a page of user profiles
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileDTO>> getAllUsers(Pageable pageable) {
        Page<UserProfile> users = Page.empty(pageable); // Placeholder - would use a repository method

        Page<UserProfileDTO> response = users.map(this::convertToUserDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all tickets with pagination.
     *
     * @param pageable the pagination information
     * @return a page of tickets
     */
    @GetMapping("/tickets")
    public ResponseEntity<Page<TicketResponse>> getAllTickets(Pageable pageable) {
        Page<Ticket> tickets = ticketService.getAllTicketsOrderByCreatedAtDesc(pageable);

        Page<TicketResponse> response = tickets.map(this::convertToTicketResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all experts with pagination.
     *
     * @param pageable the pagination information
     * @return a page of experts
     */
    @GetMapping("/experts")
    public ResponseEntity<Page<ExpertResponse>> getAllExperts(Pageable pageable) {
        Page<Expert> experts = expertService.getAllExperts(pageable);

        Page<ExpertResponse> response = experts.map(this::convertToExpertResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all consultations with pagination.
     *
     * @param pageable the pagination information
     * @return a page of consultations
     */
    @GetMapping("/consultations")
    public ResponseEntity<Page<ConsultationResponse>> getAllConsultations(Pageable pageable) {
        Page<Consultation> consultations = Page.empty(pageable); // Placeholder - would use a repository method

        Page<ConsultationResponse> response = consultations.map(this::convertToConsultationResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all chat sessions with pagination.
     *
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    @GetMapping("/chat-sessions")
    public ResponseEntity<Page<ChatSessionResponse>> getAllChatSessions(Pageable pageable) {
        Page<ChatSession> chatSessions = Page.empty(pageable); // Placeholder - would use a repository method

        Page<ChatSessionResponse> response = chatSessions.map(this::convertToChatSessionResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a system notification to all users.
     *
     * @param message the notification message
     * @return success message
     */
    @PostMapping("/notifications/system")
    public ResponseEntity<Map<String, String>> sendSystemNotification(@RequestParam String message) {
        notificationService.sendSystemNotification(message, NotificationType.INFO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "System notification sent successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a notification to users with a specific role.
     *
     * @param role the role
     * @param message the notification message
     * @return success message
     */
    @PostMapping("/notifications/role")
    public ResponseEntity<Map<String, String>> sendRoleNotification(
            @RequestParam String role,
            @RequestParam String message) {

        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("Admin Notification");
        notification.setContent(message);
        notification.setType(NotificationType.SYSTEM);

        notificationService.sendNotificationToRole(role, notification);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification sent to users with role: " + role);
        return ResponseEntity.ok(response);
    }

    /**
     * Assigns a role to a user.
     *
     * @param userId the user ID
     * @param role the role to assign
     * @return the updated user profile
     */
    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<UserProfileDTO> assignRoleToUser(
            @PathVariable UUID userId,
            @RequestParam String role) {

        UserProfile userProfile = userService.addRoleToUser(userId, role);

        UserProfileDTO response = convertToUserDTO(userProfile);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a role from a user.
     *
     * @param userId the user ID
     * @param role the role to remove
     * @return the updated user profile
     */
    @DeleteMapping("/users/{userId}/roles")
    public ResponseEntity<UserProfileDTO> removeRoleFromUser(
            @PathVariable UUID userId,
            @RequestParam String role) {

        UserProfile userProfile = userService.removeRoleFromUser(userId, role);

        UserProfileDTO response = convertToUserDTO(userProfile);
        return ResponseEntity.ok(response);
    }

    /**
     * Disables a user account.
     *
     * @param userId the user ID
     * @return the updated user profile
     */
    @PostMapping("/users/{userId}/disable")
    public ResponseEntity<UserProfileDTO> disableUserAccount(@PathVariable UUID userId) {
        UserProfile userProfile = userService.disableUserAccount(userId);

        UserProfileDTO response = convertToUserDTO(userProfile);
        return ResponseEntity.ok(response);
    }

    /**
     * Enables a user account.
     *
     * @param userId the user ID
     * @return the updated user profile
     */
    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<UserProfileDTO> enableUserAccount(@PathVariable UUID userId) {
        UserProfile userProfile = userService.enableUserAccount(userId);

        UserProfileDTO response = convertToUserDTO(userProfile);
        return ResponseEntity.ok(response);
    }

    /**
     * Converts a UserProfile entity to a UserProfileDTO.
     *
     * @param userProfile the user profile entity
     * @return the user profile DTO
     */
    private UserProfileDTO convertToUserDTO(UserProfile userProfile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(userProfile.getId());
        dto.setFirebaseUid(userProfile.getFirebaseUid());
        dto.setEmail(userProfile.getEmail());
        dto.setDisplayName(userProfile.getDisplayName());
        dto.setFirstName(userProfile.getFirstName());
        dto.setLastName(userProfile.getLastName());
        dto.setPhoneNumber(userProfile.getMobileNumber());
        dto.setProfilePictureUrl(userProfile.getProfilePictureUrl());
        dto.setBio(userProfile.getBio());
        dto.setLastLogin(userProfile.getLastLogin());
        dto.setEmailVerified(userProfile.isEmailVerified());
        dto.setAccountDisabled(userProfile.isAccountDisabled());
        dto.setRoles(userProfile.getRoles());
        return dto;
    }

    /**
     * Converts a Ticket entity to a TicketResponse DTO.
     *
     * @param ticket the ticket entity
     * @return the ticket response DTO
     */
    private TicketResponse convertToTicketResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setClassification(ticket.getClassification());
        response.setArea(ticket.getArea());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setDueDate(ticket.getDueDate());
        response.setResolvedAt(ticket.getResolvedAt());
        response.setClosedAt(ticket.getClosedAt());

        if (ticket.getUser() != null) {
            response.setUserId(ticket.getUser().getId());
            response.setUserName(ticket.getUser().getDisplayName());
        }

        if (ticket.getAssignedExpert() != null) {
            response.setExpertId(ticket.getAssignedExpert().getId());
            response.setExpertName(ticket.getAssignedExpert().getUserProfile().getDisplayName());
        }

        response.setCommentCount(ticket.getCommentCount());

        return response;
    }

    /**
     * Converts an Expert entity to an ExpertResponse DTO.
     *
     * @param expert the expert entity
     * @return the expert response DTO
     */
    private ExpertResponse convertToExpertResponse(Expert expert) {
        ExpertResponse response = new ExpertResponse();
        response.setId(expert.getId());
        response.setBio(expert.getBio());
        response.setSpecializations(expert.getSpecializations().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet()));
        response.setHourlyRate(expert.getHourlyRate() != null ?
                new java.math.BigDecimal(expert.getHourlyRate().toString()) : null);
        response.setRating(expert.getRating());
        response.setAvailable(expert.isAvailable());
        response.setAvailableFrom(expert.getAvailableFrom());
        response.setAvailableTo(expert.getAvailableTo());

        if (expert.getUserProfile() != null) {
            response.setUserId(expert.getUserProfile().getId());
            response.setDisplayName(expert.getUserProfile().getDisplayName());
            response.setEmail(expert.getUserProfile().getEmail());
            response.setProfilePictureUrl(expert.getUserProfile().getProfilePictureUrl());
        }

        return response;
    }

    /**
     * Converts a Consultation entity to a ConsultationResponse DTO.
     *
     * @param consultation the consultation entity
     * @return the consultation response DTO
     */
    private ConsultationResponse convertToConsultationResponse(Consultation consultation) {
        ConsultationResponse response = new ConsultationResponse();
        response.setId(consultation.getId());
        response.setScheduledAt(consultation.getScheduledAt());
        response.setEndTime(consultation.getEndTime());
        response.setDurationMinutes(consultation.getDurationMinutes());
        response.setStatus(consultation.getStatus());
        response.setMeetingLink(consultation.getMeetingLink());
        response.setNotes(consultation.getNotes());
        response.setUserRating(consultation.getUserRating());
        response.setUserFeedback(consultation.getUserFeedback());
        response.setExpertNotes(consultation.getExpertNotes());
        response.setCancelledReason(consultation.getCancelledReason());
        response.setCancelledAt(consultation.getCancelledAt());
        response.setCancelledBy(consultation.getCancelledBy());

        if (consultation.getUser() != null) {
            response.setUserId(consultation.getUser().getId());
            response.setUserName(consultation.getUser().getDisplayName());
        }

        if (consultation.getExpert() != null) {
            response.setExpertId(consultation.getExpert().getId());
            if (consultation.getExpert().getUserProfile() != null) {
                response.setExpertName(consultation.getExpert().getUserProfile().getDisplayName());
            }
        }

        if (consultation.getTicket() != null) {
            response.setTicketId(consultation.getTicket().getId());
            response.setTicketTitle(consultation.getTicket().getTitle());
        }

        return response;
    }

    /**
     * Converts a ChatSession entity to a ChatSessionResponse DTO.
     *
     * @param chatSession the chat session entity
     * @return the chat session response DTO
     */
    private ChatSessionResponse convertToChatSessionResponse(ChatSession chatSession) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(chatSession.getId());
        response.setTitle(chatSession.getTitle());
        response.setStartedAt(chatSession.getStartedAt());
        response.setEndedAt(chatSession.getEndedAt());
        response.setActive(chatSession.isActive());
        response.setSessionType(chatSession.getSessionType());

        if (chatSession.getUser() != null) {
            response.setUserId(chatSession.getUser().getId());
            response.setUserName(chatSession.getUser().getDisplayName());
        }

        if (chatSession.getExpert() != null) {
            response.setExpertId(chatSession.getExpert().getId());
            if (chatSession.getExpert().getUserProfile() != null) {
                response.setExpertName(chatSession.getExpert().getUserProfile().getDisplayName());
            }
        }

        if (chatSession.getTicket() != null) {
            response.setTicketId(chatSession.getTicket().getId());
            response.setTicketTitle(chatSession.getTicket().getTitle());
        }

        return response;
    }
}
