package com.planb.supportticket.controller;

import com.planb.supportticket.dto.TicketCommentDTO;
import com.planb.supportticket.dto.TicketDTO;
import com.planb.supportticket.dto.TicketFilter;
import com.planb.supportticket.dto.TicketResponse;
import com.planb.supportticket.entity.Attachment;
import com.planb.supportticket.entity.Ticket;
import com.planb.supportticket.entity.TicketComment;
import com.planb.supportticket.entity.enums.TicketStatus;
import com.planb.supportticket.entity.enums.TicketPriority;
import com.planb.supportticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for ticket operations.
 * Handles ticket CRUD operations, comments, and attachments.
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {
    private final TicketService ticketService;

    /**
     * Creates a new ticket.
     *
     * @param request the ticket request
     * @param userDetails the authenticated user
     * @return the created ticket
     */
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Ticket ticket = ticketService.createTicket(request, userId);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets tickets with filtering and pagination.
     *
     * @param filter the ticket filter
     * @param pageable the pagination information
     * @param userDetails the authenticated user
     * @return a page of tickets
     */
    @GetMapping
    public ResponseEntity<Page<TicketResponse>> getTickets(
            TicketFilter filter,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        Page<Ticket> tickets;

        if (filter != null) {
            if (filter.getUserId() != null) {
                tickets = ticketService.getTicketsByUserId(filter.getUserId(), pageable);
            } else if (filter.getExpertId() != null) {
                tickets = ticketService.getTicketsByExpertId(filter.getExpertId(), pageable);
            } else if (filter.getStatus() != null) {
                tickets = ticketService.getTicketsByStatus(filter.getStatus(), pageable);
            } else if (filter.getPriority() != null) {
                tickets = ticketService.getTicketsByPriority(filter.getPriority(), pageable);
            } else if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
                tickets = ticketService.searchTickets(filter.getKeyword(), pageable);
            } else {
                tickets = ticketService.getAllTickets(pageable);
            }
        } else {
            tickets = ticketService.getAllTickets(pageable);
        }

        Page<TicketResponse> response = tickets.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a ticket by ID.
     *
     * @param id the ticket ID
     * @return the ticket
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable UUID id) {
        Ticket ticket = ticketService.getTicketById(id);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a ticket.
     *
     * @param id the ticket ID
     * @param request the updated ticket data
     * @return the updated ticket
     */
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable UUID id,
            @RequestBody TicketDTO request) {

        Ticket ticket = ticketService.updateTicket(id, request);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a ticket.
     *
     * @param id the ticket ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the status of a ticket.
     *
     * @param id the ticket ID
     * @param status the new status
     * @return the updated ticket
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable UUID id,
            @RequestParam TicketStatus status) {

        Ticket ticket = ticketService.updateTicketStatus(id, status);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the priority of a ticket.
     *
     * @param id the ticket ID
     * @param priority the new priority
     * @return the updated ticket
     */
    @PatchMapping("/{id}/priority")
    public ResponseEntity<TicketResponse> updateTicketPriority(
            @PathVariable UUID id,
            @RequestParam TicketPriority priority) {

        Ticket ticket = ticketService.updateTicketPriority(id, priority);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Assigns a ticket to an expert.
     *
     * @param id the ticket ID
     * @param expertId the expert ID
     * @return the updated ticket
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID id,
            @RequestParam UUID expertId) {

        Ticket ticket = ticketService.assignTicketToExpert(id, expertId);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Unassigns a ticket from an expert.
     *
     * @param id the ticket ID
     * @return the updated ticket
     */
    @PostMapping("/{id}/unassign")
    public ResponseEntity<TicketResponse> unassignTicket(@PathVariable UUID id) {
        Ticket ticket = ticketService.unassignTicket(id);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a comment to a ticket.
     *
     * @param id the ticket ID
     * @param commentDTO the comment data
     * @param userDetails the authenticated user
     * @return the created comment
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketCommentDTO> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody TicketCommentDTO commentDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        TicketComment comment = ticketService.addComment(id, commentDTO, userId);

        TicketCommentDTO response = convertToCommentDTO(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets comments for a ticket.
     *
     * @param id the ticket ID
     * @return a list of comments
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TicketCommentDTO>> getComments(@PathVariable UUID id) {
        List<TicketComment> comments = ticketService.getComments(id);

        List<TicketCommentDTO> response = comments.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Adds a reply to a comment.
     *
     * @param id the ticket ID
     * @param commentId the parent comment ID
     * @param commentDTO the reply data
     * @param userDetails the authenticated user
     * @return the created reply
     */
    @PostMapping("/{id}/comments/{commentId}/replies")
    public ResponseEntity<TicketCommentDTO> addReply(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @Valid @RequestBody TicketCommentDTO commentDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        TicketComment reply = ticketService.addReply(commentId, commentDTO, userId);

        TicketCommentDTO response = convertToCommentDTO(reply);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Adds an attachment to a ticket.
     *
     * @param id the ticket ID
     * @param file the file to attach
     * @param userDetails the authenticated user
     * @return the created attachment
     */
    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Attachment> addAttachment(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Attachment attachment = ticketService.addAttachment(id, file, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
    }

    /**
     * Gets attachments for a ticket.
     *
     * @param id the ticket ID
     * @return a list of attachments
     */
    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<Attachment>> getAttachments(@PathVariable UUID id) {
        List<Attachment> attachments = ticketService.getAttachments(id);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Resolves a ticket.
     *
     * @param id the ticket ID
     * @param resolution the resolution message
     * @param userDetails the authenticated user
     * @return the resolved ticket
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<TicketResponse> resolveTicket(
            @PathVariable UUID id,
            @RequestParam String resolution,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Ticket ticket = ticketService.resolveTicket(id, resolution, userId);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Reopens a ticket.
     *
     * @param id the ticket ID
     * @param reason the reason for reopening
     * @param userDetails the authenticated user
     * @return the reopened ticket
     */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<TicketResponse> reopenTicket(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Ticket ticket = ticketService.reopenTicket(id, reason, userId);

        TicketResponse response = convertToResponse(ticket);
        return ResponseEntity.ok(response);
    }

    /**
     * Converts a Ticket entity to a TicketResponse DTO.
     *
     * @param ticket the ticket entity
     * @return the ticket response DTO
     */
    private TicketResponse convertToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setCategory(ticket.getCategory() != null ? ticket.getCategory().name() : null);
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
     * Converts a TicketComment entity to a TicketCommentDTO.
     *
     * @param comment the ticket comment entity
     * @return the ticket comment DTO
     */
    private TicketCommentDTO convertToCommentDTO(TicketComment comment) {
        TicketCommentDTO dto = new TicketCommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setInternalNote(comment.isInternalNote());

        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUserName(comment.getUser().getDisplayName());
        }

        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }

        // Add replies if any
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(this::convertToCommentDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Gets the user ID from the UserDetails.
     *
     * @param userDetails the user details
     * @return the user ID
     */
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // In a real implementation, you would extract the user ID from the UserDetails
        // This is a placeholder implementation
        return UUID.fromString(userDetails.getUsername());
    }
}
