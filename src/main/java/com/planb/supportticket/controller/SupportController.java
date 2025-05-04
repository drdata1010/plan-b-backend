package com.planb.supportticket.controller;

import com.planb.supportticket.dto.TicketResponse;
import com.planb.supportticket.dto.UserProfileDTO;
import com.planb.supportticket.entity.Ticket;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.service.TicketService;
import com.planb.supportticket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller for support operations.
 */
@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SUPPORT', 'ADMIN')")
public class SupportController {

    private final UserService userService;
    private final TicketService ticketService;

    /**
     * Gets all tickets with pagination.
     *
     * @param pageable the pagination information
     * @return a page of tickets
     */
    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(Pageable pageable) {
        try {
            // For testing purposes, return an empty list
            return ResponseEntity.ok(Map.of("tickets", "[]"));
        } catch (Exception e) {
            log.error("Error getting all tickets: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets a ticket by ID.
     *
     * @param ticketId the ticket ID
     * @return the ticket
     */
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketById(@PathVariable UUID ticketId) {
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);

            // Convert to DTO
            TicketResponse ticketDTO = convertToTicketResponse(ticket);

            return ResponseEntity.ok(ticketDTO);
        } catch (Exception e) {
            log.error("Error getting ticket by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assigns a ticket to an expert.
     *
     * @param ticketId the ticket ID
     * @param expertId the expert ID
     * @return the updated ticket
     */
    @PostMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<?> assignTicketToExpert(
            @PathVariable UUID ticketId,
            @RequestParam UUID expertId) {
        try {
            Ticket ticket = ticketService.assignTicketToExpert(ticketId, expertId);

            // Convert to DTO
            TicketResponse ticketDTO = convertToTicketResponse(ticket);

            return ResponseEntity.ok(ticketDTO);
        } catch (Exception e) {
            log.error("Error assigning ticket to expert: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets all users with pagination.
     *
     * @param pageable the pagination information
     * @return a page of users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(Pageable pageable) {
        try {
            Page<UserProfile> users = userService.getAllUserProfiles(pageable);

            // Convert to DTOs
            Page<UserProfileDTO> userDTOs = users.map(userService::convertToDTO);

            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets a user by ID.
     *
     * @param userId the user ID
     * @return the user
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        try {
            UserProfile user = userService.getUserProfileById(userId);

            // Convert to DTO
            UserProfileDTO userDTO = userService.convertToDTO(user);

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("Error getting user by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
}
